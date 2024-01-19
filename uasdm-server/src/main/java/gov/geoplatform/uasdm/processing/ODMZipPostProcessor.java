/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.processing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.odm.ODMFacade;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMProcessingTaskIF;
import gov.geoplatform.uasdm.odm.ODMUploadTaskIF;
import gov.geoplatform.uasdm.processing.ManagedDocument.DocumentInfo;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;
import net.lingala.zip4j.ZipFile;

/**
 * Can be used to download an allzip from S3 for a collection and selectively
 * re-upload files from that allzip to a different location in S3.
 * 
 * @author rrowlands
 */
public class ODMZipPostProcessor
{
  private static final Logger logger   = LoggerFactory.getLogger(ODMZipPostProcessor.class);

  public static final String  DEM_GDAL = Product.ODM_ALL_DIR + "/gdal";

  public static final String  POTREE   = Product.ODM_ALL_DIR + "/entwine_pointcloud";

  protected ODMUploadTaskIF   progressTask;

  protected CollectionIF      collection;

  protected String            filePrefix;

  protected CloseableFile     allZip;

  protected Product           product;

  public ODMZipPostProcessor(CollectionIF collection, ODMUploadTaskIF progressTask, Product product)
  {
    this.product = product;
    this.collection = collection;
    this.progressTask = progressTask;
  }

  public ProductIF processAllZip() throws InterruptedException
  {
    final String colName = FilenameUtils.getName(this.collection.getS3location().endsWith("/") ? this.collection.getS3location().substring(0, this.collection.getS3location().length() - 1) : this.collection.getS3location());

    final String folderName = "odm-" + colName + "-" + new Random().nextInt();

    try (CloseableFile unzippedParentFolder = new CloseableFile(FileUtils.getTempDirectory(), folderName))
    {
      try (CloseableFile allZip = getAllZip())
      {
        try (ZipFile zipFile = new ZipFile(allZip))
        {
          zipFile.extractAll(unzippedParentFolder.getAbsolutePath());
        }
        catch (IOException e)
        {
          throw new RuntimeException("ODM did not return any results. (There was a problem unzipping ODM's results zip file)", e);
        }

        this.cleanExistingProduct();

        this.product = (Product) this.collection.createProductIfNotExist();

        this.processProduct(product, new WorkflowTaskMonitor((AbstractWorkflowTask) this.progressTask), unzippedParentFolder);

        this.uploadAllZip(allZip);
      }
    }

    List<String> list = new ArrayList<String>();
    if (this.progressTask != null)
    {
      ODMProcessingTaskIF processingTask = this.progressTask.getProcessingTask();
      list = processingTask.getFileList();
    }

    List<DocumentIF> raws = collection.getDocuments().stream().filter(doc -> {
      return doc.getS3location().contains("/raw/");
    }).collect(Collectors.toList());

    for (DocumentIF raw : raws)
    {
      if (list.size() == 0 || list.contains(raw.getName()))
      {
        raw.addGeneratedProduct(product);
      }
    }

    // Create an associated ODMRun
    if (this.progressTask != null)
    {
      final ODMProcessingTask processingTask = (ODMProcessingTask) this.progressTask.getProcessingTask();
      final ODMRun odmRun = ODMRun.getForTask(processingTask.getOid());

      product.getProductHasDocumentChildDocuments().forEach(doc -> {

        // If a document already has an ODM run then it wasn't created in the
        // current run but a previous run. As such we do not want to assign it
        // as be generating from the current run
        if (doc.getGeneratingODMRun() == null)
        {
          odmRun.addODMRunOutputChild(doc).apply();
        }

        if (doc.getName().equals("report.pdf"))
        {
          odmRun.setReport(doc);
        }
      });

      odmRun.setOutput(processingTask.getOdmOutput());

      odmRun.setComponent((Collection) this.collection);

      odmRun.setRunEnd(new Date());

      odmRun.apply();
    }

    product.updateBoundingBox();

    IndexService.createStacItems(product);

    return this.product;
  }

  /**
   * This must be done before creating the product because the 'removeArtifact'
   * method will delete any existing products
   */
  protected void cleanExistingProduct()
  {
    if (this.progressTask != null)
    {
      try
      {

        if (truthy(this.progressTask.getProcessDem()))
        {
          this.collection.removeArtifacts(ImageryComponent.DEM, false);
        }

        if (truthy(this.progressTask.getProcessOrtho()))
        {
          this.collection.removeArtifacts(ImageryComponent.ORTHO, false);
        }

        if (truthy(this.progressTask.getProcessPtcloud()))
        {
          this.collection.removeArtifacts(ImageryComponent.PTCLOUD, false);
        }
      }
      finally
      {
        new GenerateMetadataCommand(this.collection).doIt();
      }
    }
  }

