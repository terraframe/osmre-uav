package gov.geoplatform.uasdm.bus;

import com.runwaysdk.dataaccess.transaction.Transaction;

public class Project extends ProjectBase
{
  private static final long serialVersionUID = 935245787;

  public Project()
  {
    super();
  }

  public Mission createChild()
  {
    return new Mission();
  }

  @Override
  public String getSolrIdField()
  {
    return "projectId";
  }

  @Override
  public String getSolrNameField()
  {
    return "projectName";
  }

  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    return this.addSite((Site) uasComponent);
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
  }

}
