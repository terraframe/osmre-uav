package gov.geoplatform.uasdm.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;

public class LoggedApplicationFileResourceDecorator extends ApplicationFileResourceDecorator
{
  private static final Logger logger = LoggerFactory.getLogger(LoggedApplicationFileResourceDecorator.class);

  public LoggedApplicationFileResourceDecorator(ApplicationFileResource resource)
  {
    super(resource);
  }
  
  @Override
  public void close()
  {
    logger.info("Closing file resource [" + this.getAbsolutePath() + "]");

    super.close();
  }

  @Override
  public void delete()
  {
    logger.info("Deleting file resource [" + this.getAbsolutePath() + "]");

    super.delete();
  }
}
