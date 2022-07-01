package com.runwaysdk.build.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.session.Request;

/**
 * Pulls all tifs from S3 and reuploads them as cog tiffs.
 * 
 * @author rich
 */
public class CogTiffPatcher
{
  private static final Logger logger = LoggerFactory.getLogger(CogTiffPatcher.class);
  
  public static void main(String[] args)
  {
    new CogTiffPatcher().doIt();
  }
  
  public CogTiffPatcher()
  {
    
  }
  
  @Request
  public void doIt()
  {
    File ortho = new File("/home/rich/dev/projects/uasdm/data/cogtiff/necedah_9thave_wetland/temp/non-cog-ortho.tif");
    
    final String basename = FilenameUtils.getBaseName(ortho.getName());
    
    FileUtils.deleteQuietly(new File(ortho.getParent(), basename + "-overview.tif"));
    FileUtils.deleteQuietly(new File(ortho.getParent(), basename + "-cog.tif"));
    
    processFile(ortho);
  }
  
  public void processFile(File file)
  {
    final String basename = FilenameUtils.getBaseName(file.getName());

    File overview = new File(file.getParent(), basename + "-overview.tif");
    try
    {
      FileUtils.copyFile(file, overview);
    }
    catch (IOException e)
    {
      logger.error("Error copying file. Cog generation failed for file [" + file.getAbsolutePath() + "].");
      return;
    }

    if (!this.executeProcess(new String[] {
        "gdaladdo", "-r", "average", overview.getAbsolutePath(), "2", "4", "8", "16"
      }))
    {
      logger.error("Problem occurred generating overview file. Cog generation failed for file [" + file.getAbsolutePath() + "].");
      return;
    }
    
    // TODO : Re-run it if it fails?
    
    File cog = new File(file.getParent(), basename + "-cog.tif");
    
    if (!this.executeProcess(new String[] {
        "gdal_translate", overview.getAbsolutePath(), cog.getAbsolutePath(), "-co", "COMPRESS=LZW", "-co", "TILED=YES", "-co", "COPY_SRC_OVERVIEWS=YES"
      }))
    {
      logger.error("Problem occurred generating cog file. Cog generation failed for file [" + file.getAbsolutePath() + "].");
      return;
    }
  }
  
  protected boolean executeProcess(String[] commands)
  {
    boolean success = true;
    
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
      success = false;
    }

    if (stdOut.toString().trim().length() > 0)
    {
      logger.info("Processed transform with gdal [" + stdOut.toString() + "].");
    }

    if (stdErr.toString().trim().length() > 0)
    {
      logger.error("Unexpected error while processing gdal transform [" + stdErr.toString() + "].");
      success = false;
    }
    
    return success;
  }
}
