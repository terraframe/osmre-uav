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
import java.net.URL;
import java.util.Date;
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
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public interface RemoteFileService
{

  public void download(String key, File destination) throws IOException, FileNotFoundException;

  public RemoteFileObject download(String key);

  public RemoteFileObject download(String key, List<Range> ranges);

  public void createFolder(String key);

  public void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket);

  public void copyFolder(String sourceKey, String sourceBucket, String destKey, String destBucket);

  public void deleteObject(String key);

  public void deleteObject(String key, String bucket);

  public void deleteObjects(String key);

  public void deleteObjects(String key, String bucket);

  public int getItemCount(String key);

  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize);

  public void uploadFile(File file, String key, StatusMonitorIF monitor);

  public void uploadDirectory(File directory, String key, StatusMonitorIF monitor, boolean includeSubDirectories);

  public void uploadDirectory(File directory, String key, String bucket, StatusMonitorIF monitor, boolean includeSubDirectories);

  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream);

  public boolean objectExists(String key);

  public Long calculateSize(UasComponentIF component);

  public void putStacItem(StacItem item);

  public void removeStacItem(ProductIF product);

  public RemoteFileObject getStacItem(ProductIF product);

  public RemoteFileObject proxy(String url);

  public URL presignUrl(String key, Date expiration, HttpMethod httpMethod);

  public BBoxView getBoundingBox(Product product, DocumentIF mappable);

}
