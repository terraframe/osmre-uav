package gov.geoplatform.uasdm.bus;

/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 *
 * @author Autogenerated by RunwaySDK
 */
@com.runwaysdk.business.ClassSignature(hash = -1872605417)
public enum AllPrivilegeType implements com.runwaysdk.business.BusinessEnumeration
{
  AGENCY(),
  
  OWNER(),
  
  PUBLIC();
  
  public static final java.lang.String CLASS = "gov.geoplatform.uasdm.bus.AllPrivilegeType";
  private gov.geoplatform.uasdm.bus.PrivilegeType enumeration;
  
  private synchronized void loadEnumeration()
  {
    gov.geoplatform.uasdm.bus.PrivilegeType enu = gov.geoplatform.uasdm.bus.PrivilegeType.getEnumeration(this.name());
    setEnumeration(enu);
  }
  
  private synchronized void setEnumeration(gov.geoplatform.uasdm.bus.PrivilegeType enumeration)
  {
    this.enumeration = enumeration;
  }
  
  public java.lang.String getOid()
  {
    loadEnumeration();
    return enumeration.getOid();
  }
  
  public java.lang.String getEnumName()
  {
    loadEnumeration();
    return enumeration.getEnumName();
  }
  
  public java.lang.String getDisplayLabel()
  {
    loadEnumeration();
    return enumeration.getDisplayLabel().getValue(com.runwaysdk.session.Session.getCurrentLocale());
  }
  
  public static AllPrivilegeType get(String oid)
  {
    for (AllPrivilegeType e : AllPrivilegeType.values())
    {
      if (e.getOid().equals(oid))
      {
        return e;
      }
    }
    return null;
  }
  
}