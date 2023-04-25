package gov.geoplatform.uasdm.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.opencsv.exceptions.CsvValidationException;

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

}
