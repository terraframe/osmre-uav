/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.google.common.io.Files;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.InvalidZipException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class ODMFacade
{
  public static class CloseablePair implements AutoCloseable
  {
    private CloseableFile file;

    private int           itemCount;

    public CloseablePair(CloseableFile file, int itemCount)
    {
      super();
      this.file = file;
      this.itemCount = itemCount;
    }

    public CloseableFile getFile()
    {
      return file;
    }

    public int getItemCount()
    {
      return itemCount;
    }

    public void close()
    {
      this.file.close();
    }
  }

  private static ODMServiceIF service = new ODMService();

  public synchronized static void setService(ODMServiceIF service)
  {
    ODMFacade.service = service;
  }

  public static TaskOutputResponse taskOutput(String uuid)
  {
    return service.taskOutput(uuid);
  }

  public static TaskRemoveResponse taskRemove(String uuid)
  {
    return service.taskRemove(uuid);
  }

  public static CloseableFile taskDownload(String uuid)
  {
    return service.taskDownload(uuid);
  }

  /*
   * Uploads the zip to ODM using the init / upload / commit paradigm, which is
   * recommended for large file uploads.
   * 
   * https://github.com/OpenDroneMap/NodeODM/blob/master/docs/index.adoc#post-
   * tasknewinit
   */
  public static NewResponse taskNew(ApplicationResource images, boolean isMultispectral, ODMProcessConfiguration configuration)
  {
    return service.taskNew(images, isMultispectral, configuration);
  }

  public static NewResponse taskNewInit(int imagesCount, boolean isMultispectral)
  {
    return service.taskNewInit(imagesCount, isMultispectral);
  }

  public static ODMResponse taskNewUpload(String uuid, ApplicationResource image)
  {
    return service.taskNewUpload(uuid, image);
  }

  public static ODMResponse taskNewCommit(String uuid)
  {
    return service.taskNewCommit(uuid);
  }

  public static InfoResponse taskInfo(String uuid)
  {
    return service.taskInfo(uuid);
  }

  public static CloseablePair filterAndExtract(ApplicationResource archive, ODMProcessConfiguration configuration) throws IOException
  {
    String extension = archive.getNameExtension();

    if (extension.equalsIgnoreCase("zip"))
    {
      return filterZipArchive(archive, configuration);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      return filterTarGzArchive(archive, configuration);
    }

    throw new ProgrammingErrorException(new UnsupportedOperationException("Unsupported archive type [" + extension + "]"));
  }

  private static CloseablePair filterTarGzArchive(ApplicationResource archive, ODMProcessConfiguration configuration) throws IOException
  {
    List<String> extensions = ImageryProcessingJob.getSupportedExtensions(ImageryComponent.RAW);

    CloseableFile parent = new CloseableFile(Files.createTempDir(), true);
    int itemCount = 0;

    try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(archive.openNewStream()))
    {
      try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn))
      {
        TarArchiveEntry entry;

        while ( ( entry = (TarArchiveEntry) tarIn.getNextEntry() ) != null)
        {
          final String filename = entry.getName();
          final String ext = FilenameUtils.getExtension(filename);

          if (entry.isDirectory())
          {
          }
          else if ((UasComponentIF.isValidName(filename) && extensions.contains(ext)) 
              || (filename.equalsIgnoreCase("geo.txt") && configuration.isIncludeGeoLocationFile()))
          {
            File file = new File(parent, entry.getName());

            try (FileOutputStream fos = new FileOutputStream(file))
            {
              IOUtils.copy(tarIn, fos);
            }

            itemCount++;
          }

        }
      }
    }

    return new CloseablePair(parent, itemCount);
  }

  private static CloseablePair filterZipArchive(ApplicationResource archive, ODMProcessConfiguration configuration) throws IOException
  {
    List<String> extensions = ImageryProcessingJob.getSupportedExtensions(ImageryComponent.RAW);
    CloseableFile parent = new CloseableFile(Files.createTempDir(), true);
    int itemCount = 0;

    byte[] buffer = new byte[Util.BUFFER_SIZE];

    try (CloseableFile fArchive = archive.openNewFile())
    {
      try (ZipFile zipFile = new ZipFile(fArchive))
      {
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

        while (entries.hasMoreElements())
        {
          ZipArchiveEntry entry = entries.nextElement();
          final String filename = entry.getName();
          final String ext = FilenameUtils.getExtension(filename).toLowerCase();

          if ((UasComponentIF.isValidName(filename) && extensions.contains(ext)) 
              || (filename.equalsIgnoreCase("geo.txt") && configuration.isIncludeGeoLocationFile()))
          {
            int len;

            File file = new File(parent, entry.getName());

            try (FileOutputStream fos = new FileOutputStream(file))
            {
              try (InputStream zis = zipFile.getInputStream(entry))
              {
                while ( ( len = zis.read(buffer) ) > 0)
                {
                  fos.write(buffer, 0, len);
                }
              }
            }

            itemCount++;
          }
        }
      }
      catch (ZipException e)
      {
        throw new InvalidZipException();
      }
    }

    return new CloseablePair(parent, itemCount);
  }

}
