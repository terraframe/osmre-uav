package gov.geoplatform.uasdm.processing.geolocation;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
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
  
  abstract public GeoLocationValidationResults validate();

  public static GeoLocationValidationResults validate(FileFormat geoLocationFormat, ODMProcessingPayload payload)
  {
    switch(geoLocationFormat)
    {
      case ODM:
        return new ODMGeoLocationFileValidator(payload).validate();
      case RX1R2:
        return new RX1R2GeoLocationFileValidator(payload).validate();
      default:
        throw new UnsupportedOperationException("Invalid format " + geoLocationFormat);
    }
  }
  
  public static void validate(FileFormat geoLocationFormat, ODMProcessingPayload payload, AbstractWorkflowTask task)
  {
    GeoLocationValidationResults results = validate(geoLocationFormat, payload);
    
    results.getErrors().stream().forEach(er -> task.createAction(er, TaskActionType.ERROR));
    
    if (results.hasErrors())
    {
      throw new GeoLocationFileInvalidFormatException("View messages for more information.");
    }
  }
  
}
