package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1001949778)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to PrivilegeType.java
 *
 * @author Autogenerated by RunwaySDK
 */
public class PrivilegeTypeQueryDTO extends com.runwaysdk.system.EnumerationMasterQueryDTO
{
private static final long serialVersionUID = 1001949778;

  protected PrivilegeTypeQueryDTO(String type)
  {
    super(type);
  }

@SuppressWarnings("unchecked")
public java.util.List<? extends gov.geoplatform.uasdm.bus.PrivilegeTypeDTO> getResultSet()
{
  return (java.util.List<? extends gov.geoplatform.uasdm.bus.PrivilegeTypeDTO>)super.getResultSet();
}
}