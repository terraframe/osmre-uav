package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 792727535)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to Imagery.java
 *
 * @author Autogenerated by RunwaySDK
 */
public class ImageryQueryDTO extends gov.geoplatform.uasdm.bus.UasComponentQueryDTO
{
private static final long serialVersionUID = 792727535;

  protected ImageryQueryDTO(String type)
  {
    super(type);
  }

@SuppressWarnings("unchecked")
public java.util.List<? extends gov.geoplatform.uasdm.bus.ImageryDTO> getResultSet()
{
  return (java.util.List<? extends gov.geoplatform.uasdm.bus.ImageryDTO>)super.getResultSet();
}
}