package gov.geoplatform.uasdm.processing.geolocation;

import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;

/**
 * 
 * @author rrowlands
 *
 */
abstract public class GeoLocationFileValidator
{
  
  protected ODMProcessingPayload payload;
  
  public GeoLocationFileValidator(ODMProcessingPayload payload)
  {
    this.payload = payload;
  }
  
  abstract public void validate();

  public static void validate(FileFormat geoLocationFormat, ODMProcessingPayload payload)
  {
    switch(geoLocationFormat)
    {
      case ODM:
        new ODMGeoLocationFileValidator(payload).validate();
        break;
      case RX1R2:
        new RX1R2GeoLocationFileValidator(payload).validate();
        break;
      default:
        throw new UnsupportedOperationException("Invalid format " + geoLocationFormat);
    }
  }
  
}
