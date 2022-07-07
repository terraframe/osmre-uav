package gov.geoplatform.uasdm.processing;

import java.io.File;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class S3FileUpload implements Processor
{
  protected String s3Path;
  
  protected boolean isDirectory;
  
  protected StatusMonitorIF monitor;
  
  protected CollectionIF collection;
  
  /**
   * 
   * @param s3Path The s3 target upload path, relative to the collection.
   * @param collection
   * @param isDirectory
   * @param monitor
   */
  public S3FileUpload(String s3Path, CollectionIF collection, StatusMonitorIF monitor, boolean isDirectory)
  {
    this.s3Path = s3Path;
    this.monitor = monitor;
    this.collection = collection;
    this.isDirectory = isDirectory;
  }

  public boolean isDirectory()
  {
    return this.isDirectory;
  }

  public String getS3Path()
  {
    return s3Path;
  }
  
  public void setS3Path(String s3Path)
  {
    this.s3Path = s3Path;
  }
  
  @Override
  public boolean process(File file)
  {
    if (!file.exists())
    {
      this.monitor.addError("S3 uploader expected file [" + file.getAbsolutePath() + "] to exist.");
      return false;
    }
    
    String key = this.getS3Key(file);
    
    if (file.isDirectory())
    {
      RemoteFileFacade.uploadDirectory(file, key, this.monitor, true);
    }
    else
    {
      RemoteFileFacade.uploadFile(file, key, this.monitor);
    }

    CollectionReport.updateSize((CollectionIF) this.collection);
    
    return true;
  }
  
  protected String getS3Key(File file)
  {
    String key = this.collection.getS3location() + this.s3Path;
    
    return key;
  }
}