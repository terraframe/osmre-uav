package gov.geoplatform.uasdm.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class FileTestUtils
{
  public static File getTestResourceFile(URI uri)
  {
    File file = new File(uri);

    String extension = FilenameUtils.getExtension(file.getName());

    if (extension.equals("test"))
    {
      File newFile = new File(file.getParentFile(), file.getName().replace(".test", ""));

      if (!newFile.exists())
      {
        try
        {
          FileUtils.copyFile(file, newFile);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
      return newFile;
    }

    return file;
  }
}
