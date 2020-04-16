package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.odm.ODMFacade.CloseablePair;

public class ODMFacadeTest
{

  @Test
  public void testFilterZip() throws IOException, URISyntaxException
  {
    File file = new File(this.getClass().getResource("/small-fix-with-video.zip").toURI());

    final FileResource resource = new FileResource(file);

    try (final CloseablePair result = ODMFacade.filter(resource))
    {
      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(result.getFile())))
      {
        ZipEntry entry;

        while ( ( entry = zis.getNextEntry() ) != null)
        {
          final String filename = entry.getName();
          final String ext = FilenameUtils.getExtension(filename);

          Assert.assertFalse(ext.equals("mp4"));
        }
      }
    }
  }

  @Test
  public void testFilterTarGz() throws IOException, URISyntaxException
  {
    File file = new File(this.getClass().getResource("/small-fix-with-video.tar.gz").toURI());

    final FileResource resource = new FileResource(file);

    try (final CloseablePair result = ODMFacade.filter(resource))
    {
      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(result.getFile())))
      {
        ZipEntry entry;

        while ( ( entry = zis.getNextEntry() ) != null)
        {
          final String filename = entry.getName();
          final String ext = FilenameUtils.getExtension(filename);

          Assert.assertFalse(ext.equals("mp4"));
        }
      }
    }
  }

}
