package gov.geoplatform.uasdm.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.ResourceException;

public class LoggingArchiveFileResource extends ArchiveFileResource
{
  private static final Logger logger = LoggerFactory.getLogger(LoggingArchiveFileResource.class);

  public LoggingArchiveFileResource(ApplicationFileResource archive)
  {
    super(archive);
  }

  @Override
  public CloseableFile extract() throws ResourceException
  {
    CloseableFile file = super.extract();

    try
    {
      logger.info("Extracting archive resource [" + this.getAbsolutePath() + "] to [" + file.getAbsolutePath() + "]");
    }
    catch (Exception e)
    {

    }

    return file;
  }

  @Override
  public void close()
  {
    logger.info("Deleting archive resource [" + this.getAbsolutePath() + "]");

    if (this.extractedParent != null)
    {
      logger.info("Deleting extracted parent [" + this.extractedParent.getAbsolutePath() + "]");
    }

    super.close();

    if (this.archive != null && this.archive.exists())
    {
      logger.error("Delete of archive [" + this.archive.getAbsolutePath() + "] failed");
    }

    if (this.extractedParent != null && this.extractedParent.exists())
    {
      logger.error("Delete of extracted parent [" + this.extractedParent.getAbsolutePath() + "] failed");
    }
  }

  @Override
  public void delete()
  {
    logger.info("Deleting archive resource [" + this.getAbsolutePath() + "]");

    if (this.extractedParent != null)
    {
      logger.info("Deleting extracted parent [" + this.extractedParent.getAbsolutePath() + "]");
    }

    super.delete();

    if (this.archive != null && this.archive.exists())
    {
      logger.error("Delete of archive [" + this.archive.getAbsolutePath() + "] failed");
    }

    if (this.extractedParent != null && this.extractedParent.exists())
    {
      logger.error("Delete of extracted parent [" + this.extractedParent.getAbsolutePath() + "] failed");
    }
  }

}
