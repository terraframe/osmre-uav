package gov.geoplatform.uasdm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMStatusServer;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.gis.geoserver.GeoserverFacade;

public class Util
{
  private static Logger   logger      = LoggerFactory.getLogger(ODMStatusServer.class);

  public static final int BUFFER_SIZE = 1024;

  public static void uploadFileToS3(File child, String key, AbstractWorkflowTaskIF task)
  {
    try
    {
      BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
      TransferManager tx = new TransferManager(new StaticCredentialsProvider(awsCreds));

      try
      {
        Upload myUpload = tx.upload(AppProperties.getBucketName(), key, child);

        if (myUpload.isDone() == false)
        {
          logger.info("Source: " + child.getAbsolutePath());
          logger.info("Destination: " + myUpload.getDescription());

          if (task != null)
          {
            task.lock();
            task.setMessage(myUpload.getDescription());
            task.apply();
          }
        }

        myUpload.addProgressListener(new ProgressListener()
        {
          int count = 0;

          @Override
          public void progressChanged(ProgressEvent progressEvent)
          {
            if (count % 2000 == 0)
            {
              long total = myUpload.getProgress().getTotalBytesToTransfer();
              long current = myUpload.getProgress().getBytesTransferred();

              logger.info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

              count = 0;
            }

            count++;
          }
        });

        myUpload.waitForCompletion();
      }
      finally
      {
        tx.shutdownNow();
      }
    }
    catch (Exception e)
    {
      if (task != null)
      {
        task.createAction(e.getMessage(), "error");
      }
      logger.error("Exception occured while uploading [" + key + "].", e);
    }
  }

  public static void createImageServices(ImageryComponent imageryComponent)
  {
    try
    {
      List<SiteObject> objects = imageryComponent.getSiteObjects(ImageryComponent.ORTHO);

      Util.getSiteObjects(ImageryComponent.ORTHO, objects, imageryComponent);

      for (SiteObject object : objects)
      {
        String key = object.getKey();

        if (key.endsWith(".tif"))
        {
          String storeName = imageryComponent.getStoreName(key);

          if (GeoserverFacade.layerExists(storeName))
          {
            Util.removeCoverageStore(storeName);
          }

          File geotiff = Util.download(key, storeName);

          GeoserverFacade.publishGeoTiff(storeName, geotiff);
        }
      }
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static void getSiteObjects(String folder, List<SiteObject> objects, ImageryComponent imageryComponent)
  {
    if (folder.equals(ImageryComponent.ORTHO))
    {
      for (SiteObject object : objects)
      {
        String key = object.getKey();

        if (key.endsWith(".tif"))
        {
          String storeName = imageryComponent.getStoreName(key);

          if (GeoserverFacade.layerExists(storeName))
          {
            object.setImageKey(storeName);
          }
        }
      }
    }
  }

  public static File download(String key, String storeName)
  {
    try
    {
      BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
      AmazonS3 client = new AmazonS3Client(new StaticCredentialsProvider(awsCreds));

      String bucketName = AppProperties.getBucketName();

      GetObjectRequest request = new GetObjectRequest(bucketName, key);

      S3Object s3Obj = client.getObject(request);

      File temp = Files.createTempFile("geotiff-" + storeName, ".tif").toFile();
      IOUtils.copy(s3Obj.getObjectContent(), new FileOutputStream(temp));

      return temp;
    }
    catch (Throwable t)
    {
      throw new ProgrammingErrorException(t);
    }
  }

  public static void deleteS3Object(String key, ImageryComponent imageryComponent)
  {
    if (key.endsWith(".tif"))
    {
      String[] paths = key.split("/");
      if (paths.length > 1)
      {
        if (paths[paths.length - 2].startsWith(ImageryComponent.ORTHO))
        {
          String storeName = imageryComponent.getStoreName(key);

          Util.removeCoverageStore(storeName);
        }
      }
    }
  }

  public static void removeCoverageStore(String storeName)
  {
    GeoserverFacade.removeStyle(storeName);
    GeoserverFacade.forceRemoveLayer(storeName);
    GeoserverFacade.removeCoverageStore(storeName);
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

          boolean success = uploadFile(task, ancestors, imageryComponent.buildUploadKey(uploadTarget), filename, tmp, imageryComponent);

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
              boolean success = uploadFile(task, ancestors, imageryComponent.buildUploadKey(uploadTarget), filename, tmp, imageryComponent);

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

}
