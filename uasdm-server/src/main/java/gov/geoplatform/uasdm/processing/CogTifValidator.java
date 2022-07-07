package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  public boolean isValidCog(File file)
  {
    try
    {
      final String template = AppProperties.getCogValidatorCommand();
      
      final String cmd = template.replace("{cog_file}", file.getAbsolutePath());
      
      return new SystemProcessExecutor(this.monitor).execute(cmd.split(" "));
    }
    catch(Throwable t)
    {
      logger.info("Error validating cog", t);
    }
    
    return false;
  }
}
