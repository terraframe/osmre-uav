package gov.geoplatform.uasdm.processing;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;

abstract public class SystemProcessProcessor extends ManagedDocument
{

  private static final Logger logger = LoggerFactory.getLogger(SystemProcessProcessor.class);
  
  private StringBuilder stdOut = null;
  
  private StringBuilder stdErr = null;
  
  public SystemProcessProcessor(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor, boolean searchable)
  {
    super(s3Path, product, collection, monitor, searchable);
  }
  
  public String getStdOut()
  {
    return stdOut.toString().trim();
  }
  
  public String getStdErr()
  {
    return stdErr.toString().trim();
  }
  
  protected boolean executeProcess(String[] commands)
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
      this.monitor.addError("Interrupted when invoking system process.");
      logger.info("Interrupted when invoking system process", e);
      return false;
    }

    if (this.getStdOut().length() > 0)
    {
      logger.info("Invoked system process with output [" + stdOut.toString() + "].");
    }

    if (this.getStdErr().length() > 0)
    {
      this.monitor.addError("Unexpected error invoking system process [" + this.getStdErr() + "].");
      return false;
    }
    
    return true;
  }
  
}
