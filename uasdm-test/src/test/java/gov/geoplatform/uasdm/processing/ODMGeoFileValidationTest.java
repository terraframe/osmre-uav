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
package gov.geoplatform.uasdm.processing;

import org.junit.Test;

import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileInvalidFormatException;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileValidator;

public class ODMGeoFileValidationTest
{
  @Test
  public void testValidate()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("EPSG:4326\nDSC00001.jpg    47.6537057  -092.5672988    0589.32\nDSC00002.jpg   47.6537057  -092.5672988    0589.32\nDSC00003.jpg   47.6537057  -092.5672988    0589.32");
    GeoLocationFileValidator.validate(FileFormat.ODM, payload);
  }
  
  @Test
  public void testValidateWhitespace()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("EPSG:4326\nDSC00001.jpg	47.6537057  -092.5672988	0589.32\nDSC00002.jpg   47.6537057  -092.5672988	0589.32\nDSC00003.jpg   47.6537057  -092.5672988	0589.32");
    GeoLocationFileValidator.validate(FileFormat.ODM, payload);
  }
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testNoEPSG()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("DSC00001.jpg    47.6537057  -092.5672988    0589.32\\nDSC00002.jpg   47.6537057  -092.5672988    0589.32\\nDSC00003.jpg   47.6537057  -092.5672988    0589.32");
    GeoLocationFileValidator.validate(FileFormat.ODM, payload);
  }
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateBadCase()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.JPG");
    payload.addImage("DSC00002.JPG");
    payload.addImage("DSC00003.JPG");
    
    payload.setGeoLocationFile("EPSG:4326\nDSC00001.jpg 47.6537057 -092.5672988 0589.32\nDSC00002.jpg 47.6537057 -092.5672988 0589.32\nDSC00003.jpg 47.6537057 -092.5672988 0589.32");
    GeoLocationFileValidator.validate(FileFormat.ODM, payload);
  }
  
  /*
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateMissingFile()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("DSC00001.jpg 47.6537057 -092.5672988 0589.32\nDSC00002.jpg 47.6537057 -092.5672988 0589.32");
    GeoLocationFileValidator.validate(FileFormat.RX1R2, payload);
  }
  */
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateLatBounds()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("EPSG:4326\nDSC00001.jpg -092.5672988 47.6537057 0589.32\nDSC00002.jpg 47.6537057 -092.5672988 0589.32\nDSC00003.jpg 47.6537057 -092.5672988 0589.32");
    GeoLocationFileValidator.validate(FileFormat.ODM, payload);
  }
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateLongBounds()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("EPSG:4326\nDSC00001.jpg 47.6537057 -0192.5672988 0589.32\nDSC00002.jpg 47.6537057 -092.5672988 0589.32\nDSC00003.jpg 47.6537057 -092.5672988 0589.32");
    GeoLocationFileValidator.validate(FileFormat.ODM, payload);
  }
}