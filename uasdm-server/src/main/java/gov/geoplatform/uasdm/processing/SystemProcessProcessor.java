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
  
  public SystemProcessProcessor(String filename, AbstractWorkflowTask progressTask, Product product, CollectionIF collection, String s3FolderName, boolean searchable)
  {
    super(filename, progressTask, product, collection, s3FolderName, searchable);
  }
  
  protected void executeProcess(String[] commands)
  {
    final Runtime rt = Runtime.getRuntime();

    StringBuilder stdOut = new StringBuilder();
    StringBuilder stdErr = new StringBuilder();

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
          logger.error("Error occured while processing dem file with gdal.", t);
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
      logger.error("Interrupted when processing dem file with gdal", e);
    }

    if (stdOut.toString().trim().length() > 0)
    {
      logger.info("Processed transform with gdal [" + stdOut.toString() + "].");
    }

    if (stdErr.toString().trim().length() > 0)
    {
      logger.error("Unexpected error while processing gdal transform [" + stdErr.toString() + "].");
    }
  }
  
}
