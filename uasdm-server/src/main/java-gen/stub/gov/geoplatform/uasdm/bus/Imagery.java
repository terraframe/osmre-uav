package gov.geoplatform.uasdm.bus;

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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.GeoprismUser;
import net.geoprism.gis.geoserver.GeoserverFacade;

public class Imagery extends ImageryBase implements ImageryComponent
{
  public static final long serialVersionUID = -134374478;

  final Logger             log              = LoggerFactory.getLogger(Imagery.class);

  public Imagery()
  {
    super();
  }

  /**
   * Returns null, as a Imagery cannot have a child.
   */
  @Override
  public UasComponent createDefaultChild()
  {
    // TODO throw exception.
    return null;
  }

  @Override
  public String getSolrIdField()
  {
    return "imageryId";
  }

  @Override
  public String getSolrNameField()
  {
    return "imageryName";
  }

  @Override
  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addProject((Project) uasComponent);
  }

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  @Override
  public void applyWithParent(UasComponentIF parent)
  {
    super.applyWithParent(parent);

    if (this.isNew())
    {
      this.createS3Folder(this.buildRawKey());

      this.createS3Folder(this.buildGeoRefKey());

      this.createS3Folder(this.buildOrthoKey());
    }
  }

  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildRawKey(), RAW);

      this.deleteS3Folder(this.buildGeoRefKey(), GEOREF);

      this.deleteS3Folder(this.buildOrthoKey(), ORTHO);
    }
  }

  protected void deleteS3Object(String key)
  {
    Imagery.deleteS3Object(key, this);
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    ImageryWorkflowTaskQuery query = new ImageryWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getImagery().EQ(this.getOid()));

    try (OIterator<? extends ImageryWorkflowTask> iterator = query.getIterator())
    {
      return new LinkedList<AbstractWorkflowTask>(iterator.getAll());
    }
  }

  public String buildRawKey()
  {
    return this.getS3location() + RAW + "/";
  }

  public String buildGeoRefKey()
  {
    return this.getS3location() + GEOREF + "/";
  }

  public String buildOrthoKey()
  {
    return this.getS3location() + ORTHO + "/";
  }

  @Override
  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    return Imagery.uploadArchive(task, archive, this, uploadTarget);
  }

  @Override
  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    return Imagery.uploadZipArchive(task, archive, this, uploadTarget);
  }

  @Override
  public List<SiteObject> getSiteObjects(String folder)
  {
    List<SiteObject> objects = new LinkedList<SiteObject>();

    if (folder == null)
    {
      SiteObject raw = new SiteObject();
      raw.setId(this.getOid() + "-" + RAW);
      raw.setName(RAW);
      raw.setComponentId(this.getOid());
      raw.setKey(this.buildRawKey());
      raw.setType(SiteObject.FOLDER);

      SiteObject geoRef = new SiteObject();
      geoRef.setId(this.getOid() + "-" + GEOREF);
      geoRef.setName(GEOREF);
      geoRef.setComponentId(this.getOid());
      geoRef.setKey(this.buildGeoRefKey());
      geoRef.setType(SiteObject.FOLDER);

      SiteObject ortho = new SiteObject();
      ortho.setId(this.getOid() + "-" + ORTHO);
      ortho.setName(ORTHO);
      ortho.setComponentId(this.getOid());
      ortho.setKey(this.buildOrthoKey());
      ortho.setType(SiteObject.FOLDER);

      objects.add(raw);
      objects.add(geoRef);
      objects.add(ortho);
    }
    else
    {
      this.getSiteObjects(folder, objects);
    }

    return objects;
  }

  @Override
  protected void getSiteObjects(String folder, List<SiteObject> objects)
  {
    super.getSiteObjects(folder, objects);

    Imagery.getSiteObjects(folder, objects, this);
  }

  public void createImageServices()
  {
    Imagery.createImageServices(this);
  }

  public String getStoreName(String key)
  {
    String baseName = FilenameUtils.getBaseName(key);

    return this.getOid() + "-" + baseName;
  }

  public Imagery getUasComponent()
  {
    return this;
  }

  public Logger getLog()
  {
    return this.log;
  }

  @Override
  public AbstractWorkflowTask createWorkflowTask(String uploadId)
  {
    ImageryWorkflowTask task = new ImageryWorkflowTask();
    task.setUploadId(uploadId);
    task.setImagery(this.getOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setTaskLabel("UAV data upload for imagery [" + this.getName() + "]");

    return task;
  }

  public static void createImageServices(ImageryComponent imageryComponent)
  {
    try
    {
      List<SiteObject> objects = imageryComponent.getSiteObjects(ORTHO);

      Imagery.getSiteObjects(ORTHO, objects, imageryComponent);

      for (SiteObject object : objects)
      {
        String key = object.getKey();

        if (key.endsWith(".tif"))
        {
          String storeName = imageryComponent.getStoreName(key);

          if (GeoserverFacade.layerExists(storeName))
          {
            Imagery.removeCoverageStore(storeName);
          }

          File geotiff = Imagery.download(key, storeName);

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
    if (folder.equals(ORTHO))
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
        if (paths[paths.length - 2].startsWith(ORTHO))
        {
          String storeName = imageryComponent.getStoreName(key);

          Imagery.removeCoverageStore(storeName);
        }
      }
    }
  }

  protected static void removeCoverageStore(String storeName)
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

  private static List<String> uploadTarGzArchive(AbstractWorkflowTask task, ApplicationResource archive, ImageryComponent imageryComponent, String uploadTarget)
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
  private static boolean uploadFile(AbstractWorkflowTask task, List<UasComponentIF> ancestors, String keySuffix, String name, File tmp, ImageryComponent imageryComponent)
  {
    if (isValidName(name))
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
