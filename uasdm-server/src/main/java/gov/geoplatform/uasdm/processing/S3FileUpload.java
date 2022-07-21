package gov.geoplatform.uasdm.processing;

import java.io.File;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class S3FileUpload implements Processor
{
  protected String s3Path;
  
  protected StatusMonitorIF monitor;
  
  protected CollectionIF collection;
  
  protected Product product;
  
  /**
   * 
   * @param s3Path The s3 target upload path, relative to the collection.
   * @param collection
   * @param isDirectory
   * @param monitor
   */
  public S3FileUpload(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    this.s3Path = s3Path;
    this.monitor = monitor;
    this.product = product;
    this.collection = collection;
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
    
    this.uploadFile(file);

    CollectionReport.updateSize(this.collection);
    
    return true;
  }
  
  protected String getS3Key()
  {
    if (this.s3Path.contains(this.collection.getS3location()))
    {
      return this.s3Path;
    }
    
    String key = this.collection.getS3location() + this.s3Path;
    
    return key;
  }
  
  protected void uploadFile(File file)
  {
    String key = this.getS3Key();
    
    if (file.isDirectory())
    {
      RemoteFileFacade.uploadDirectory(file, key, this.monitor, true);
      
      if (this.product.isPublished())
      {
        // TODO : copyObject is more efficient but doesn't work on directories
        RemoteFileFacade.uploadDirectory(file, key, AppProperties.getPublicBucketName(), this.monitor, true);
      }
    }
    else
    {
      RemoteFileFacade.uploadFile(file, key, this.monitor);
      
      if (this.product.isPublished())
      {
        RemoteFileFacade.copyObject(key, AppProperties.getBucketName(), key, AppProperties.getPublicBucketName());
      }
    }
  }
}