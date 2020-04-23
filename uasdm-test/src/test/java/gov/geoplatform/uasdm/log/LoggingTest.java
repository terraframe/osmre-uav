package gov.geoplatform.uasdm.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTest
{
  private static Logger logger = LoggerFactory.getLogger(LoggingTest.class);

  public static void main(String[] args)
  {
    logger.debug("Debug log message");
    logger.info("Info log message");
    logger.error("Error log message");
  }
}
