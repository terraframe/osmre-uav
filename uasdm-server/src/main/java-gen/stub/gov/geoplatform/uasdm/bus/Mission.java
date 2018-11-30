package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.AppProperties;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import net.geoprism.GeoprismUser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

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

  public void uploadMetadata(String name, long length, InputStream istream)
  {
    if (name.endsWith("_uasmeta.xml") && isValidName(name))
    {
      String key = this.buildAccessibleSupportKey() + name;

      try
      {
        TransferManager tx = new TransferManager(new ClasspathPropertiesFileCredentialsProvider());

        try
        {
          ObjectMetadata metadata = new ObjectMetadata();
          metadata.setContentLength(length);

          Upload myUpload = tx.upload(AppProperties.getBucketName(), key, istream, metadata);
          myUpload.waitForCompletion();
          
          this.lock();
          this.setMetadataUploaded(true);
          this.apply();
        }
        finally
        {
          tx.shutdownNow();
        }
      }
      catch (AmazonClientException | InterruptedException e)
      {
        throw new ProgrammingErrorException(e);
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
      mQ.AND(mQ.getMetadataUploaded().EQ(false).OR(mQ.getMetadataUploaded().EQ((Boolean)null)));

      OIterator<? extends Mission> i = mQ.getIterator();
      
      for (Mission mission : i)
      {
        missionList.add(mission);
      }
    }
    
    return missionList;
  }
}
