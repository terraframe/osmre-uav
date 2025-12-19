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
package gov.geoplatform.uasdm.remote.s3;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gov.geoplatform.uasdm.cog.TiTillerProxy.BBoxView;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.RemoteFileService;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

@Service
public class S3RemoteFileServiceNoOp implements RemoteFileService
{
  public static final String STAC_BUCKET = "-stac-";

  private Logger             logger      = LoggerFactory.getLogger(S3RemoteFileServiceNoOp.class);
  
  @Override
  public void destroy()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Long download(String key, File destination)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RemoteFileObject download(String key)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RemoteFileObject download(String key, String range)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void createFolder(String key)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void copyFolder(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteObject(String key)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteObject(String key, String bucket)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteObjects(String key)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteObjects(String key, String bucket)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String uploadFile(File file, String key, StatusMonitorIF monitor)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void uploadDirectory(File directory, String key, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void uploadDirectory(File directory, String key, String bucket, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean objectExists(String key)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Long calculateSize(UasComponentIF component)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void putStacItem(StacItem item)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeStacItem(ProductIF product)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public RemoteFileObject getStacItem(ProductIF product)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RemoteFileObject proxy(String url)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String presignUrl(String key, Duration duration)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BBoxView getBoundingBox(Product product, DocumentIF mappable)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
