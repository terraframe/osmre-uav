package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.SiteObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.geoprism.gis.geoserver.GeoserverFacade;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class Imagery extends ImageryBase implements ImageryComponent
{
  private static final long serialVersionUID = -134374478;
  
  final Logger               log              = LoggerFactory.getLogger(Imagery.class);
  
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
  public void applyWithParent(UasComponent parent)
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
  
  protected static void deleteS3Object(String key, ImageryComponent imageryComponent)
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
  
  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    ImageryWorkflowTaskQuery query = new ImageryWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getImagery().EQ(this));

    OIterator<? extends ImageryWorkflowTask> iterator = query.getIterator();

    try
    {
      return new LinkedList<AbstractWorkflowTask>(iterator.getAll());
    }
    finally
    {
      iterator.close();
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

  public void uploadArchive(WorkflowTask task, File archive)
  {
    Imagery.uploadArchive(task, archive, this);
  }
  
  public void uploadZipArchive(WorkflowTask task, File archive)
  {
    Imagery.uploadZipArchive(task, archive, this);
  }


  public static void uploadArchive(WorkflowTask task, File archive, ImageryComponent imageryComponent)
  {
    String extension = FilenameUtils.getExtension(archive.getName());

    if (extension.equalsIgnoreCase("zip"))
    {
      uploadZipArchive(task, archive, imageryComponent);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      uploadTarGzArchive(task, archive, imageryComponent);
    }
  }
  
  protected static void uploadZipArchive(WorkflowTask task, File archive, ImageryComponent imageryComponent)
  {
    List<UasComponent> ancestors = imageryComponent.getAncestors();

    byte[] buffer = new byte[BUFFER_SIZE];

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archive)))
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
          uploadFile(task, ancestors, imageryComponent.buildRawKey(), entry.getName(), tmp, imageryComponent);
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
  }
  
  private static void uploadTarGzArchive(WorkflowTask task, File archive, ImageryComponent imageryComponent)
  {
    List<UasComponent> ancestors = imageryComponent.getAncestors();

    byte data[] = new byte[BUFFER_SIZE];

    try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new FileInputStream(archive)))
    {
      try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn))
      {
        TarArchiveEntry entry;

        while ( ( entry = (TarArchiveEntry) tarIn.getNextEntry() ) != null)
        {
          /** If the entry is a directory, create the directory. **/
          if (entry.isDirectory())
          {
            File f = new File(entry.getName());
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
              uploadFile(task, ancestors, imageryComponent.buildRawKey(), entry.getName(), tmp, imageryComponent);
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
  }
  
  @Transaction
  private static void uploadFile(WorkflowTask task, List<UasComponent> ancestors, String keySuffix, String name, File tmp, ImageryComponent imageryComponent)
  {
    if (isValidName(name))
    {
      String key = keySuffix + name;

      try
      {
        TransferManager tx = new TransferManager(new ClasspathPropertiesFileCredentialsProvider());

        try
        {
          Upload myUpload = tx.upload(AppProperties.getBucketName(), key, tmp);

          if (myUpload.isDone() == false)
          {
            imageryComponent.getLog().info("Transfer: " + myUpload.getDescription());
            imageryComponent.getLog().info(" - State: " + myUpload.getState());
            imageryComponent.getLog().info(" - Progress: " + myUpload.getProgress().getBytesTransferred());

            task.lock();
            task.setMessage(myUpload.getDescription());
            task.apply();
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

                imageryComponent.getLog().info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

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

        SolrService.updateOrCreateDocument(ancestors, imageryComponent.getUasComponent(), key, name);
      }
      catch (Exception e)
      {
        task.createAction(e.getMessage(), "error");
      }
    }
    else
    {
      task.createAction("The filename [" + name + "] is invalid", "error");
    }
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
  
  protected static void getSiteObjects(String folder, List<SiteObject> objects, ImageryComponent imageryComponent)
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
      AmazonS3 client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
      
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
  
  public void createImageServices()
  {
    Imagery.createImageServices(this);
  }
  
  public static void createImageServices(ImageryComponent imageryComponent)
  {
    try
    {
      LinkedList<SiteObject> objects = new LinkedList<SiteObject>();

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
  
  
  public String getStoreName(String key)
  {
    /*
     * There will only be a single orth tiff per collections so just use the oid
     */
    return this.getOid();
  }
  
  public Imagery getUasComponent()
  {
    return this;
  }
  
  public Logger getLog()
  {
    return this.log;
  }
  

  
}
