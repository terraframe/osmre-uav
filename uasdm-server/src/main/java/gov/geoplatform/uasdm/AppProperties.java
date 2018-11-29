package gov.geoplatform.uasdm;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class AppProperties
{
  private static AppProperties instance;

  private Properties           props;

  private AppProperties()
  {
    this.props = new Properties();

    try
    {
      this.props.load(this.getClass().getResourceAsStream("/app.properties"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static synchronized AppProperties instance()
  {
    if (instance == null)
    {
      instance = new AppProperties();
    }

    return instance;
  }
  
  public static String getBucketName()
  {
    return instance().props.getProperty("bucket.name");
  }

  public static File getTempDirectory()
  {
    return new File(instance().props.getProperty("temp.dir"));
  }

  public static File getUploadDirectory()
  {
    return new File(instance().props.getProperty("upload.dir"));
  }

  public static Integer getChunkExpireTime()
  {
    return new Integer(instance().props.getProperty("chunk.expire.time"));
  }

}
