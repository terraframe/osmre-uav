package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.MissionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class Mission extends MissionBase implements MissionIF
{
  private static final long  serialVersionUID   = -112103870;

  public static final String ACCESSIBLE_SUPPORT = "accessible_support";

  public Mission()
  {
    super();
  }

  @Override
  public Collection createDefaultChild()
  {
    Collection collection = new Collection();

    collection.addPrivilegeType(AllPrivilegeType.AGENCY);

    return collection;
  }

  @Override
  public String getSolrIdField()
  {
    return "missionId";
  }

  @Override
  public String getSolrNameField()
  {
    return "missionName";
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
  public void applyWithParent(UasComponentIF parent)
  {
    super.applyWithParent(parent);

    if (this.isNew())
    {
      this.createS3Folder(this.buildAccessibleSupportKey());
    }
  }

  @Transaction
  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildAccessibleSupportKey(), ACCESSIBLE_SUPPORT);
    }
  }

  private String buildAccessibleSupportKey()
  {
    return this.getS3location() + ACCESSIBLE_SUPPORT + "/";
  }

  public static List<Mission> getAll()
  {
    MissionQuery query = new MissionQuery(new QueryFactory());

    try (OIterator<? extends Mission> it = query.getIterator())
    {
      return new LinkedList<Mission>(it.getAll());
    }
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(String folder, Integer pageNumber, Integer pageSize)
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
      return this.getSiteObjects(folder, objects, pageNumber, pageSize);
    }

    return new SiteObjectsResultSet(objects.size(), pageNumber, pageSize, objects, folder);
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }
}
