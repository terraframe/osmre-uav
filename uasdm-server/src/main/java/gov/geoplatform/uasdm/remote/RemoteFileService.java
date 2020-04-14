package gov.geoplatform.uasdm.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public interface RemoteFileService
{

  public void download(String key, File destination) throws IOException, FileNotFoundException;

  public RemoteFileObject download(String key);

  public RemoteFileObject download(String key, List<Range> ranges);

  public void createFolder(String key);

  public void deleteObject(String key);

  public void deleteObjects(String key);

  public int getItemCount(String key);

  public SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize);

  public void uploadFile(File file, String key, AbstractWorkflowTaskIF task);

}
