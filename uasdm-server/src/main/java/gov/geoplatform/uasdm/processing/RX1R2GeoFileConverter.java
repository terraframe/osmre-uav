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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.apache.commons.io.FileUtils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import gov.geoplatform.uasdm.GenericException;

public class RX1R2GeoFileConverter implements AutoCloseable
{
  private InputStream input;

  private File        output;

  public RX1R2GeoFileConverter(InputStream input)
  {
    this.input = input;
  }

  @Override
  public void close()
  {
    if (this.output != null)
    {
      FileUtils.deleteQuietly(this.output);
    }
  }

  public static RX1R2GeoFileConverter open(File input) throws FileNotFoundException
  {
    RX1R2GeoFileConverter reader = new RX1R2GeoFileConverter(new FileInputStream(input));

    return reader;
  }

  public static RX1R2GeoFileConverter open(InputStream input)
  {
    RX1R2GeoFileConverter reader = new RX1R2GeoFileConverter(input);

    return reader;
  }

  public File getOutput() throws IOException, CsvValidationException
  {
    this.output = File.createTempFile("geo", ".txt");
    this.output.deleteOnExit();

    try (FileWriter writer = new FileWriter(this.output))
    {
      writer.write("EPSG:4326");
      writer.write("\n");

      try (CSVReader csvReader = new CSVReader(new InputStreamReader(new BufferedInputStream(this.input))))
      {

        String[] line;

        // we are going to read data line by line
        while ( ( line = csvReader.readNext() ) != null)
        {
          if (line.length == 4)
          {
            String fileName = line[0];
            BigDecimal geoY = new BigDecimal(line[1]);
            BigDecimal geoX = new BigDecimal(line[2]);
            BigDecimal geoZ = new BigDecimal(line[3]);

            writer.write(fileName);
            writer.write("\t");

            writer.write(geoX.toPlainString());
            writer.write("\t");

            writer.write(geoY.toPlainString());
            writer.write("\t");

            writer.write(geoZ.toPlainString());
            writer.write("\n");
          }
          else
          {
//            GenericException exception = new GenericException();
//            exception.setUserMessage("Unable to parse geo logger file: Unexpected number of columns");
//            throw exception;
          }
        }
      }
    }

    return this.output;
  }

}
