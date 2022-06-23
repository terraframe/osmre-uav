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
import java.util.List;

import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.s3.S3RemoteFileService;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class RemoteFileFacade
{
  private static RemoteFileService service = new S3RemoteFileService();

  public static synchronized void setService(RemoteFileService service)
  {
    RemoteFileFacade.service = service;
  }

  public static void download(String key, File destination) throws FileNotFoundException, IOException
  {
    service.download(key, destination);
  }

  public static RemoteFileObject download(String key)
  {
    return service.download(key);
  }

  public static RemoteFileObject download(String key, List<Range> ranges)
  {
    return service.download(key, ranges);
  }

  public static void createFolder(String key)
  {
    service.createFolder(key);
  }
  
  public static void copyObject(String sourceKey, String sourceBucket, String destKey, String destBucket)
  {
    service.copyObject(sourceKey, sourceBucket, destKey, destBucket);
  }

  public static void deleteObject(String key)
  {
    service.deleteObject(key);
  }
  
  public static void deleteObject(String key, String bucket)
  {
    service.deleteObject(key, bucket);
  }
  
  public static void deleteObjects(String key)
  {
    service.deleteObjects(key);
  }

  public static void deleteObjects(String key, String bucket)
  {
    service.deleteObjects(key, bucket);
  }

  public static int getItemCount(String key)
  {
    return service.getItemCount(key);
  }

  public static SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    return service.getSiteObjects(component, folder, objects, pageNumber, pageSize);
  }

  public static void uploadFile(File child, String key, AbstractWorkflowTaskIF task)
  {
    service.uploadFile(child, key, task);
  }

  public static void uploadFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    service.putFile(key, metadata, stream);
  }

  public static Long calculateSize(UasComponentIF component)
  {
    return service.calculateSize(component);
  }

  public static void uploadDirectory(File directory, String key, AbstractWorkflowTaskIF task, boolean includeSubDirectories)
  {
    service.uploadDirectory(directory, key, task, includeSubDirectories);
  }

  public static boolean objectExists(String key)
  {
    return service.objectExists(key);
  }

}
