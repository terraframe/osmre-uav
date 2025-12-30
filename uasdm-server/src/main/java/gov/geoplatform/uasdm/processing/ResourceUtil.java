package gov.geoplatform.uasdm.processing;

import java.util.List;

import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.GenericException;

public abstract class ResourceUtil
{
  public static ApplicationFileResource getResource(ApplicationFileResource res)
  {
    boolean isArchive = res.getName().endsWith(".zip") || res.getName().endsWith(".tar.gz");

    if (isArchive)
    {
      List<ApplicationFileResource> files = res.getChildrenFiles().getAll();

      if (files.size() > 1)
      {
        GenericException ex = new GenericException();
        ex.setUserMessage("Uploaded archives must contain only a single geotiff or a tiff and an .aux.xml file");

        throw ex;
      }

      return files.get(0);
    }

    return res;
  }

}
