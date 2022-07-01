package gov.geoplatform.uasdm.processing;

import java.io.File;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class S3FileUpload implements Processor
{
  protected String filename;
  
  protected String s3FolderName;

  protected boolean isDirectory;
  
  protected AbstractWorkflowTask progressTask;
  
  protected CollectionIF collection;
  
  protected String prefix;

  public S3FileUpload(String filename, AbstractWorkflowTask progressTask, CollectionIF collection, String s3FolderName, String prefix, boolean isDirectory)
  {
    this.filename = filename;
    this.progressTask = progressTask;
    this.collection = collection;
    this.s3FolderName = s3FolderName;
    this.isDirectory = isDirectory;
    this.prefix = prefix;
  }

  public boolean isDirectory()
  {
    return this.isDirectory;
  }

  public String getS3FolderName()
  {
    return s3FolderName;
  }

  public void setS3FolderName(String s3FolderName)
  {
    this.s3FolderName = s3FolderName;
  }
  
  @Override
  public void process(File file)
  {
    if (file.exists())
    {
      this.processFile(file);
    }
    else
    {
      this.handleUnprocessed();
    }
  }
  
  protected String getS3Key(File file)
  {
    String name = file.getName();
    
    if (prefix != null && prefix.length() > 0)
    {
      name = prefix + "_" + name;
    }
    
    String s3Folder = (this.getS3FolderName() == null) ? "" : this.getS3FolderName();
    
    String key = this.collection.getS3location() + s3Folder + "/" + name;
    
    return key;
  }

  public void processFile(File file)
  {
    String key = this.getS3Key(file);
    
    if (file.isDirectory())
    {
      RemoteFileFacade.uploadDirectory(file, key, this.progressTask, true);
    }
    else
    {
      RemoteFileFacade.uploadFile(file, key, this.progressTask);
    }

    CollectionReport.updateSize((CollectionIF) this.collection);
  }

  @Override
  public String getFileName()
  {
    return this.filename;
  }

  @Override
  public void handleUnprocessed()
  {
    
  }
}