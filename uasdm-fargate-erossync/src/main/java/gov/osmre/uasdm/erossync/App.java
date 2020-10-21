package gov.osmre.uasdm.erossync;

import java.io.IOException;

public class App
{
  public static void main(String[] args) throws IOException
  {
    validate();
    new S3ToFTPCopier(System.getenv("EROSSYNC_FTP_SERVER"), System.getenv("EROSSYNC_S3_BUCKET")).copyDirectory(System.getenv("EROSSYNC_S3_SOURCE_PATH"), System.getenv("EROSSYNC_FTP_TARGET_PATH"), true);
  }
  
  public static void validate()
  {
    assertEnvNotNull("EROSSYNC_FTP_SERVER");
    assertEnvNotNull("EROSSYNC_S3_BUCKET");
    assertEnvNotNull("EROSSYNC_S3_SOURCE_PATH");
    assertEnvNotNull("EROSSYNC_FTP_TARGET_PATH");
    assertEnvNotNull("EROSSYNC_FTP_USERNAME");
    assertEnvNotNull("EROSSYNC_FTP_PASSWORD");
  }
  
  public static void assertEnvNotNull(String envParam)
  {
    if (System.getenv(envParam) == null || System.getenv(envParam).length() == 0)
    {
      throw new RuntimeException("Expected environment variable [" + envParam + "] to be set. Instead, it was either null or empty.");
    }
  }
}
