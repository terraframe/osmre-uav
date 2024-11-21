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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.AppProperties;

public class CogTifValidator
{
  private Logger  logger = LoggerFactory.getLogger(CogTifValidator.class);

  StatusMonitorIF monitor;

  public CogTifValidator()
  {
  }

  public CogTifValidator(StatusMonitorIF monitor)
  {
    this.monitor = monitor;
  }

  public static String[] getCogValidatorCommand(String template, String cogFile)
  {
    JsonArray jaCmds = JsonParser.parseString(template).getAsJsonArray();
    String[] cmds = new String[jaCmds.size()];

    for (int i = 0; i < jaCmds.size(); ++i)
    {
      cmds[i] = jaCmds.get(i).getAsString().replace("{cog_file}", cogFile);
    }

    return cmds;
  }

  public boolean isValidCog(ApplicationFileResource res)
  {
    try
    {
      if (AppProperties.isCondaEnabled())
      {
        final List<String> cmd = AppProperties.getCogValidatorCommand(res.getUnderlyingFile().getAbsolutePath());

        if (cmd == null || cmd.size() == 0)
        {
          return res.getName().endsWith(".cog.tif") || res.getName().endsWith(".cog.tiff");
        }
        else
        {
          SystemProcessExecutor exec = new SystemProcessExecutor(this.monitor);
          exec.suppressError("\\/opt\\/conda\\/envs\\/silvimetric.*FutureWarning.*UseExceptions.*warnings.warn\\(");

          if (exec.execute(cmd.toArray(new String[0])))
          {
            return !exec.getStdOut().contains("it is recommended to include internal overviews");
          }
          else
          {
            return false;
          }
        }
      }
      else
      {
        return res.getName().endsWith(".cog.tif") || res.getName().endsWith(".cog.tiff");
      }
    }
    catch (Throwable t)
    {
      logger.info("Error validating cog", t);
    }

    return false;
  }
}
