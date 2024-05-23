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
package gov.geoplatform.uasdm.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.HttpMethod;

import gov.geoplatform.uasdm.cog.TiTillerProxy.BBoxView;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.RemoteFileService;
import gov.geoplatform.uasdm.util.FileTestUtils;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class MockRemoteFileService implements RemoteFileService
{
  public static enum RemoteFileActionType {
    CREATE_FOLDER, COPY, DELETE, DELETE_FOLDER, DOWNLOAD, PROXY, UPLOAD, UPLOAD_FOLDER, SIZE, EXISTS, COUNT, GET_ITEMS, PUT_STAC_ITEM, REMOVE_STAC_ITEM, GET_STAC_ITEM
  }

  public static class RemoteFileAction
  {
    private RemoteFileActionType type;

    private Object[] parameters;

    public RemoteFileAction(RemoteFileActionType type, Object... parameters)
    {
      this.type = type;
      this.parameters = parameters;
    }

    public Object[] getParameters()
    {
      return parameters;
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

  private List<RemoteFileAction> actions = new LinkedList<RemoteFileAction>();

  @Override
  public void download(String key, File destination) throws IOException, FileNotFoundException
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, key, destination));
  }

  @Override
  public RemoteFileObject proxy(String url)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.PROXY, url));

    return getMockFile(url);
  }

  @Override
  public RemoteFileObject download(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, key));

    return getMockFile(key);
  }

  @Override
  public RemoteFileObject download(String key, List<Range> ranges)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DOWNLOAD, key, ranges));

    return this.getMockFile(key);
  }

  @Override
  public void createFolder(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.CREATE_FOLDER, key));
  }

  @Override
  public void deleteObject(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE, key));
  }

  @Override
  public void deleteObjects(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE_FOLDER, key));
  }

  @Override
  public int getItemCount(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.COUNT, key));
    
    return 0;
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.GET_ITEMS, component, folder, objects, pageNumber, pageSize));
    
    if (folder.contains("odm_all"))
    {
      SiteObject object = new SiteObject();
      object.setComponentId(component.getOid());
      object.setKey(component.getS3location() + "odm_all/all123.zip");
      object.setLastModified(new Date());

      return new SiteObjectsResultSet(1, 1L, 10L, Arrays.asList(object), folder);
    }

    return new SiteObjectsResultSet(0, 1L, 10L, new LinkedList<SiteObject>(), folder);
  }

  @Override
  public void uploadFile(File file, String key, StatusMonitorIF monitor)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD, file, key, monitor));
  }

  @Override
  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD, key, metadata, stream));
  }

  public List<RemoteFileAction> getActions()
  {
    return actions;
  }

  @Override
  public void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.COPY, sourceKey, sourceBucket, destKey, destBucket));
  }

  @Override
  public void deleteObject(String key, String bucket)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE, key, bucket));
  }

  @Override
  public void deleteObjects(String key, String bucket)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.DELETE_FOLDER, key, bucket));
  }

  @Override
  public void uploadDirectory(File directory, String key, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD_FOLDER, directory, key, monitor, includeSubDirectories));
  }

  @Override
  public void uploadDirectory(File directory, String key, String bucket, StatusMonitorIF monitor, boolean includeSubDirectories)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.UPLOAD_FOLDER, directory, key, bucket, monitor, includeSubDirectories));
  }

  @Override
  public boolean objectExists(String key)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.EXISTS, key));

    return false;
  }

  @Override
  public Long calculateSize(UasComponentIF component)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.SIZE, component));

    return 1L;
  }

  @Override
  public void putStacItem(StacItem item)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.PUT_STAC_ITEM, item));
  }

  @Override
  public void removeStacItem(ProductIF product)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.REMOVE_STAC_ITEM, product));
  }

  @Override
  public RemoteFileObject getStacItem(ProductIF product)
  {
    this.actions.add(new RemoteFileAction(RemoteFileActionType.GET_STAC_ITEM, product));

    try
    {
      return new MockRemoteFileObject(new File(this.getClass().getResource("/stac_item.json").toURI()));
    }
    catch (URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BBoxView getBoundingBox(Product product, DocumentIF mappable)
  {
    return new BBoxView(-180, -90, 180, 90);
  }

  private RemoteFileObject getMockFile(String key)
  {
    try
    {
      if (key.endsWith("metadata.xml"))
      {
        return new MockRemoteFileObject(new File(this.getClass().getResource("/metadata.xml").toURI()));
      }
      else if (key.contains("odm_all"))
      {
        return new MockRemoteFileObject(FileTestUtils.createZip(this.getClass().getResource("/all").toURI(), "all.zip"));
      }
    }
    catch (URISyntaxException | IOException e)
    {
      throw new RuntimeException(e);
    }

    return new MockRemoteFileObject();
  }
  
  @Override
  public URL presignUrl(String key, Date expiration, HttpMethod httpMethod)
  {
    return null;
  }
}
