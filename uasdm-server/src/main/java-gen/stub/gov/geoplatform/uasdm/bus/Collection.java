package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.view.SiteObject;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import net.geoprism.GeoprismUser;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

public class Collection extends CollectionBase implements ImageryComponent
{
  private static final long serialVersionUID = 1371809368;

  final Logger              log              = LoggerFactory.getLogger(Collection.class);

  public Collection()
  {
    super();
  }

  /**
   * Returns null, as a Collection cannot have a child.
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

    if (this.getImageHeight() != null)
    {
      object.put("imageHeight", this.getImageHeight());
    }
    if (this.getImageWidth() != null)
    {
      object.put("imageWidth", this.getImageWidth());
    }

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
    List<AbstractWorkflowTask> tasks = this.getTasks();

    for (AbstractWorkflowTask task : tasks)
    {
      task.delete();
    }

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildRawKey(), RAW);

      this.deleteS3Folder(this.buildPointCloudKey(), PTCLOUD);

      this.deleteS3Folder(this.buildDemKey(), DEM);

      this.deleteS3Folder(this.buildOrthoKey(), ORTHO);
    }
  }

  protected void deleteS3Object(String key)
  {
    Imagery.deleteS3Object(key, this);
  }

  public List<AbstractWorkflowTask> getTasks()
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(this));

    OIterator<? extends WorkflowTask> iterator = query.getIterator();

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

  @Override
  public void uploadArchive(AbstractWorkflowTask task, File archive, String uploadTarget)
  {
    Imagery.uploadArchive(task, archive, this, uploadTarget);
  }

  @Override
  public void uploadZipArchive(AbstractWorkflowTask task, File archive, String uploadTarget)
  {
    Imagery.uploadZipArchive(task, archive, this, uploadTarget);
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

  public Collection getUasComponent()
  {
    return this;
  }

  public Logger getLog()
  {
    return this.log;
  }
}
