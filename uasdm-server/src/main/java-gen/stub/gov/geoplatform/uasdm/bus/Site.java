package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.view.AttributeListType;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.EqCondition;
import gov.geoplatform.uasdm.view.Option;

public class Site extends SiteBase
{
  private static final long  serialVersionUID  = -986618112;

  public static final String DEFAULT_SITE_NAME = "Cottonwood";

  public Site()
  {
    super();
  }

  @Override
  public void applyWithParent(UasComponent parent)
  {
    if (this.isNew() && isDuplicateSiteFolderName(this.getOid(), this.getFolderName()))
    {
      DuplicateSiteException e = new DuplicateSiteException();
      e.setFolderName(this.getFolderName());

      throw e;
    }

    super.applyWithParent(parent);
  }

  @Override
  public List<AttributeType> attributes()
  {
    AttributeListType attributeType = (AttributeListType) AttributeType.create(this.getMdAttributeDAO(Site.BUREAU));
    attributeType.setOptions(Site.getBureauOptions());

    AttributeType otherAttributeType = AttributeType.create(this.getMdAttributeDAO(Site.OTHERBUREAUTXT));
    otherAttributeType.setCondition(Site.getBureauCondition());
    otherAttributeType.setRequired(true);

    List<AttributeType> list = super.attributes();
    list.add(attributeType);
    list.add(otherAttributeType);

    return list;
  }

  @Override
  public String getSolrIdField()
  {
    return "siteId";
  }

  @Override
  public String getSolrNameField()
  {
    return "siteName";
  }

  public Project createChild()
  {
    return new Project();
  }

  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    return this.addProjects((Project) uasComponent);
  }

  protected boolean needsUpdate()
  {
    return this.isModified(Site.BUREAU);
  }

  @Request
  public static EqCondition getBureauCondition()
  {
    return new EqCondition(Site.BUREAU, Bureau.getByKey(Bureau.OTHER).getOid());
  }

  @Request
  public static List<Option> getBureauOptions()
  {
    List<Option> options = new LinkedList<Option>();

    BureauQuery query = new BureauQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel());

    List<? extends Bureau> bureaus = query.getIterator().getAll();

    for (Bureau bureau : bureaus)
    {
      options.add(new Option(bureau.getOid(), bureau.getDisplayLabel()));
    }

    return options;
  }

  public static boolean isDuplicateSiteFolderName(String oid, String folderName)
  {
    QueryFactory qf = new QueryFactory();
    SiteQuery query = new SiteQuery(qf);

    query.WHERE(query.getFolderName().EQ(folderName));

    if (oid != null)
    {
      query.AND(query.getOid().NE(oid));
    }

    OIterator<? extends UasComponent> i = query.getIterator();

    try
    {
      if (i.hasNext())
      {
        return true;
      }
    }
    finally
    {
      i.close();
    }
    return false;
  }

}
