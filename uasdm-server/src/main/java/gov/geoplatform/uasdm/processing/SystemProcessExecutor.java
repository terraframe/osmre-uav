package gov.geoplatform.uasdm.processing;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

public class SystemProcessExecutor
{

  private static final Logger logger = LoggerFactory.getLogger(SystemProcessExecutor.class);
  
  private StatusMonitorIF monitor;
  
  private StringBuilder stdOut = null;
  
  private StringBuilder stdErr = null;
  
  public SystemProcessExecutor(StatusMonitorIF monitor)
  {
    this.monitor = monitor;
  }
  
  public String getStdOut()
  {
    return stdOut.toString().trim();
  }
  
  public String getStdErr()
  {
    return stdErr.toString().trim();
  }
  
  public boolean execute(String[] commands)
  {
    final Runtime rt = Runtime.getRuntime();

    this.stdOut = new StringBuilder();
    this.stdErr = new StringBuilder();
    
    Thread t = new Thread()
    {
      public void run()
      {
        try
        {
          Process proc = rt.exec(commands);

          BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

          BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

          // read the output from the command
          String s = null;
          while ( ( s = stdInput.readLine() ) != null)
          {
            stdOut.append(s + "\n");
          }

          // read any errors from the attempted command
          while ( ( s = stdError.readLine() ) != null)
          {
            stdErr.append(s + "\n");
          }
          
        }
        catch (Throwable t)
        {
          logger.error("Error occured while invoking system process.", t);
        }
      }
    };
    t.start();

    try
    {
      t.join(10000);
    }
    catch (InterruptedException e)
    {
      if (this.monitor != null)
      {
        this.monitor.addError("Interrupted when invoking system process.");
      }
      logger.info("Interrupted when invoking system process", e);
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
      logger.info(msg);
      return false;
    }
    
    return true;
  }
  
}
