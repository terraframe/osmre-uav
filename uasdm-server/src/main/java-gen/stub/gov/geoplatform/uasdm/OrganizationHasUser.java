package gov.geoplatform.uasdm;

public class OrganizationHasUser extends OrganizationHasUserBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1080548542;
  
  public OrganizationHasUser(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public OrganizationHasUser(net.geoprism.registry.Organization parent, gov.geoplatform.uasdm.UserInfo child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
