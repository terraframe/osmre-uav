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
