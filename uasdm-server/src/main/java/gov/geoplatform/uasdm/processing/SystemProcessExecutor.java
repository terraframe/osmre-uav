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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;

public class SystemProcessExecutor
{

  private static final Logger logger = LoggerFactory.getLogger(SystemProcessExecutor.class);
  
  private StatusMonitorIF monitor = null;
  
  private StringBuffer stdOut = null;
  
  private StringBuffer stdErr = null;
  
  private int exitCode;
  
  private Map<String, String> environment = new HashMap<String, String>();
  
  private List<String> suppressedErrors = new ArrayList<String>();
  
  public SystemProcessExecutor(StatusMonitorIF monitor)
  {
    this.monitor = monitor;
  }
  
  public SystemProcessExecutor()
  {
    // When monitor is null, we will throw errors as exceptions.
  }
  
  public SystemProcessExecutor suppressError(String errorRegex)
  {
    this.suppressedErrors.add(errorRegex);
    return this;
  }
  
  public String getStdOut()
  {
    return stdOut.toString().trim();
  }
  
  public String getStdErr()
  {
    return stdErr.toString().trim();
  }
  
  public int getExitCode()
  {
    return this.exitCode;
  }
  
  public SystemProcessExecutor setEnvironment(String key, String value)
  {
    this.environment.put(key, value);
    return this;
  }
  
  public boolean execute(String... commands)
  {
    this.stdOut = new StringBuffer();
    this.stdErr = new StringBuffer();
    
    this.exitCode = -1;
    
    ProcessBuilder processBuilder = new ProcessBuilder();

    processBuilder.command(commands);

//    processBuilder.environment().put("PROJ_DATA", "/home/rrowlands/miniconda3/envs/silvimetric/share/proj");
    processBuilder.environment().putAll(environment);
    
    try
    {
      Process process = processBuilder.start();
      
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      
      Thread stdOutReader = new Thread("SystemProcessExecutor sdOut reader")
      {
        public void run()
        {
          String s = null;
          try
          {
            while ( ( s = stdInput.readLine() ) != null)
            {
              stdOut.append(s + "\n");
            }
          }
          catch (IOException e)
          {
            logger.error("stdOut thread encountered an error while reading output", e);
          }
        }
      };
      stdOutReader.start();
      
      Thread stdErrReader = new Thread("SystemProcessExecutor sdError reader")
      {
        public void run()
        {
          String s = null;
          try
          {
            while ( ( s = stdError.readLine() ) != null)
            {
              stdErr.append(s + "\n");
            }
          }
          catch (IOException e)
          {
            logger.error("stdError thread encountered an error while reading output", e);
          }
        }
      };
      stdErrReader.start();
      
      process.waitFor();
      
      exitCode = process.exitValue();
    }
    catch (Throwable t)
    {
      if (this.monitor != null)
      {
        this.monitor.addError("Interrupted while invoking " + commands[0] + " " + RunwayException.localizeThrowable(t, Locale.US));
      }
      else
      {
        throw new RuntimeException(t);
      }
      logger.info("Interrupted when invoking " + commands[0], t);
      return false;
    }

    if (this.getStdOut().length() > 0)
    {
      logger.info("Invoked " + commands[0] + " with output [" + stdOut.toString() + "].");
    }
    
    this.suppressErrors();
    if (this.getStdErr().length() > 0)
    {
      String msg = "Unexpected error invoking " + commands[0] + " [" + this.getStdErr() + "].";
      if (this.monitor != null)
      {
        this.monitor.addError(msg);
      }
      else
      {
        throw new RuntimeException(msg);
      }
      logger.info(msg);
      return false;
    }
    
    return exitCode == 0;
  }
  
  private void suppressErrors()
  {
    String err = this.getStdErr();
    
    if (err.length() > 0)
    {
      for (String regex : this.suppressedErrors)
      {
        err = err.replaceAll(regex, "");
      }
      
      this.stdErr = new StringBuffer(err.trim());
    }
  }
  
}
