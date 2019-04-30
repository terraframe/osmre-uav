package gov.geoplatform.uasdm.bus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.GeoprismUser;

public class Collection extends CollectionBase
{
  private static final long  serialVersionUID = 1371809368;

  public static final int    BUFFER_SIZE      = 1024;

  public static final String RAW              = "raw";

  public static final String PTCLOUD          = "ptcloud";

  public static final String DEM              = "dem";

  public static final String ORTHO            = "ortho";

  final Logger               log              = LoggerFactory.getLogger(Collection.class);

  public Collection()
  {
    super();
  }

  /**
   * Returns null, as a Collection cannot have a child.
   */
  public UasComponent createChild()
  {
    // TODO throw exception.

    return null;
  }

  @Override
  public String getSolrIdField()
  {
    return "collectionId";
  }

  @Override
  public String getSolrNameField()
  {
    return "collectionName";
  }

  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addMission((Mission) uasComponent);
  }
  
  public static java.util.Collection<Collection> getMissingMetadata()
  {
    java.util.Collection<Collection> collectionList = new LinkedHashSet<Collection>();

    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      QueryFactory qf = new QueryFactory();

      CollectionQuery cQ = new CollectionQuery(qf);

      CollectionUploadEventQuery eQ = new CollectionUploadEventQuery(qf);

      // Get Events created by the current user
      eQ.WHERE(eQ.getGeoprismUser().EQ(singleActor));

      // Get Collections associated with those tasks
      cQ.WHERE(cQ.getOid().EQ(eQ.getCollection().getOid()));

      // Get the Missions of those Collections;
      cQ.AND(cQ.getMetadataUploaded().EQ(false).OR(cQ.getMetadataUploaded().EQ((Boolean) null)));

      OIterator<? extends Collection> i = cQ.getIterator();

      for (Collection collection : i)
      {
        collectionList.add(collection);
      }
    }

    return collectionList;
  }
  
  private JSONObject toMetadataMessage()
  {
    JSONObject object = new JSONObject();
    object.put("collectionId", this.getOid());
    object.put("message", "Metadata missing for collection [" + this.getName() + "]");

    return object;
  }
  
  public static JSONArray toMetadataMessage(java.util.Collection<Collection> collections)
  {
    JSONArray messages = new JSONArray();

    for (Collection collection : collections)
    {
      messages.put(collection.toMetadataMessage());
    }

    return messages;
  }
  
  public void uploadMetadata(String name, InputStream istream)
  {
    if (name.endsWith("_uasmeta.xml") && isValidName(name))
    {
      List<UasComponent> ancestors = this.getAncestors();

//      String key = this.buildAccessibleSupportKey() + name;
      String key = name;

      File temp = null;

      try
      {
        temp = File.createTempFile("metadata", "xml");

        try (FileOutputStream ostream = new FileOutputStream(temp))
        {
          IOUtils.copy(istream, ostream);
        }

        try
        {
          TransferManager tx = new TransferManager(new ClasspathPropertiesFileCredentialsProvider());

          try
          {
            Upload myUpload = tx.upload(AppProperties.getBucketName(), key, temp);
            myUpload.waitForCompletion();

            this.lock();
            this.setMetadataUploaded(true);
            this.apply();
          }
          finally
          {
            tx.shutdownNow();
          }

          SolrService.updateOrCreateMetadataDocument(ancestors, this, key, name, temp);
        }
        catch (AmazonClientException | InterruptedException e)
        {
          throw new ProgrammingErrorException(e);
        }
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
      finally
      {
        if (temp != null)
        {
          FileUtils.deleteQuietly(temp);
        }
      }
    }
    else
    {
      throw new InvalidMetadataFilenameException("The name field has an invalid character");
    }
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

      this.createS3Folder(this.buildPointCloudKey());

      this.createS3Folder(this.buildDemKey());

      this.createS3Folder(this.buildOrthoKey());
    }
  }

  public void delete()
  {
    List<WorkflowTask> tasks = this.getTasks();

    for (WorkflowTask task : tasks)
    {
      task.delete();
    }

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildRawKey());

      this.deleteS3Folder(this.buildPointCloudKey());

      this.deleteS3Folder(this.buildDemKey());

      this.deleteS3Folder(this.buildOrthoKey());
    }
  }

  public List<WorkflowTask> getTasks()
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(this));

    OIterator<? extends WorkflowTask> iterator = query.getIterator();

    try
    {
      return new LinkedList<WorkflowTask>(iterator.getAll());
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

  public String buildPointCloudKey()
  {
    return this.getS3location() + PTCLOUD + "/";
  }

  public String buildDemKey()
  {
    return this.getS3location() + DEM + "/";
  }

  public String buildOrthoKey()
  {
    return this.getS3location() + ORTHO + "/";
  }

  public void uploadArchive(WorkflowTask task, File archive)
  {
    String extension = FilenameUtils.getExtension(archive.getName());

    if (extension.equalsIgnoreCase("zip"))
    {
      this.uploadZipArchive(task, archive);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      this.uploadTarGzArchive(task, archive);
    }
  }

  private void uploadTarGzArchive(WorkflowTask task, File archive)
  {
    List<UasComponent> ancestors = this.getAncestors();

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
              this.uploadFile(task, ancestors, this.buildRawKey(), entry.getName(), tmp);
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

  public void uploadZipArchive(WorkflowTask task, File archive)
  {
    List<UasComponent> ancestors = this.getAncestors();

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
          this.uploadFile(task, ancestors, this.buildRawKey(), entry.getName(), tmp);
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

  @Transaction
  private void uploadFile(WorkflowTask task, List<UasComponent> ancestors, String keySuffix, String name, File tmp)
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
            this.log.info("Transfer: " + myUpload.getDescription());
            this.log.info(" - State: " + myUpload.getState());
            this.log.info(" - Progress: " + myUpload.getProgress().getBytesTransferred());
            
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

                log.info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

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

        SolrService.updateOrCreateDocument(ancestors, this, key, name);
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

      SiteObject ptCloud = new SiteObject();
      ptCloud.setId(this.getOid() + "-" + PTCLOUD);
      ptCloud.setName(PTCLOUD);
      ptCloud.setComponentId(this.getOid());
      ptCloud.setKey(this.buildPointCloudKey());
      ptCloud.setType(SiteObject.FOLDER);

      SiteObject dem = new SiteObject();
      dem.setId(this.getOid() + "-" + DEM);
      dem.setName(DEM);
      dem.setComponentId(this.getOid());
      dem.setKey(this.buildDemKey());
      dem.setType(SiteObject.FOLDER);

      SiteObject ortho = new SiteObject();
      ortho.setId(this.getOid() + "-" + ORTHO);
      ortho.setName(ORTHO);
      ortho.setComponentId(this.getOid());
      ortho.setKey(this.buildOrthoKey());
      ortho.setType(SiteObject.FOLDER);

      objects.add(raw);
      objects.add(ptCloud);
      objects.add(dem);
      objects.add(ortho);
    }
    else
    {
      this.getSiteObjects(folder, objects);
    }

    return objects;
  }
}
