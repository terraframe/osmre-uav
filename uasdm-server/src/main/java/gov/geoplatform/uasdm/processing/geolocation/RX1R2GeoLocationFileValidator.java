package gov.geoplatform.uasdm.processing.geolocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

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
