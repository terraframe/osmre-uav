/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.io.Files;
import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.InvalidZipException;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.RadiometricCalibration;
import gov.geoplatform.uasdm.processing.WorkswellWirisThermalPhotometricProcessor;
import gov.geoplatform.uasdm.processing.geolocation.RX1R2GeoFileConverter;

public class ODMFacade
{
  public static class ODMProcessingPayload implements AutoCloseable
  {
    private CloseableFile file;

    private Set<String>   imageNames = new HashSet<String>();
    
    private String        geoLocationFile;

    private String        groundControlPointFile;

    public ODMProcessingPayload(CloseableFile file)
    {
      this.file = file;
    }
    
    public void setGeoLocationFile(String geoLocationFile)
    {
      this.geoLocationFile = geoLocationFile;
    }
    
    public String getGeoLocationFile()
    {
      return geoLocationFile;
    }

    public String getGroundControlPointFile() {
      return groundControlPointFile;
    }

    public void setGroundControlPointFile(String groundControlPointFile) {
      this.groundControlPointFile = groundControlPointFile;
    }

    public CloseableFile getFile()
    {
      return file;
    }
    
    public void addImage(String filename)
    {
      imageNames.add(filename);
    }

    public int getImageCount()
    {
      return imageNames.size();
    }
    
    public Set<String> getImageNames()
    {
      return imageNames;
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
  public static NewResponse taskNew(ApplicationResource images, boolean isMultispectral, ODMProcessConfiguration configuration, Collection col, AbstractWorkflowTask task)
  {
    return service.taskNew(images, isMultispectral, configuration, col, task);
  }

  public static NewResponse taskNewInit(Collection collection, int imagesCount, boolean isMultispectral, ODMProcessConfiguration configuration)
  {
    return service.taskNewInit(collection, imagesCount, isMultispectral, configuration);
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

  public static ODMProcessingPayload filterAndExtract(ApplicationResource archive, ODMProcessConfiguration configuration, Collection col) throws IOException, CsvValidationException
  {
    String extension = archive.getNameExtension();

    ODMProcessingPayload payload;
    
    if (extension.equalsIgnoreCase("zip"))
    {
      payload = filterZipArchive(archive, configuration);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      payload = filterTarGzArchive(archive, configuration);
    }
    else
    {
      throw new ProgrammingErrorException(new UnsupportedOperationException("Unsupported archive type [" + extension + "]"));
    }
    
    if (col != null && (col.getSensor().getName().toLowerCase().contains("workswell wiris pro") || col.getSensor().getModel().toLowerCase().contains("wiris pro"))
        && configuration.getRadiometricCalibration() != RadiometricCalibration.NONE)
    {
      new WorkswellWirisThermalPhotometricProcessor().process(new FileResource(payload.getFile()));
    }
    
    return payload;
  }

  private static ODMProcessingPayload filterTarGzArchive(ApplicationResource archive, ODMProcessConfiguration configuration) throws IOException, CsvValidationException
  {
    List<String> extensions = ImageryProcessingJob.getSupportedExtensions(ImageryComponent.RAW);

    CloseableFile parent = new CloseableFile(Files.createTempDir(), true);
    final ODMProcessingPayload payload = new ODMProcessingPayload(parent);

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
          else if (filename.equalsIgnoreCase("geo.txt") && configuration.isIncludeGeoLocationFile())
          {
            File file = new File(parent, "geo.txt");

            if (configuration.getGeoLocationFormat().equals(FileFormat.RX1R2))
            {
              try (RX1R2GeoFileConverter reader = RX1R2GeoFileConverter.open(tarIn))
              {
                FileUtils.copyFile(reader.getOutput(), file);
              }
            }
            else
            {
              try (FileOutputStream fos = new FileOutputStream(file))
              {
                IOUtils.copy(tarIn, fos);
              }
            }

            payload.setGeoLocationFile(IOUtils.toString(tarIn, "UTF-8"));
          }
          else if (filename.equalsIgnoreCase("gcp_list.txt") && configuration.isIncludeGroundControlPointFile())
          {
            File file = new File(parent, entry.getName());

            try (FileOutputStream fos = new FileOutputStream(file))
            {
              IOUtils.copy(tarIn, fos);
            }

            payload.setGroundControlPointFile(IOUtils.toString(tarIn, "UTF-8"));
          }
          else if ( ( UasComponentIF.isValidName(filename) && extensions.contains(ext) ))
          {
            File file = new File(parent, entry.getName());

            try (FileOutputStream fos = new FileOutputStream(file))
            {
              IOUtils.copy(tarIn, fos);
            }

            payload.addImage(filename);
          }

        }
      }
    }

    return payload;
  }

  private static ODMProcessingPayload filterZipArchive(ApplicationResource archive, ODMProcessConfiguration configuration) throws IOException, CsvValidationException
  {
    List<String> extensions = ImageryProcessingJob.getSupportedExtensions(ImageryComponent.RAW);
    CloseableFile parent = new CloseableFile(Files.createTempDir(), true);
    final ODMProcessingPayload payload = new ODMProcessingPayload(parent);

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

          if (filename.equalsIgnoreCase("geo.txt") && configuration.isIncludeGeoLocationFile())
          {
            File file = new File(parent, "geo.txt");

            if (configuration.getGeoLocationFormat().equals(FileFormat.RX1R2))
            {
              try (InputStream zis = zipFile.getInputStream(entry))
              {
                try (RX1R2GeoFileConverter reader = RX1R2GeoFileConverter.open(zis))
                {
                  FileUtils.copyFile(reader.getOutput(), file);
                }
              }
            }
            else
            {
              try (FileOutputStream fos = new FileOutputStream(file))
              {
                try (InputStream zis = zipFile.getInputStream(entry))
                {
                  IOUtils.copy(zis, fos);
                }

              }
            }

            payload.setGeoLocationFile(IOUtils.toString(zipFile.getInputStream(entry), "UTF-8"));
          }
          else if (filename.equalsIgnoreCase("gcp_list.txt") && configuration.isIncludeGroundControlPointFile())
          {
            File file = new File(parent, "gcp_list.txt");

            try (FileOutputStream fos = new FileOutputStream(file))
            {
              try (InputStream zis = zipFile.getInputStream(entry))
              {
                IOUtils.copy(zis, fos);
              }
            }

            payload.setGroundControlPointFile(IOUtils.toString(zipFile.getInputStream(entry), "UTF-8"));
          }
          else if ( ( UasComponentIF.isValidName(filename) && extensions.contains(ext) ))
          {
            File file = new File(parent, entry.getName());

            try (FileOutputStream fos = new FileOutputStream(file))
            {
              try (InputStream zis = zipFile.getInputStream(entry))
              {
                IOUtils.copy(zis, fos);
              }
            }

            payload.addImage(filename);
          }
        }
      }
      catch (ZipException e)
      {
        throw new InvalidZipException();
      }
    }

    return payload;
  }

}
