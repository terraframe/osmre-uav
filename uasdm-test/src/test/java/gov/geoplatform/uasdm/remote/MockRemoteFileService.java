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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class MockRemoteFileService implements RemoteFileService
{
  private Set<String> downloads = new TreeSet<String>();

  private Set<String> deletes   = new TreeSet<String>();

  private Set<String> uploads   = new TreeSet<String>();

  private Set<String> creates   = new TreeSet<String>();

  @Override
  public void download(String key, File destination) throws IOException, FileNotFoundException
  {
    this.downloads.add(key);
  }

  @Override
  public RemoteFileObject download(String key)
  {
    this.downloads.add(key);

    return new MockRemoteFileObject();
  }

  @Override
  public RemoteFileObject download(String key, List<Range> ranges)
  {
    this.downloads.add(key);

    return new MockRemoteFileObject();
  }

  @Override
  public void createFolder(String key)
  {
    this.creates.add(key);
  }

  @Override
  public void deleteObject(String key)
  {
    this.deletes.add(key);
  }

  @Override
  public void deleteObjects(String key)
  {
    this.deletes.add(key);
  }

  @Override
  public int getItemCount(String key)
  {
    return 0;
  }
  
  @Override
  public void uploadFile(File file, String key, AbstractWorkflowTaskIF task)
  {
    this.uploads.add(key);
  }

  @Override
  public void putFile(String key, RemoteFileMetadata metadata, InputStream stream)
  {
    this.uploads.add(key);
  }

  public Set<String> getDownloads()
  {
    return downloads;
  }

  public Set<String> getDeletes()
  {
    return deletes;
  }

  public Set<String> getUploads()
  {
    return uploads;
  }

  public Set<String> getCreates()
  {
    return creates;
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void uploadDirectory(File directory, String key, AbstractWorkflowTaskIF task, boolean includeSubDirectories)
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
}
