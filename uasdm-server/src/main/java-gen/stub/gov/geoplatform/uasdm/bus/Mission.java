package gov.geoplatform.uasdm.bus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
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

public class Mission extends MissionBase
{
  private static final long  serialVersionUID   = -112103870;

  public static final String ACCESSIBLE_SUPPORT = "accessible_support";

  public Mission()
  {
    super();
  }

  public Collection createChild()
  {
    return new Collection();
  }

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
      this.createS3Folder(this.buildAccessibleSupportKey());
    }
  }

  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildAccessibleSupportKey());
    }
  }

  private String buildAccessibleSupportKey()
  {
    return this.getS3location() + ACCESSIBLE_SUPPORT + "/";
  }

  private JSONObject toMetadataMessage()
  {
    JSONObject object = new JSONObject();
    object.put("missionId", this.getOid());
    object.put("message", "Metadata missing for mission [" + this.getName() + "]");

    return object;
  }

  public static List<Mission> getAll()
  {
    MissionQuery query = new MissionQuery(new QueryFactory());

    OIterator<? extends Mission> it = query.getIterator();

    try
    {
      return new LinkedList<Mission>(it.getAll());
    }
    finally
    {
      it.close();
    }
  }

  public static JSONArray toMetadataMessage(List<Mission> missions)
  {
    JSONArray messages = new JSONArray();

    for (Mission mission : missions)
    {
      messages.put(mission.toMetadataMessage());
    }

    return messages;
  }

  public void uploadMetadata(String name, InputStream istream)
  {
    if (name.endsWith("_uasmeta.xml") && isValidName(name))
    {
      String key = this.buildAccessibleSupportKey() + name;

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

          SolrService.updateOrCreateMetadataDocument(this, key, name, temp);
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

  public static List<Mission> getMissingMetadata()
  {
    List<Mission> missionList = new LinkedList<Mission>();

    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      QueryFactory qf = new QueryFactory();

      MissionQuery mQ = new MissionQuery(qf);
      CollectionQuery cQ = new CollectionQuery(qf);

      WorkflowTaskQuery wQ = new WorkflowTaskQuery(qf);

      // Get Workflow Tasks created by the current user
      wQ.WHERE(wQ.getGeoprismUser().EQ(singleActor));

      // Get Collections associated with those tasks
      cQ.WHERE(cQ.getOid().EQ(wQ.getCollection().getOid()));

      // Get the Missions of those Collections;
      mQ.WHERE(mQ.collections(cQ));
      mQ.AND(mQ.getMetadataUploaded().EQ(false).OR(mQ.getMetadataUploaded().EQ((Boolean) null)));

      OIterator<? extends Mission> i = mQ.getIterator();

      for (Mission mission : i)
      {
        missionList.add(mission);
      }
    }

    return missionList;
  }

  @Override
  public List<SiteObject> getSiteObjects(String folder)
  {
    List<SiteObject> objects = new LinkedList<SiteObject>();

    if (folder == null)
    {
      SiteObject object = new SiteObject();
      object.setId(this.getOid() + "-" + ACCESSIBLE_SUPPORT);
      object.setName(ACCESSIBLE_SUPPORT);
      object.setComponentId(this.getOid());
      object.setKey(this.buildAccessibleSupportKey());
      object.setType(SiteObject.FOLDER);

      objects.add(object);
    }
    else
    {
      this.getSiteObjects(folder, objects);
    }

    return objects;
  }
}
