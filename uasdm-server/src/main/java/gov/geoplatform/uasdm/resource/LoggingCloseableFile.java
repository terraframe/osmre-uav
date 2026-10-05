package gov.geoplatform.uasdm.resource;

import java.io.File;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.CloseableFile;

public class LoggingCloseableFile extends CloseableFile
{
  private static final Logger logger           = LoggerFactory.getLogger(LoggingCloseableFile.class);

  private static final long   serialVersionUID = -1082071298536822240L;

  public LoggingCloseableFile(File file, boolean deleteOnClose)
  {
    super(file, deleteOnClose);

  }

  public LoggingCloseableFile(File parent, String child, boolean deleteOnClose)
  {
    super(parent, child, deleteOnClose);

  }

  public LoggingCloseableFile(File parent, String child)
  {
    super(parent, child);

  }

  public LoggingCloseableFile(File file)
  {
    super(file);

  }

  public LoggingCloseableFile(String pathname, boolean deleteOnClose)
  {
    super(pathname, deleteOnClose);

  }

  public LoggingCloseableFile(String parent, String child, boolean deleteOnClose)
  {
    super(parent, child, deleteOnClose);

  }

  public LoggingCloseableFile(String parent, String child)
  {
    super(parent, child);

  }

  public LoggingCloseableFile(String pathname)
  {
    super(pathname);

  }

  public LoggingCloseableFile(URI uri, boolean deleteOnClose, boolean deleteParentToo)
  {
    super(uri, deleteOnClose, deleteParentToo);

  }

  public LoggingCloseableFile(URI uri, boolean deleteOnClose)
  {
    super(uri, deleteOnClose);

  }

  public void close()
  {
    if (this.isDeleteOnClose())
    {
      logger.info("Deleting closable file: " + this.getAbsolutePath());
    }

    super.close();
  }

}