  protected void processProduct(Product product, StatusMonitorIF monitor, CloseableFile unzippedParentFolder) throws InterruptedException
  {
    if (this.progressTask != null && truthy(this.progressTask.getProcessDem()))
    {
      this.runProcessor(unzippedParentFolder, "odm_dem/dsm.tif", new ManagedDocument(buildS3Path(ImageryComponent.DEM, this.filePrefix, "dsm" + CogTifProcessor.COG_EXTENSION), this.product, this.collection, monitor));
      this.runProcessor(unzippedParentFolder, "odm_dem/dtm.tif", new ManagedDocument(buildS3Path(ImageryComponent.DEM, this.filePrefix, "dtm" + CogTifProcessor.COG_EXTENSION), this.product, this.collection, monitor));

      this.runProcessor(unzippedParentFolder, "odm_dem/dsm.tif", new HillshadeProcessor(buildS3Path(DEM_GDAL, this.filePrefix, "dsm" + CogTifProcessor.COG_EXTENSION), this.product, this.collection, monitor));

      // Add the report to the dem folder
      this.runProcessor(unzippedParentFolder, "odm_report/report.pdf", new ManagedDocument(buildS3Path(ImageryComponent.DEM, this.filePrefix, "report.pdf"), this.product, this.collection, monitor));
    }

    if (this.progressTask != null && truthy(this.progressTask.getProcessOrtho()))
    {
      this.runProcessor(unzippedParentFolder, "odm_orthophoto/odm_orthophoto.png", new ManagedDocument(buildS3Path(ImageryComponent.ORTHO, this.filePrefix, "odm_orthophoto.png"), this.product, this.collection, monitor));
      this.runProcessor(unzippedParentFolder, "odm_orthophoto/odm_orthophoto.tif", new ManagedDocument(buildS3Path(ImageryComponent.ORTHO, this.filePrefix, "odm_orthophoto" + CogTifProcessor.COG_EXTENSION), this.product, this.collection, monitor, new DocumentInfo().setOrthoCorrectionModel("unknown")));

      // Add the report to the ortho folder
      this.runProcessor(unzippedParentFolder, "odm_report/report.pdf", new ManagedDocument(buildS3Path(ImageryComponent.ORTHO, this.filePrefix, "report.pdf"), this.product, this.collection, monitor));
    }

    if (this.progressTask != null && truthy(this.progressTask.getProcessPtcloud()))
    {
      EpsgProcessor processor = new EpsgProcessor();

      this.runProcessor(unzippedParentFolder, "odm_georeferencing/odm_georeferencing_model_geo.txt", processor);
      this.runProcessor(unzippedParentFolder, "odm_georeferencing/odm_georeferenced_model.laz", new ManagedDocument(buildS3Path(ImageryComponent.PTCLOUD, this.filePrefix, "odm_georeferenced_model.laz"), this.product, this.collection, monitor, new DocumentInfo().setProjectionName(processor.getLine())));

      this.runProcessor(unzippedParentFolder, "entwine_pointcloud/ept.json", new S3FileUpload(buildS3Path(POTREE, this.filePrefix, "ept.json"), this.product, this.collection, monitor));
      this.runProcessor(unzippedParentFolder, "entwine_pointcloud/ept-build.json", new S3FileUpload(buildS3Path(POTREE, this.filePrefix, "ept-build.json"), this.product, this.collection, monitor));
      this.runProcessor(unzippedParentFolder, "entwine_pointcloud/ept-sources", new S3FileUpload(buildS3Path(POTREE, this.filePrefix, "ept-sources"), this.product, this.collection, monitor));
      this.runProcessor(unzippedParentFolder, "entwine_pointcloud/ept-hierarchy", new S3FileUpload(buildS3Path(POTREE, this.filePrefix, "ept-hierarchy"), this.product, this.collection, monitor));
      this.runProcessor(unzippedParentFolder, "entwine_pointcloud/ept-data", new S3FileUpload(buildS3Path(POTREE, this.filePrefix, "ept-data"), this.product, this.collection, monitor));

      // Add the report to the pt clould folder
      this.runProcessor(unzippedParentFolder, "odm_report/report.pdf", new ManagedDocument(buildS3Path(ImageryComponent.PTCLOUD, this.filePrefix, "report.pdf"), this.product, this.collection, monitor));
    }

  }

  public static String buildS3Path(String folder, String prefix, String filename)
  {
    String path = folder + "/";

    if (prefix != null && prefix.length() > 0)
    {
      path = path + prefix + "_";
    }

    path = path + filename;

    return path;
  }

  protected void runProcessor(CloseableFile unzippedParentFolder, String odmFilePath, Processor processor) throws InterruptedException
  {
    if (Thread.interrupted())
    {
      throw new InterruptedException();
    }

    File odmFile = new File(unzippedParentFolder, odmFilePath);

    if (!odmFile.exists())
    {
      this.progressTask.createAction("ODM did not produce an expected file [" + odmFilePath + "].", TaskActionType.ERROR.getType());
    }
    else
    {
      processor.process(new FileResource(odmFile));
    }
  }

  protected void uploadAllZip(CloseableFile allZip)
  {
    if (DevProperties.uploadAllZip())
    {
      new ManagedDocument(Product.ODM_ALL_DIR + "/" + allZip.getName(), this.product, this.collection, new WorkflowTaskMonitor((AbstractWorkflowTask) this.progressTask)).process(new FileResource(allZip));
    }
  }

  public void setAllZip(CloseableFile file)
  {
    this.allZip = file;
  }

  protected CloseableFile getAllZip()
  {
    if (this.allZip != null)
    {
      return this.allZip;
    }

    if (this.progressTask != null)
    {
      CloseableFile allZip;

      if (DevProperties.runOrtho())
      {
        if (DevProperties.isLocalODM())
        {
          allZip = ODMFacade.taskDownload(progressTask.getOdmUUID());
        }
        else
        {
          // https://github.com/OpenDroneMap/ClusterODM/issues/113
          allZip = RemoteFileFacade.download(progressTask.getOdmUUID() + "/all.zip").openNewFile();
        }
      }
      else
      {
        allZip = DevProperties.orthoResults();
      }

      return allZip;
    }
    else
    {
      return this.product.downloadAllZip().openNewFile();
    }
  }

  private boolean truthy(Boolean b)
  {
    return Boolean.TRUE.equals(b); // Prevents against throwing NPE if b is null
  }

}
