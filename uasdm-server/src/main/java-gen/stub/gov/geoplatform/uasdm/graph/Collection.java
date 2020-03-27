package gov.geoplatform.uasdm.graph;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.CollectionUploadEventQuery;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import net.geoprism.GeoprismUser;

public class Collection extends CollectionBase implements ImageryComponent, CollectionIF
{
  public static final long    serialVersionUID = 166854005;

  private static final String PARENT_EDGE      = "gov.geoplatform.uasdm.graph.MissionHasCollection";

  final Logger                log              = LoggerFactory.getLogger(Collection.class);

  public Collection()
  {
    super();
  }

  @Override
  public void appLock()
  {
    // Balk
  }

  @Override
  public UasComponent createDefaultChild()
  {
    throw new UnsupportedOperationException();
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

  @Override
  protected MdEdgeDAOIF getParentMdEdge()
  {
    return MdEdgeDAO.getMdEdgeDAO(PARENT_EDGE);
  }

  @Override
  protected MdEdgeDAOIF getChildMdEdge()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<UasComponentIF> getChildren()
  {
    return new LinkedList<UasComponentIF>();
  }

  @Override
  protected String buildProductExpandClause()
  {
    return Collection.expandClause();
  }

  public JSONObject toMetadataMessage()
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
    if (this.isNew())
    {
      this.setMetadataUploaded(false);
    }

    super.applyWithParent(parent);

    if (this.isNew())
    {
      this.createS3Folder(this.buildRawKey());

      this.createS3Folder(this.buildPointCloudKey());

      this.createS3Folder(this.buildDemKey());

      this.createS3Folder(this.buildOrthoKey());
    }
  }

  @Transaction
  public void delete()
  {
    List<AbstractWorkflowTask> tasks = this.getTasks();

    for (AbstractWorkflowTask task : tasks)
    {
      task.delete();
    }

    List<CollectionUploadEvent> events = this.getEvents();

    for (CollectionUploadEvent event : events)
    {
      event.delete();
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
    Util.deleteS3Object(key, this);
  }

  public List<AbstractWorkflowTask> getTasks()
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this.getOid()));

    try (OIterator<? extends WorkflowTask> iterator = query.getIterator())
    {
      return new LinkedList<AbstractWorkflowTask>(iterator.getAll());
    }
  }

  public List<CollectionUploadEvent> getEvents()
  {
    CollectionUploadEventQuery query = new CollectionUploadEventQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this.getOid()));

    try (OIterator<? extends CollectionUploadEvent> iterator = query.getIterator())
    {
      return new LinkedList<CollectionUploadEvent>(iterator.getAll());
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
  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    return Util.uploadArchive(task, archive, this, uploadTarget);
  }

  @Override
  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    return Util.uploadZipArchive(task, archive, this, uploadTarget);
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(String folder, Integer pageNumber, Integer pageSize)
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
      return this.getSiteObjects(folder, objects, pageNumber, pageSize);
    }

    return new SiteObjectsResultSet(objects.size(), pageNumber, pageSize, objects, folder);
  }

  @Override
  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize)
  {
    if (!folder.equals(RAW) && ( pageNumber != null || pageSize != null ))
    {
      throw new ProgrammingErrorException(new UnsupportedOperationException("Pagination only supported for raw right now."));
    }

    SiteObjectsResultSet rs = super.getSiteObjects(folder, objects, pageNumber, pageSize);

    Util.getSiteObjects(folder, objects, this);

    return rs;
  }

  public void createImageServices()
  {
    Util.createImageServices(this);
  }

  @Override
  public Integer getNumberOfChildren()
  {
    return this.getDocuments().size();
  }

  public UasComponentIF getUasComponent()
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
    WorkflowTask workflowTask = new WorkflowTask();
    workflowTask.setUploadId(uploadId);
    workflowTask.setComponent(this.getOid());
    workflowTask.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    workflowTask.setTaskLabel("UAV data upload for collection [" + this.getName() + "]");

    return workflowTask;
  }

  public static java.util.Collection<CollectionIF> getMissingMetadata()
  {
    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
      final MdVertexDAOIF mdCollection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdCollection.getDBClassName());
      builder.append(" WHERE " + METADATAUPLOADED + " = :metadataUploaded");
      builder.append(" AND " + OWNER + " = :owner");
      builder.append(" AND outE('" + mdEdge.getDBClassName() + "').size() > 0");

      final GraphQuery<CollectionIF> query = new GraphQuery<CollectionIF>(builder.toString());
      query.setParameter("metadataUploaded", false);
      query.setParameter("owner", singleActor.getOid());

      return query.getResults();
    }

    return new LinkedHashSet<CollectionIF>();
  }

  public static JSONArray toMetadataMessage(java.util.Collection<CollectionIF> collections)
  {
    JSONArray messages = new JSONArray();

    for (CollectionIF collection : collections)
    {
      messages.put(collection.toMetadataMessage());
    }

    return messages;
  }

  public static String expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    return "OUT('" + mdEdge.getDBClassName() + "')";
  }

}
