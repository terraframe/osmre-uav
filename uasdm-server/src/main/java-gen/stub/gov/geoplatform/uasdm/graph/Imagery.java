package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.GeoprismUser;

public class Imagery extends ImageryBase implements ImageryIF
{
  private static final long   serialVersionUID = 1836149418;

  private static final String PARENT_EDGE      = "gov.geoplatform.uasdm.graph.ProjectHasImagery";

  final Logger                log              = LoggerFactory.getLogger(Imagery.class);

  public Imagery()
  {
    super();
  }

  @Override
  public UasComponent createDefaultChild()
  {
    throw new UnsupportedOperationException();
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
  protected String buildProductExpandClause()
  {
    throw new UnsupportedOperationException();
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
    Util.deleteS3Object(key, this);
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
    return Util.uploadArchive(task, archive, this, uploadTarget);
  }

  @Override
  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    return Util.uploadZipArchive(task, archive, this, uploadTarget);
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

    Util.getSiteObjects(folder, objects, this);
  }

  public void createImageServices()
  {
    Util.createImageServices(this);
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

}