/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
