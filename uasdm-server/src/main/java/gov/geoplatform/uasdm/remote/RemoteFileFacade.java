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

  public static void deleteObject(String key)
  {
    service.deleteObject(key);
  }

  public static void deleteObjects(String key)
  {
    service.deleteObjects(key);
  }

  public static int getItemCount(String key)
  {
    return service.getItemCount(key);
  }

  public static SiteObjectsResultSet getSiteObjects(UasComponentIF component, String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize)
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

}
