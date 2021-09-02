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
package gov.geoplatform.uasdm.odm;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.geoserver.GeoserverPublisher;
import gov.geoplatform.uasdm.geoserver.ImageMosaicPublisher;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.SolrService;
import net.geoprism.gis.geoserver.GeoserverProperties;
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
  private static final Logger  logger    = LoggerFactory.getLogger(ODMZipPostProcessor.class);

  public static final String   DEM_GDAL  = Product.ODM_ALL_DIR + "/gdal";

  public static final String   POTREE    = Product.ODM_ALL_DIR + "/entwine_pointcloud";

  protected List<DocumentIF>   documents = new LinkedList<DocumentIF>();

  protected ODMUploadTaskIF    uploadTask;

  protected List<S3FileUpload> config;

  protected UasComponentIF     collection;

  protected String             filePrefix;

  protected String             s3Location;

  protected CloseableFile      allZip;

  protected Product            product;

  // public ODMZipPostProcessor(List<S3FileUpload> config, UasComponentIF
  // component, ODMUploadTaskIF uploadTask, Product product)
  // {
  // this.config = config;
  // this.component = component;
  // this.s3Location = component.getS3location();
  // this.uploadTask = uploadTask;
  // this.product = product;
  //
  // if (config == null)
  // {
  // buildProcessingConfig();
  // }
  // initConfig();
  // }

  // public ODMZipPostProcessor(List<S3FileUpload> config, UasComponentIF
  // component, ODMUploadTaskIF uploadTask)
  // {
  // this.config = config;
  // this.component = component;
  // this.s3Location = component.getS3location();
  // this.uploadTask = uploadTask;
  //
  // if (config == null)
  // {
  // buildProcessingConfig();
  // }
  // initConfig();
  // }

  public ODMZipPostProcessor(UasComponentIF collection, ODMUploadTaskIF uploadTask, Product product)
  {
    this.product = product;
    this.collection = collection;
    this.s3Location = collection.getS3location();
    this.uploadTask = uploadTask;

    buildProcessingConfig();
    initConfig();
  }

  public void initConfig()
  {
    for (S3FileUpload file : this.config)
    {
      file.setUploader(this);
    }
  }

  public void buildProcessingConfig()
  {
    List<S3FileUpload> processingConfigs = new ArrayList<S3FileUpload>();

    processingConfigs.add(new ManagedDocument("odm_dem", ImageryComponent.DEM, new String[] { "dsm.tif", "dtm.tif" }));

    processingConfigs.add(new DemGdalProcessor("odm_dem", DEM_GDAL, new String[] { "dsm.tif" }));

    processingConfigs.add(new ManagedDocument("odm_georeferencing", ImageryComponent.PTCLOUD, new String[] { "odm_georeferenced_model.laz" }));

    processingConfigs.add(new ManagedDocument("odm_orthophoto", ImageryComponent.ORTHO, new String[] { "odm_orthophoto.png", "odm_orthophoto.tif" }));

    processingConfigs.add(new ManagedDocument("micasense", "micasense", null));

    processingConfigs.add(new S3FileUpload("entwine_pointcloud", POTREE, new String[] { "ept.json", "ept-build.json" }, false));
    processingConfigs.add(new S3FileUpload("entwine_pointcloud", POTREE, new String[] { "ept-sources", "ept-hierarchy", "ept-data" }, true));

    this.config = processingConfigs;
  }

  public ProductIF processAllZip() throws InterruptedException
  {
    final String folderName = "odm-" + FilenameUtils.getName(s3Location) + "-" + new Random().nextInt();

    this.product = (Product) this.collection.createProductIfNotExist();
    product.clear();

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

        for (S3FileUpload config : this.config)
        {
          this.processConfig(config, unzippedParentFolder);
        }
      }
    }

    product.addDocuments(this.getDocuments());

    List<String> list = new ArrayList<String>();
    if (this.uploadTask != null)
    {
      ODMProcessingTaskIF processingTask = this.uploadTask.getProcessingTask();
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

    product.createImageService(true);

    product.updateBoundingBox();

    return this.product;
  }

  public List<DocumentIF> getDocuments()
  {
    return documents;
  }

  protected void processConfig(S3FileUpload config, CloseableFile unzippedParentFolder) throws InterruptedException
  {
    if (Thread.interrupted())
    {
      throw new InterruptedException();
    }

    File parentDir = new File(unzippedParentFolder, config.odmFolderName);

    if (parentDir.exists())
    {
      processDirectory(parentDir, config.s3FolderName, config, filePrefix);
    }

    config.handleUnprocessedFiles();
  }

  protected void uploadAllZip(CloseableFile allZip)
  {
    String allKey = this.collection.getS3location() + "odm_all" + "/" + allZip.getName();

    if (DevProperties.uploadAllZip())
    {
      Util.uploadFileToS3(allZip, allKey, null);

      documents.add(this.collection.createDocumentIfNotExist(allKey, allZip.getName()));

      if (this.collection instanceof CollectionIF)
      {
        Long calculateSize = RemoteFileFacade.calculateSize(this.collection);

        CollectionReport.update((CollectionIF) this.collection, calculateSize);
      }
    }
  }

  protected void processDirectory(File parentDir, String s3FolderPrefix, S3FileUpload config, String filePrefix) throws InterruptedException
  {
    File[] children = parentDir.listFiles();

    if (children == null)
    {
      logger.error("Problem occurred while listing files of directory [" + parentDir.getAbsolutePath() + "].");
      return;
    }

    for (File child : children)
    {
      if (Thread.interrupted())
      {
        throw new InterruptedException();
      }

      String name = child.getName();

      if (filePrefix != null && filePrefix.length() > 0)
      {
        name = filePrefix + "_" + name;
      }

      if (config.shouldProcessFile(child))
      {
        String key = this.s3Location + s3FolderPrefix + "/" + name;

        config.processFile(child, key);
      }
      else if (child.isDirectory())
      {
        processDirectory(child, s3FolderPrefix + "/" + child.getName(), config, filePrefix);
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

    if (this.uploadTask != null)
    {
      CloseableFile allZip;

      if (DevProperties.runOrtho())
      {
        allZip = ODMFacade.taskDownload(uploadTask.getOdmUUID());
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

  public static class S3FileUpload
  {
    protected ODMZipPostProcessor uploader;

    private String                odmFolderName;

    private String                s3FolderName;

    private String[]              mandatoryFiles;

    private ArrayList<String>     processedFiles;

    private boolean               isDirectory;

    public S3FileUpload(String odmFolderName, String s3FolderName, String[] mandatoryFiles, boolean isDirectory)
    {
      this.odmFolderName = odmFolderName;
      this.s3FolderName = s3FolderName;
      this.mandatoryFiles = mandatoryFiles;
      this.processedFiles = new ArrayList<String>();
      this.isDirectory = isDirectory;
    }

    public ODMZipPostProcessor getUploader()
    {
      return uploader;
    }

    public void setUploader(ODMZipPostProcessor uploader)
    {
      this.uploader = uploader;
    }

    public boolean isDirectory()
    {
      return this.isDirectory;
    }

    protected boolean shouldProcessFile(File file)
    {
      String name = file.getName();
      if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
      {
        return false;
      }

      if (this.isDirectory != file.isDirectory())
      {
        return false;
      }

      if (this.mandatoryFiles == null)
      {
        return true;
      }

      if (ArrayUtils.contains(mandatoryFiles, file.getName()) && DevProperties.shouldUploadProduct(file.getName()))
      {
        processedFiles.add(file.getName());
        return true;
      }

      return false;
    }

    protected void handleUnprocessedFiles()
    {
      List<String> unprocessed = this.getUnprocessedFiles();

      if (unprocessed.size() > 0)
      {
        for (String name : unprocessed)
        {
          this.handleUnprocessedFile(name);
        }
      }
    }

    protected void handleUnprocessedFile(String name)
    {

    }

    protected List<String> getUnprocessedFiles()
    {
      ArrayList<String> unprocessed = new ArrayList<String>();

      if (mandatoryFiles == null)
      {
        return unprocessed;
      }

      for (String file : mandatoryFiles)
      {
        if (!processedFiles.contains(file))
        {
          unprocessed.add(file);
        }
      }

      return unprocessed;
    }

    public String getOdmFolderName()
    {
      return odmFolderName;
    }

    public void setOdmFolderName(String odmFolderName)
    {
      this.odmFolderName = odmFolderName;
    }

    public String getS3FolderName()
    {
      return s3FolderName;
    }

    public void setS3FolderName(String s3FolderName)
    {
      this.s3FolderName = s3FolderName;
    }

    public String[] getMandatoryFiles()
    {
      return mandatoryFiles;
    }

    public void setMandatoryFiles(String[] mandatoryFiles)
    {
      this.mandatoryFiles = mandatoryFiles;
    }

    public ArrayList<String> getProcessedFiles()
    {
      return processedFiles;
    }

    public void setProcessedFiles(ArrayList<String> processedFiles)
    {
      this.processedFiles = processedFiles;
    }

    protected void processFile(File file, String key)
    {
      if (file.isDirectory())
      {
        RemoteFileFacade.uploadDirectory(file, key, this.uploader.uploadTask, true);
      }
      else
      {
        RemoteFileFacade.uploadFile(file, key, this.uploader.uploadTask);
      }

      if (this.uploader.collection instanceof CollectionIF)
      {
        Long calculateSize = RemoteFileFacade.calculateSize(this.uploader.collection);

        CollectionReport.update((CollectionIF) this.uploader.collection, calculateSize);
      }
    }
  }

  public static class ManagedDocument extends S3FileUpload
  {
    private boolean searchable;

    public ManagedDocument(String odmFolderName, String s3FolderName, String[] mandatoryFiles)
    {
      this(odmFolderName, s3FolderName, mandatoryFiles, true);
    }

    public ManagedDocument(String odmFolderName, String s3FolderName, String[] mandatoryFiles, boolean searchable)
    {
      super(odmFolderName, s3FolderName, mandatoryFiles, false);

      this.searchable = searchable;
    }

    @Override
    protected void handleUnprocessedFile(String name)
    {
      if (this.uploader.uploadTask != null)
      {
        this.uploader.uploadTask.createAction("ODM did not produce an expected file [" + this.getS3FolderName() + "/" + name + "].", "error");
      }
    }

    @Override
    protected void processFile(File file, String key)
    {
      super.processFile(file, key);

      if (!file.isDirectory())
      {
        this.uploader.documents.add(this.uploader.collection.createDocumentIfNotExist(key, file.getName()));

        if (searchable)
        {
          SolrService.updateOrCreateDocument(this.uploader.collection.getAncestors(), this.uploader.collection, key, file.getName());
        }
      }
    }
  }

  public static class DemGdalProcessor extends ManagedDocument
  {
    public DemGdalProcessor(String odmFolderName, String s3FolderName, String[] mandatoryFiles)
    {
      super(odmFolderName, s3FolderName, mandatoryFiles, false);
    }

    @Override
    protected void processFile(File file, String key)
    {
      final String basename = FilenameUtils.getBaseName(file.getName());

      File hillshade = new File(file.getParent(), basename + "-gdal.tif");

      this.executeProcess(new String[] { "gdaldem", "hillshade", file.getAbsolutePath(), hillshade.getAbsolutePath() });

      if (hillshade.exists())
      {
        super.processFile(hillshade, key);
      }
    }

    private void executeProcess(String[] commands)
    {
      final Runtime rt = Runtime.getRuntime();

      StringBuilder stdOut = new StringBuilder();
      StringBuilder stdErr = new StringBuilder();

      Thread t = new Thread()
      {
        public void run()
        {
          try
          {
            Process proc = rt.exec(commands);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // read the output from the command
            String s = null;
            while ( ( s = stdInput.readLine() ) != null)
            {
              stdOut.append(s + "\n");
            }

            // read any errors from the attempted command
            while ( ( s = stdError.readLine() ) != null)
            {
              stdErr.append(s + "\n");
            }
          }
          catch (Throwable t)
          {
            logger.error("Error occured while processing dem file with gdal.", t);
          }
        }
      };
      t.start();

      try
      {
        t.join(10000);
      }
      catch (InterruptedException e)
      {
        logger.error("Interrupted when processing dem file with gdal", e);
      }

      if (stdOut.toString().trim().length() > 0)
      {
        logger.info("Processed hillshade with gdal [" + stdOut.toString() + "].");
      }

      if (stdErr.toString().trim().length() > 0)
      {
        logger.error("Unexpected error while processing gdal hillshade [" + stdErr.toString() + "].");
      }
    }
  }

}
