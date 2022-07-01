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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.odm.ODMFacade;
import gov.geoplatform.uasdm.odm.ODMProcessingTaskIF;
import gov.geoplatform.uasdm.odm.ODMUploadTaskIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Can be used to download an allzip from S3 for a collection and selectively
 * re-upload files from that allzip to a different location in S3.
 * 
 * @author rrowlands
 */
public class ODMZipPostProcessor
{
  private static final Logger logger = LoggerFactory.getLogger(ODMZipPostProcessor.class);

  public static final String DEM_GDAL = Product.ODM_ALL_DIR + "/gdal";

  public static final String POTREE = Product.ODM_ALL_DIR + "/entwine_pointcloud";

  protected ODMUploadTaskIF progressTask;

  protected CollectionIF collection;

  protected String filePrefix;

  protected CloseableFile allZip;

  protected Product product;

  public ODMZipPostProcessor(CollectionIF collection, ODMUploadTaskIF progressTask, Product product)
  {
    this.product = product;
    this.collection = collection;
    this.progressTask = progressTask;
  }

  public ProductIF processAllZip() throws InterruptedException
  {
    final String folderName = "odm-" + FilenameUtils.getName(this.collection.getS3location()) + "-" + new Random().nextInt();

    try (CloseableFile unzippedParentFolder = new CloseableFile(FileUtils.getTempDirectory(), folderName))
    {
      try (CloseableFile allZip = getAllZip())
      {
        try
        {
          new ZipFile(allZip).extractAll(unzippedParentFolder.getAbsolutePath());
        }
        catch (ZipException e)
        {
          throw new RuntimeException("ODM did not return any results. (There was a problem unzipping ODM's results zip file)", e);
        }
        
        this.cleanExistingProduct();

        this.product = (Product) this.collection.createProductIfNotExist();
        
        this.processProduct(product, (AbstractWorkflowTask) this.progressTask, unzippedParentFolder);
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

    // If the product is published, copy tifs from the private bucket to the public bucket
    if (product.getPublished())
    {
      for (DocumentIF mappable : product.getMappableDocuments())
      {
        RemoteFileFacade.copyObject(mappable.getS3location(), AppProperties.getBucketName(), mappable.getS3location(), AppProperties.getPublicBucketName());
      }
    }

    product.updateBoundingBox();

    return this.product;
  }
  
  /**
   * This must be done before creating the product because the 'removeArtifact' method will delete any existing products
   */
  protected void cleanExistingProduct()
  {
    if (this.progressTask.getProcessDem())
    {
      this.collection.removeArtifacts(ImageryComponent.DEM);
    }

    if (this.progressTask.getProcessOrtho())
    {
      this.collection.removeArtifacts(ImageryComponent.ORTHO);
    }
    
    if (this.progressTask.getProcessPtcloud())
    {
      this.collection.removeArtifacts(ImageryComponent.PTCLOUD);
    }
  }
  
  protected void processProduct(Product product, AbstractWorkflowTask task, CloseableFile unzippedParentFolder) throws InterruptedException
  {
    if (this.progressTask.getProcessDem())
    {
      this.runProcessor(unzippedParentFolder, "odm_dem",
          new ManagedDocument("dsm.tif", task, this.product, this.collection, ImageryComponent.DEM),
          new ManagedDocument("dtm.tif", task, this.product, this.collection, ImageryComponent.DEM)
      );

      this.runProcessor(unzippedParentFolder, "odm_dem", new GdalDemProcessor("dsm.tif", task, this.product, this.collection, DEM_GDAL));
    }

    if (this.progressTask.getProcessOrtho())
    {
      this.runProcessor(unzippedParentFolder, "odm_orthophoto",
          new ManagedDocument("odm_orthophoto.png", task, this.product, this.collection, ImageryComponent.ORTHO),
          new ManagedDocument("odm_orthophoto.tif", task, this.product, this.collection, ImageryComponent.ORTHO)
      );
    }
    
    if (this.progressTask.getProcessPtcloud())
    {
      this.runProcessor(unzippedParentFolder, "odm_georeferencing", new ManagedDocument("odm_georeferenced_model.laz", task, this.product, this.collection, ImageryComponent.PTCLOUD));

      this.runProcessor(unzippedParentFolder, "micasense", new ManagedDocument("micasense", task, this.product, this.collection, null));

      this.runProcessor(unzippedParentFolder, "entwine_pointcloud",
          new S3FileUpload("ept.json", task, this.collection, POTREE, false),
          new S3FileUpload("ept-build.json", task, this.collection, POTREE, false),
          new S3FileUpload("ept-sources", task, this.collection, POTREE, true),
          new S3FileUpload("ept-hierarchy", task, this.collection, POTREE, true),
          new S3FileUpload("ept-data", task, this.collection, POTREE, true)
      );
    }
  }

  protected void runProcessor(CloseableFile unzippedParentFolder, String odmFolderName, Processor... processors) throws InterruptedException
  {
    if (Thread.interrupted())
    {
      throw new InterruptedException();
    }

    File parentDir = new File(unzippedParentFolder, odmFolderName);

    if (parentDir.exists())
    {
      for (Processor processor : processors)
      {
        File file = new File(parentDir, processor.getFileName());
  
        if (Thread.interrupted())
        {
          throw new InterruptedException();
        }

        processor.process(file, this.filePrefix);
      }
    }
  }

  protected void uploadAllZip(CloseableFile allZip)
  {
    if (DevProperties.uploadAllZip())
    {
      String allKey = this.collection.getS3location() + "odm_all" + "/" + allZip.getName();

      Util.uploadFileToS3(allZip, allKey, null);

      this.collection.createDocumentIfNotExist(allKey, allZip.getName(), null, "ODM");

      if (this.collection instanceof CollectionIF)
      {
        CollectionReport.updateSize((CollectionIF) this.collection);
      }
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
        allZip = ODMFacade.taskDownload(progressTask.getOdmUUID());
      }
      else
      {
        allZip = DevProperties.orthoResults();
      }

      uploadAllZip(allZip);

      return allZip;
    }
    else
    {
      return this.product.downloadAllZip().openNewFile();
    }
  }

}
