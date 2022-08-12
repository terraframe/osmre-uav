package gov.geoplatform.uasdm.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class FileTestUtils
{
  public static File createZip(URI uri) throws IOException
  {
    return createZip(uri, "test.zip");
  }

  public static File createZip(URI uri, String name) throws IOException
  {
    File directory = new File(uri);
    File file = new File(directory.getParentFile(), name);

    if (!file.exists())
    {
      Path sourceFolderPath = Paths.get(uri);
      Path zipFilePath = Paths.get(file.toURI());

      // This baseFolderPath is upto the parent folder of sourceFolder.
      // Used to remove nesting of parent folder inside zip
//      Path baseFolderPath = Paths.get(sourceFolderName.substring(0, sourceFolderName.indexOf(sourceFolderPath.getFileName().toString())));

      try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile())))
      {
        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>()
        {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
          {
            zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(dir) + File.separator));
            zos.closeEntry();
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
          {
            zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
            Files.copy(file, zos);
            zos.closeEntry();
            return FileVisitResult.CONTINUE;
          }
        });
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    return file;
  }

  public static File createTarGz(URI uri) throws IOException
  {
    Path dir = Paths.get(uri);
    File directory = new File(uri);
    File file = new File(directory.getParentFile(), "test.tar.gz");

    if (!file.exists())
    {
      try (BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(file)))
      {
        GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);

        try (TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut))
        {
          Files.list(dir).forEach(path -> {
            try
            {

              if (!Files.isRegularFile(path))
              {
                throw new IOException("Support only file!");
              }

              TarArchiveEntry tarEntry = new TarArchiveEntry(path.toFile(), path.getFileName().toString());

              tOut.putArchiveEntry(tarEntry);

              // copy file to TarArchiveOutputStream
              Files.copy(path, tOut);

              tOut.closeArchiveEntry();
            }
            catch (IOException e)
            {
              e.printStackTrace();
            }

          });

          tOut.finish();
        }
      }
    }

    return file;
  }
}
