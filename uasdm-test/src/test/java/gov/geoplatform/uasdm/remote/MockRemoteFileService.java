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
package gov.geoplatform.uasdm.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class MockRemoteFileService implements RemoteFileService
{
  public static enum RemoteFileActionType
  {
    CREATE_FOLDER,
    COPY,
    DELETE,
    DELETE_FOLDER,
    DOWNLOAD,
    UPLOAD,
    UPLOAD_FOLDER
  }
  
  public static class RemoteFileAction
  {
    private String key;
    
    private String bucket;
    
    private RemoteFileActionType type;
    
    public RemoteFileAction(RemoteFileActionType type, String key, String bucket)
    {
      this.key = key;
      this.bucket = bucket;
      this.type = type;
    }

    public String getKey()
    {
      return key;
    }

    public void setKey(String key)
    {
      this.key = key;
    }

    public String getBucket()
    {
      return bucket;
    }

    public void setBucket(String bucket)
    {
      this.bucket = bucket;
    }

    public RemoteFileActionType getType()
    {
      return type;
    }

    public void setType(RemoteFileActionType type)
    {
      this.type = type;
    }
  }
  
  private Collection<RemoteFileAction> actions = new LinkedList<RemoteFileAction>();

  @Override
  public void download(String key, File destination) throws IOException, FileNotFoundException
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, key, AppProperties.getBucketName()));
  }
  
  @Override
  public RemoteFileObject proxy(String url)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, url, AppProperties.getBucketName()));

    return new MockRemoteFileObject();
  }

  @Override
  public RemoteFileObject download(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, key, AppProperties.getBucketName()));

    return new MockRemoteFileObject();
  }

  @Override
  public RemoteFileObject download(String key, List<Range> ranges)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, key, AppProperties.getBucketName()));

    return new MockRemoteFileObject();
  }

  @Override
  public void createFolder(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.CREATE_FOLDER, key, AppProperties.getBucketName()));
  }

  @Override
  public void deleteObject(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE, key, AppProperties.getBucketName()));
  }

  @Override
  public void deleteObjects(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE_FOLDER, key, AppProperties.getBucketName()));
  }

  @Override
  public int getItemCount(String key)
  {
    return 0;
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    return new SiteObjectsResultSet(0, 1L, 10L, new LinkedList<SiteObject>(), folder);
  }

  @Override
  public void uploadFile(File file, String key, StatusMonitorIF monitor)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD, key, AppProperties.getBucketName()));
  }

  @Override
  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD, key, AppProperties.getBucketName()));
  }

  public Collection<RemoteFileAction> getActions()
  {
    return actions;
  }

  @Override
  public void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.COPY, sourceKey, AppProperties.getBucketName()));
  }

  @Override
  public void deleteObject(String key, String bucket)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE, key, AppProperties.getBucketName()));
  }

  @Override
  public void deleteObjects(String key, String bucket)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE_FOLDER, key, AppProperties.getBucketName()));
  }

  @Override
  public void uploadDirectory(File directory, String key, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD_FOLDER, key, AppProperties.getBucketName()));
  }
  
  @Override
  public void uploadDirectory(File directory, String key, String bucket, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD_FOLDER, key, bucket));
  }

  @Override
  public boolean objectExists(String key)
  {
    return false;
  }

  @Override
  public Long calculateSize(UasComponentIF component)
  {
    return null;
  }

  @Override
  public void putStacItem(StacItem item)
  {
    
  }

  @Override
  public void removeStacItem(ProductIF product)
  {
    
  }

  @Override
  public RemoteFileObject getStacItem(ProductIF product)
  {
    return null;
  }
}
