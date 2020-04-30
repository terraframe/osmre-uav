package gov.geoplatform.uasdm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.gis.geoserver.GeoserverFacade;

public class Util
{
  public static final int BUFFER_SIZE = 1024;

  public static void uploadFileToS3(File child, String key, AbstractWorkflowTaskIF task)
  {
    RemoteFileFacade.uploadFile(child, key, task);
  }

  public static void createImageServices(String workspace, UasComponentIF imageryComponent)
  {
    try
    {
      List<SiteObject> objects = imageryComponent.getSiteObjects(ImageryComponent.ORTHO, null, null).getObjects();

      Util.getSiteObjects(ImageryComponent.ORTHO, objects, imageryComponent);

      for (SiteObject object : objects)
      {
        String key = object.getKey();

        if (key.endsWith(".tif"))
        {
          String storeName = imageryComponent.getStoreName(key);

          if (GeoserverFacade.layerExists(workspace, storeName))
          {
            Util.removeCoverageStore(workspace, storeName);
          }

          File geotiff = Util.download(key, storeName);

          try
          {
            GeoserverFacade.publishGeoTiff(workspace, storeName, geotiff);
          }
          finally
          {
            FileUtils.deleteQuietly(geotiff);
          }
        }
      }
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static void getSiteObjects(String folder, List<SiteObject> objects, UasComponentIF imageryComponent)
  {
//    if (folder.equals(ImageryComponent.ORTHO))
//    {
//      for (SiteObject object : objects)
//      {
//        String key = object.getKey();
//
//        if (key.endsWith(".tif"))
//        {
//          String storeName = imageryComponent.getStoreName(key);
//
//          if (GeoserverFacade.layerExists(storeName))
//          {
//            object.setImageKey(storeName);
//          }
//        }
//      }
//    }
  }

  public static File download(String key, String storeName)
  {
    try
    {
      File temp = Files.createTempFile("geotiff-" + storeName, ".tif").toFile();

      RemoteFileFacade.download(key, temp);

      return temp;
    }
    catch (Throwable t)
    {
      throw new ProgrammingErrorException(t);
    }
  }

  public static void deleteS3Object(String key, ImageryComponent imageryComponent)
  {
//    if (key.endsWith(".tif"))
//    {
//      String[] paths = key.split("/");
//
//      if (paths.length > 1)
//      {
//        if (paths[paths.length - 2].startsWith(ImageryComponent.ORTHO))
//        {
//          String storeName = imageryComponent.getStoreName(key);
//
//          Util.removeCoverageStore(workspace, storeName);
//        }
//      }
//    }
  }

  public static void removeCoverageStore(String workspace, String storeName)
  {
//    GeoserverFacade.removeStyle(storeName);
//    GeoserverFacade.forceRemoveLayer(workspace, storeName);
    GeoserverFacade.removeCoverageStore(workspace, storeName);
  }

  public static boolean isVideoFile(String path)
  {
    final String ext = FilenameUtils.getExtension(path);

    if (ext.equalsIgnoreCase("mp4"))
    {
      return true;
    }

    String mimeType = URLConnection.guessContentTypeFromName(path);

    return mimeType != null && mimeType.startsWith("video");
  }

  public static List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, ImageryComponent imageryComponent, String uploadTarget)
  {
    String extension = archive.getNameExtension();

    if (extension.equalsIgnoreCase("zip"))
    {
      return uploadZipArchive(task, archive, imageryComponent, uploadTarget);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      return uploadTarGzArchive(task, archive, imageryComponent, uploadTarget);
    }

    return new LinkedList<String>();
  }

  public static List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, ImageryComponent imageryComponent, String uploadTarget)
  {
    List<UasComponentIF> ancestors = imageryComponent.getAncestors();
    List<String> filenames = new LinkedList<String>();

    byte[] buffer = new byte[BUFFER_SIZE];

    try (ZipInputStream zis = new ZipInputStream(archive.openNewStream()))
    {
      ZipEntry entry;
      while ( ( entry = zis.getNextEntry() ) != null)
      {
        File tmp = File.createTempFile("raw", "tmp");

        try
        {
          try (FileOutputStream fos = new FileOutputStream(tmp))
          {
            int len;
            while ( ( len = zis.read(buffer) ) > 0)
            {
              fos.write(buffer, 0, len);
            }
          }

          // Upload the file to S3
          String filename = entry.getName();
          String folder = uploadTarget.equals(ImageryComponent.RAW) && isVideoFile(filename) ? ImageryComponent.VIDEO : ImageryComponent.RAW;

          boolean success = uploadFile(task, ancestors, imageryComponent.buildUploadKey(folder), filename, tmp, imageryComponent);

          if (success)
          {
            filenames.add(filename);
          }
        }
        finally
        {
          FileUtils.deleteQuietly(tmp);
        }
      }
    }
    catch (IOException e)
    {
      task.createAction(e.getMessage(), "error");

      throw new ProgrammingErrorException(e);
    }

    return filenames;
  }

