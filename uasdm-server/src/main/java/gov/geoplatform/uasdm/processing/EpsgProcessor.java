/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.processing;

import java.io.BufferedReader;
import java.io.FileReader;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.CloseableFile;

public class EpsgProcessor implements Processor
{
  private String line;

  public String getLine()
  {
    return line;
  }

  @Override
  public ProcessResult process(ApplicationFileResource res)
  {
    if (!res.isDirectory())
    {
      // Get the PROJECTION data from the first line of the file
      try (CloseableFile file = res.openNewFile())
      {
        try (BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
          this.line = reader.readLine();
        }

        return ProcessResult.success(this.line);
      }
      catch (Exception e)
      {
        return ProcessResult.fail();
      }
    }

    return ProcessResult.fail();
  }
}