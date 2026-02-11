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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.opencsv.CSVReader;

import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;

public class RX1R2GeoLocationFileValidator extends GeoLocationFileValidator
{
  public RX1R2GeoLocationFileValidator(ODMProcessingPayload payload)
  {
    super(payload);
  }
  
  @Override
  public GeoLocationValidationResults validate()
  {
    GeoLocationValidationResults results = new GeoLocationValidationResults();
    
    try (CSVReader csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(this.payload.getGeoLocationFile().getBytes()))))
    {
      String[] line;
      int num = 1;

      // we are going to read data line by line
      while ( ( line = csvReader.readNext() ) != null)
      {
        if (line.length >= 4)
        {
          String fileName = line[0];
          
          if (!payload.getImageNames().contains(fileName))
          {
            results.addError("Line [" + num + "] references an image which does not exist. Image names must match exactly and are case sensitive.");
          }
          
          try
          {
            BigDecimal latitude = new BigDecimal(line[1]);
            
            if (latitude.compareTo(new BigDecimal(90)) > 0 || latitude.compareTo(new BigDecimal(-90)) < 0)
            {
              results.addError("Line [" + num + "] contains a latitude which is out of bounds. Latitude must be between -90 and 90.");
            }
          }
          catch (Throwable t)
          {
            results.addError("Line [" + num + "] contains a non-numeric latitude.");
          }
          
          try
          {
            BigDecimal longitude = new BigDecimal(line[2]);
            
            if (longitude.compareTo(new BigDecimal(180)) > 0 || longitude.compareTo(new BigDecimal(-180)) < 0)
            {
              results.addError("Line [" + num + "] contains a longitude which is out of bounds. Longitude must be between -180 and 180.");
            }
          }
          catch (Throwable t)
          {
            results.addError("Line [" + num + "] contains a non-numeric longitude.");
          }
          
          try
          {
            new BigDecimal(line[3]);
          }
          catch (Throwable t)
          {
            results.addError("Line [" + num + "] contains a non-numeric value.");
          }
        }
        else
        {
          results.addError("Expected at least 4 values on line [" + num + "].");
        }
        
        num++;
      }
    }
    catch (Throwable t)
    {
      throw new GeoLocationFileInvalidFormatException(t.getMessage(), t);
    }
    
    return results;
  }
}
