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
import java.util.Locale;

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
  
  public SystemProcessExecutor(StatusMonitorIF monitor)
  {
    this.monitor = monitor;
  }
  
  public SystemProcessExecutor()
  {
    // When monitor is null, we will throw errors as exceptions.
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
  
  public boolean execute(String... commands)
  {
    final Runtime rt = Runtime.getRuntime();

    this.stdOut = new StringBuffer();
    this.stdErr = new StringBuffer();
    
    this.exitCode = -1;
    
    try
    {
      Process process = rt.exec(commands);
      
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
        this.monitor.addError("Interrupted when invoking system process. " + RunwayException.localizeThrowable(t, Locale.US));
      }
      else
      {
        throw new RuntimeException(t);
      }
      logger.info("Interrupted when invoking system process", t);
      return false;
    }

    if (this.getStdOut().length() > 0)
    {
      logger.info("Invoked system process with output [" + stdOut.toString() + "].");
    }

    if (this.getStdErr().length() > 0)
    {
      String msg = "Unexpected error invoking system process [" + this.getStdErr() + "].";
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
  
}
