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

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.opencsv.CSVReader;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;

import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;

public class RX1R2GeoLocationFileValidator extends GeoLocationFileValidator
{
  public RX1R2GeoLocationFileValidator(FileFormat geoLocationFormat, ApplicationFileResource geoLocationFile, Set<String> imageNames)
  {
    super(geoLocationFormat, geoLocationFile, imageNames);
  }
  
  @Override
  public GeoLocationValidationResults validate()
  {
    GeoLocationValidationResults results = new GeoLocationValidationResults();
    
    Set<String> geoLocFilenames = new HashSet<String>();
    
    // Step 1. Read the file, validate each line.
    try (CSVReader csvReader = new CSVReader(new InputStreamReader(this.geoLocationFile.openNewStream())))
    {
      String[] line;
      int num = 1;

      // we are going to read data line by line
      while ( ( line = csvReader.readNext() ) != null)
      {
        // First line might be a SRS
        if (num == 1 && line.length == 1 && line[0].trim().toUpperCase().startsWith("EPSG")) {
          continue;
        }
        
        if (line.length >= 4)
        {
          String fileName = line[0];
          
          geoLocFilenames.add(fileName.toLowerCase());
          
          if (!imageNames.containsKey(fileName.toLowerCase()))
          {
            results.addWarning("Line [" + num + "] references an image [" + fileName + "] which does not exist in the collection. Image names are not case sensitive.");
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
    
    // Step 2. Check for images in the collection which aren't in the geo location file. This is a hard error, since it wouldn't have coordinates.
    for (String lowerName : imageNames.keySet()) {
      if (!geoLocFilenames.contains(lowerName)) {
        results.addError("The geo location file did not include a reference to " + imageNames.get(lowerName) + " (an image in your collection). Processing the collection would cause this image to not have a valid geo location.");
      }
    }
    
    return results;
  }
}
