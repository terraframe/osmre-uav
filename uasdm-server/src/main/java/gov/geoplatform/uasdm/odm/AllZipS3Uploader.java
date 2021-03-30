package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tika.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader.BasicODMFile;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.SolrService;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Can be used to download an allzip from S3 for a collection and selectively re-upload files
 * from that allzip to a different location in S3.
 * 
 * @author rrowlands
 */
public class AllZipS3Uploader
{
  private static final Logger logger = LoggerFactory.getLogger(AllZipS3Uploader.class);
  
  protected List<DocumentIF> documents = new LinkedList<DocumentIF>();
  
  protected ODMUploadTaskIF uploadTask;
  
  protected List<BasicODMFile> config;
  
  protected UasComponentIF component;
  
  protected String filePrefix;
  
  protected String s3Location;
  
  protected CloseableFile allZip;
  
  protected Product product;
  
  public AllZipS3Uploader(List<BasicODMFile> config, UasComponentIF component, ODMUploadTaskIF uploadTask, Product product)
  {
    this.config = config;
    this.component = component;
    this.s3Location = component.getS3location();
    this.uploadTask = uploadTask;
    this.product = product;
    
    if (config == null)
    {
      buildProcessingConfig();
    }
    initConfig();
  }
  
  public AllZipS3Uploader(List<BasicODMFile> config, UasComponentIF component, ODMUploadTaskIF uploadTask)
  {
    this.config = config;
    this.component = component;
    this.s3Location = component.getS3location();
    this.uploadTask = uploadTask;
    
    if (config == null)
    {
      buildProcessingConfig();
    }
    initConfig();
  }
  
  public AllZipS3Uploader(UasComponentIF component, ODMUploadTaskIF uploadTask)
  {
    this.component = component;
    this.s3Location = component.getS3location();
    this.uploadTask = uploadTask;
    
    buildProcessingConfig();
    initConfig();
  }
  
  public void initConfig()
  {
    for (BasicODMFile file : this.config)
    {
      file.setUploader(this);
    }
  }
  
  public void buildProcessingConfig()
  {
    List<BasicODMFile> processingConfigs = new ArrayList<BasicODMFile>();

    processingConfigs.add(new MandatoryErosFile("odm_dem", ImageryComponent.DEM, new String[] { "dsm.tif", "dtm.tif" }));

    processingConfigs.add(new MandatoryErosFile("odm_georeferencing", ImageryComponent.PTCLOUD, new String[] { "odm_georeferenced_model.laz" }));

    processingConfigs.add(new MandatoryErosFile("odm_orthophoto", ImageryComponent.ORTHO, new String[] { "odm_orthophoto.png", "odm_orthophoto.tif" }));

    processingConfigs.add(new MandatoryErosFile("micasense", "micasense", null));
    
    processingConfigs.add(new BasicODMFile("potree_pointcloud", "odm_all/potree", new String[]{"cloud.js"}, false));
    processingConfigs.add(new BasicODMFile("potree_pointcloud", "odm_all/potree", new String[]{"data"}, true));

    this.config = processingConfigs;
  }
  
  public ProductIF processAllZip() throws InterruptedException, SpecialException
  {
    final String folderName = "odm-" + FilenameUtils.getName(s3Location) + "-" + new Random().nextInt();
    
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
          throw new SpecialException("ODM did not return any results. (There was a problem unzipping ODM's results zip file)", e);
        }

        for (BasicODMFile config : this.config)
        {
          this.processConfig(config, unzippedParentFolder);
        }
      }
    }
    
    this.product = (Product) this.component.createProductIfNotExist();
    product.clear();

    product.addDocuments(this.getDocuments());
    
    List<String> list = new ArrayList<String>();
    if (this.uploadTask != null)
    {
      ODMProcessingTaskIF processingTask = this.uploadTask.getProcessingTask();
      list = processingTask.getFileList();
    }
    
    List<DocumentIF> raws = component.getDocuments().stream().filter(doc -> {
      return doc.getS3location().contains("/raw/");
    }).collect(Collectors.toList());

    for (DocumentIF raw : raws)
    {
      if (list.size() == 0 || list.contains(raw.getName()))
      {
        raw.addGeneratedProduct(product);
      }
    }
    
    return this.product;
  }
  
  public List<DocumentIF> getDocuments()
  {
    return documents;
  }
  
  protected void processConfig(BasicODMFile config, CloseableFile unzippedParentFolder) throws InterruptedException
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
    String allKey = this.component.getS3location() + "odm_all" + "/" + allZip.getName();

    if (DevProperties.uploadAllZip())
    {
      Util.uploadFileToS3(allZip, allKey, null);
      
      documents.add(component.createDocumentIfNotExist(allKey, allZip.getName()));
    }
  }
  
  protected void processDirectory(File parentDir, String s3FolderPrefix, BasicODMFile config, String filePrefix) throws InterruptedException
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

      if (UasComponentIF.isValidName(name) && config.shouldProcessFile(child))
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
  
  public static class BasicODMFile
  {
    protected AllZipS3Uploader  uploader;
    
    private String            odmFolderName;

    private String            s3FolderName;

    private String[]          mandatoryFiles;

    private ArrayList<String> processedFiles;
    
    private boolean           isDirectory;

    public BasicODMFile(String odmFolderName, String s3FolderName, String[] mandatoryFiles, boolean isDirectory)
    {
      this.odmFolderName = odmFolderName;
      this.s3FolderName = s3FolderName;
      this.mandatoryFiles = mandatoryFiles;
      this.processedFiles = new ArrayList<String>();
      this.isDirectory = isDirectory;
    }
    
    public AllZipS3Uploader getUploader()
    {
      return uploader;
    }

    public void setUploader(AllZipS3Uploader uploader)
    {
      this.uploader = uploader;
    }

    public boolean isDirectory()
    {
      return this.isDirectory;
    }

    protected boolean shouldProcessFile(File file)
    {
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
    }
  }
  
  public static class MandatoryErosFile extends BasicODMFile
  {
    public MandatoryErosFile(String odmFolderName, String s3FolderName, String[] mandatoryFiles)
    {
      super(odmFolderName, s3FolderName, mandatoryFiles, false);
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
        this.uploader.documents.add(this.uploader.component.createDocumentIfNotExist(key, file.getName()));
        
        SolrService.updateOrCreateDocument(this.uploader.component.getAncestors(), this.uploader.component, key, file.getName());
      }
    }
  }
  
  public static class SpecialException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public SpecialException(String string, ZipException e)
    {
      super(string, e);
    }
  }
  
}
