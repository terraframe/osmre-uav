package gov.geoplatform.uasdm.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.AppProperties;

public class CogTifValidator
{
  private Logger logger = LoggerFactory.getLogger(CogTifValidator.class);
  
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
      final String template = AppProperties.getCogValidatorCommand();
      
      if (template == null || template.length() == 0)
      {
        return res.getName().endsWith(".cog.tif") || res.getName().endsWith(".cog.tiff");
      }
      else
      {
        final String[] cmds = getCogValidatorCommand(template, res.getUnderlyingFile().getAbsolutePath());
        
        SystemProcessExecutor exec = new SystemProcessExecutor(this.monitor);
        
        if (exec.execute(cmds))
        {
          return !exec.getStdOut().contains("it is recommended to include internal overviews");
        }
        else
        {
          return false;
        }
      }
    }
    catch(Throwable t)
    {
      logger.info("Error validating cog", t);
    }
    
    return false;
  }
}
