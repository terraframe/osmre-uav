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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileInvalidFormatException;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileValidator;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationValidationResults;
import gov.geoplatform.uasdm.processing.geolocation.RX1R2GeoFileConverter;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class RX1R2GeoFileConverterTest
{
  @Test
  public void testConvert() throws URISyntaxException, CsvValidationException, FileNotFoundException, IOException
  {
    URL url = this.getClass().getResource("/PIX4D.CSV");

    File file = new File(url.toURI());

    try (RX1R2GeoFileConverter reader = RX1R2GeoFileConverter.open(file))
    {
      try (BufferedReader output = new BufferedReader(new FileReader(reader.getOutput())))
      {
        Assert.assertEquals("EPSG:4326", output.readLine());
        Assert.assertEquals("DSC00001.jpg\t-92.5672988\t47.6537057\t589.32", output.readLine());

      }
    }

  }

  @Test
  public void testValidate()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new ArchiveFileResource(new FileResource(new CloseableFile(""))));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("DSC00001.jpg,47.6537057,-092.5672988,0589.32\nDSC00002.jpg,47.6537057,-092.5672988,0589.32\nDSC00003.jpg,47.6537057,-092.5672988,0589.32");
    validateThrowErrors(payload);
  }
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateBadCase()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new ArchiveFileResource(new FileResource(new CloseableFile(""))));
    
    payload.addImage("DSC00001.JPG");
    payload.addImage("DSC00002.JPG");
    payload.addImage("DSC00003.JPG");
    
    payload.setGeoLocationFile("DSC00001.jpg,47.6537057,-092.5672988,0589.32\nDSC00002.jpg,47.6537057,-092.5672988,0589.32\nDSC00003.jpg,47.6537057,-092.5672988,0589.32");
    validateThrowErrors(payload);
  }
  
  /*
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateMissingFile()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new CloseableFile(""));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("DSC00001.jpg,47.6537057,-092.5672988,0589.32\nDSC00002.jpg,47.6537057,-092.5672988,0589.32");
    validateThrowErrors(payload);
  }
  */
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateLatBounds()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new ArchiveFileResource(new FileResource(new CloseableFile(""))));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("DSC00001.jpg,-092.5672988,47.6537057,0589.32\nDSC00002.jpg,47.6537057,-092.5672988,0589.32\nDSC00003.jpg,47.6537057,-092.5672988,0589.32");
    validateThrowErrors(payload);
  }
  
  @Test(expected = GeoLocationFileInvalidFormatException.class)
  public void testValidateLongBounds()
  {
    ODMProcessingPayload payload = new ODMProcessingPayload(new ArchiveFileResource(new FileResource(new CloseableFile(""))));
    
    payload.addImage("DSC00001.jpg");
    payload.addImage("DSC00002.jpg");
    payload.addImage("DSC00003.jpg");
    
    payload.setGeoLocationFile("DSC00001.jpg,47.6537057,-0192.5672988,0589.32\nDSC00002.jpg,47.6537057,-092.5672988,0589.32\nDSC00003.jpg,47.6537057,-092.5672988,0589.32");
    validateThrowErrors(payload);
  }
  
  private void validateThrowErrors(ODMProcessingPayload payload)
  {
    GeoLocationValidationResults results = GeoLocationFileValidator.validate(FileFormat.RX1R2, payload);
    
    if (results.hasErrors())
    {
      throw new GeoLocationFileInvalidFormatException(StringUtils.join(results.getErrors(), ", "));
    }
  }
}
