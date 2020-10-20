package gov.osmre.uasdm.erossync;

import java.io.IOException;

public class App
{
  public static void main(String[] args) throws IOException {
    new S3ToFTPCopier("ftp.terraframe.com", "terraframe-test-bucket").copyDirectory("", "eros/test", true);
  }
}
