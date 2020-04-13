package gov.geoplatform.uasdm.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize)
  {
    return new SiteObjectsResultSet(0, 1, 10, new LinkedList<SiteObject>(), folder);
  }

  @Override
  public void uploadFile(File file, String key, AbstractWorkflowTaskIF task)
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
}
