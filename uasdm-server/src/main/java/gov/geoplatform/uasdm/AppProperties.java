package gov.geoplatform.uasdm;

import java.io.File;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;
import com.runwaysdk.constants.DeployProperties;

public class AppProperties
{
  /**
   * The server.properties configuration file
   */
  private ConfigurationReaderIF props;

  private AppProperties()
  {
    this.props = ConfigurationManager.getReader(UasdmConfigGroup.COMMON, "app.properties");
  }

  private static class Singleton
  {
    private static AppProperties INSTANCE = new AppProperties();

    private static AppProperties getInstance()
    {
      // INSTANCE will only ever be null if there is a problem. The if check is
      // to allow for debugging.
      if (INSTANCE == null)
      {
        INSTANCE = new AppProperties();
      }

      return INSTANCE;
    }

    private static ConfigurationReaderIF getProps()
    {
      return getInstance().props;
    }
  }

  public static String getBucketName()
  {
    return Singleton.getProps().getString("bucket.name");
  }

  public static String getBucketRegion()
  {
    return Singleton.getProps().getString("bucket.region");
  }

  public static String getOdmUrl()
  {
    return Singleton.getProps().getString("odm.url");
  }

  public static String getOdmUsername()
  {
    return Singleton.getProps().getString("odm.username");
  }

  public static String getOdmPassword()
  {
    return Singleton.getProps().getString("odm.password");
  }

  public static String getSolrUrl()
  {
    return Singleton.getProps().getString("solr.url");
  }

  public static Boolean isSolrEnabled()
  {
    return Singleton.getProps().getBoolean("solr.enabled");
  }

  public static File getTempDirectory()
  {
    return new File(DeployProperties.getDeployPath() + File.separator + Singleton.getProps().getString("temp.dir"));
  }

  public static File getUploadDirectory()
  {
    return new File(DeployProperties.getDeployPath() + File.separator + Singleton.getProps().getString("upload.dir"));
  }

  public static Integer getChunkExpireTime()
  {
    return Singleton.getProps().getInteger("chunk.expire.time");
  }
  
  public static Integer getInviteUserTokenExpireTime()
  {
    return Singleton.getProps().getInteger("invite.user.token.expire.time", 72);
  }

}
