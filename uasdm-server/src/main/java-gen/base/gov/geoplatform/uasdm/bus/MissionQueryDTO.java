package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -1384856950)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to Mission.java
 *
 * @author Autogenerated by RunwaySDK
 */
public class MissionQueryDTO extends gov.geoplatform.uasdm.bus.UasComponentQueryDTO
{
private static final long serialVersionUID = -1384856950;

  protected MissionQueryDTO(String type)
  {
    super(type);
  }

@SuppressWarnings("unchecked")
public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getResultSet()
{
  return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>)super.getResultSet();
}
}