  public static List<String> uploadTarGzArchive(AbstractWorkflowTask task, ApplicationResource archive, ImageryComponent imageryComponent, String uploadTarget)
  {
    List<UasComponentIF> ancestors = imageryComponent.getAncestors();
    List<String> filenames = new LinkedList<String>();

    byte data[] = new byte[BUFFER_SIZE];

    try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(archive.openNewStream()))
    {
      try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn))
      {
        TarArchiveEntry entry;

        while ( ( entry = (TarArchiveEntry) tarIn.getNextEntry() ) != null)
        {
          /** If the entry is a directory, create the directory. **/
          String filename = entry.getName();
          if (entry.isDirectory())
          {
            File f = new File(filename);
            boolean created = f.mkdir();
            if (!created)
            {
              System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n", f.getAbsolutePath());
            }
          }
          else
          {
            File tmp = File.createTempFile("raw", "tmp");

            try
            {
              try (FileOutputStream fos = new FileOutputStream(tmp))
              {
                int count;

                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE))
                {
                  while ( ( count = tarIn.read(data, 0, BUFFER_SIZE) ) != -1)
                  {
                    dest.write(data, 0, count);
                  }
                }
              }

              // Upload the file to S3
              String folder = uploadTarget.equals(ImageryComponent.RAW) && isVideoFile(filename) ? ImageryComponent.VIDEO : ImageryComponent.RAW;

              boolean success = uploadFile(task, ancestors, imageryComponent.buildUploadKey(folder), filename, tmp, imageryComponent);

              if (success)
              {
                filenames.add(filename);
              }
            }
            finally
            {
              FileUtils.deleteQuietly(tmp);
            }

          }
        }
      }
    }
    catch (IOException e)
    {
      task.createAction(e.getMessage(), "error");

      throw new ProgrammingErrorException(e);
    }

    return filenames;
  }

  @Transaction
  public static DocumentIF putFile(UasComponent component, String folder, String fileName, RemoteFileMetadata metadata, InputStream stream)
  {
    String key = component.getS3location() + folder + "/" + fileName;

    RemoteFileFacade.uploadFile(key, metadata, stream);

    final DocumentIF document = component.createDocumentIfNotExist(key, fileName);

    if (document.isNew())
    {
      SolrService.updateOrCreateDocument(component.getAncestors(), component, key, fileName);
    }

    return document;
  }

  @Transaction
  public static boolean uploadFile(AbstractWorkflowTask task, List<UasComponentIF> ancestors, String keySuffix, String name, File tmp, ImageryComponent imageryComponent)
  {
    if (UasComponentIF.isValidName(name))
    {
      String key = keySuffix + name;

      try
      {
        Util.uploadFileToS3(tmp, key, task);

        final UasComponentIF component = imageryComponent.getUasComponent();
        component.createDocumentIfNotExist(key, name);

        SolrService.updateOrCreateDocument(ancestors, component, key, name);

        return true;
      }
      catch (Exception e)
      {
        task.createAction(e.getMessage(), "error");
      }
    }
    else
    {
      task.createAction("The filename [" + name + "] is invalid. No spaces or special characters such as <, >, -, +, =, !, @, #, $, %, ^, &, *, ?,/, \\ or apostrophes are allowed.", "error");
    }

    return false;
  }

  public static boolean hasImages(List<String> files)
  {
    boolean hasImages = files.parallelStream().anyMatch(filename -> {
      final String extension = FilenameUtils.getExtension(filename);

      return !extension.equalsIgnoreCase("mp4");
    });

    return hasImages;
  }

}
