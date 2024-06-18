/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.processing;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
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
  public boolean process(ApplicationFileResource res)
  {
    if (!res.exists())
    {
      this.monitor.addError("S3 uploader expected file [" + res.getAbsolutePath() + "] to exist.");
      return false;
    }
    
    this.uploadFile(res);

    CollectionReportFacade.updateSize(this.collection).doIt();
    
    return true;
  }
  
  protected String getS3Key()
  {
    if (this.s3Path.startsWith(this.collection.getS3location()))
    {
      return this.s3Path;
    }
    
    return this.collection.getS3location(product, s3Path);
  }
  
  protected void uploadFile(ApplicationFileResource res)
  {
    String key = this.getS3Key();
    
    if (res.isDirectory())
    {
      RemoteFileFacade.uploadDirectory(res.getUnderlyingFile(), key, this.monitor, true);
      
      if (this.product.isPublished())
      {
        // TODO : copyObject is more efficient but doesn't work on directories
        RemoteFileFacade.uploadDirectory(res.getUnderlyingFile(), key, AppProperties.getPublicBucketName(), this.monitor, true);
      }
    }
    else
    {
      RemoteFileFacade.uploadFile(res.getUnderlyingFile(), key, this.monitor);
      
      if (this.product.isPublished())
      {
        RemoteFileFacade.copyObject(key, AppProperties.getBucketName(), key, AppProperties.getPublicBucketName());
      }
    }
  }
}