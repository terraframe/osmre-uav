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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.AppProperties;

public class CopcLazValidator
{
  private Logger  logger = LoggerFactory.getLogger(CopcLazValidator.class);

  StatusMonitorIF monitor;

  public CopcLazValidator()
  {
  }

  public CopcLazValidator(StatusMonitorIF monitor)
  {
    this.monitor = monitor;
  }
  
  public boolean isValidLaz(ApplicationFileResource res)
  {
    ApplicationFileResource resource = ResourceUtil.unpackResource(res);
    
    try
    {
      var cmd = AppProperties.getCondaTool("pdal");
      cmd.addAll(Arrays.asList(new String[] { "info", resource.getAbsolutePath() }));
      
      boolean success = new SystemProcessExecutor(this.monitor)
          .setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath())
          .execute(cmd.toArray(new String[0]));

      return success;
    }
    catch (Throwable t)
    {
      logger.info("Error validating laz", t);
    }

    return false;
  }

  public boolean isValidCopc(ApplicationFileResource res)
  {
    ApplicationFileResource resource = ResourceUtil.unpackResource(res);
    
    try
    {
      var cmd = AppProperties.getCondaTool("pdal");
      cmd.addAll(Arrays.asList(new String[] { "info", "--metadata", resource.getAbsolutePath() }));
      
      var executor = new SystemProcessExecutor(this.monitor).setEnvironment("PROJ_DATA", AppProperties.getSilvimetricProjDataPath());
      
      if (!executor.execute(cmd.toArray(new String[0])))
        return false;

      String out = executor.getStdOut();
      if (out == null || out.isBlank())
        return false;

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(out);

      // PDAL emits { "metadata": { ... } } for --metadata
      JsonNode copcNode = root.path("metadata").path("copc");

      // "valid COPC" if metadata.copc exists and isn't null/missing
      return !copcNode.isMissingNode() && !copcNode.isNull();
    }
    catch (Throwable t)
    {
      logger.info("Error validating copc laz", t);
    }

    return false;
  }
}
