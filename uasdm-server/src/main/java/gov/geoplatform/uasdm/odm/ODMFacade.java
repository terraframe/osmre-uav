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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.InvalidZipException;
import gov.geoplatform.uasdm.Util;
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

  private static HTTPConnector connector;

  private static final Logger  logger = LoggerFactory.getLogger(ODMFacade.class);

  public static synchronized void initialize()
  {
    if (connector != null)
    {
      return;
    }
    else
    {
      connector = new HTTPConnector();

//      connector.setCredentials(AppProperties.getOdmUsername(), AppProperties.getOdmPassword());
      connector.setServerUrl(AppProperties.getOdmUrl());
    }
  }

  public static void main(String[] args)
  {
    mainInReq();
  }

  @Request
  public static void mainInReq()
  {
    TaskRemoveResponse resp = ODMFacade.taskRemove("a4eb86d5-cd26-4395-8275-942e3ae2e240");

    System.out.println(resp.isSuccess() + " : " + resp.getHTTPResponse().getStatusCode() + " : " + resp.getHTTPResponse().getResponse());
  }

//  public static void options()
//  {
//    initialize();
//    
//    HTTPResponse resp = connector.httpGet("options", new NameValuePair[] {});
//    
//    System.out.println(resp.getResponse());
//  }

  public static TaskOutputResponse taskOutput(String uuid)
  {
    initialize();

    HTTPResponse resp = connector.httpGet("task/" + uuid + "/output", new NameValuePair[] {});

    return new TaskOutputResponse(resp);
  }

  public static TaskRemoveResponse taskRemove(String uuid)
  {
    initialize();

    NameValuePair[] nvp = new NameValuePair[1];
    nvp[0] = new NameValuePair("uuid", uuid);

    HTTPResponse resp = connector.httpPost("task/remove", nvp);

    return new TaskRemoveResponse(resp);
  }

  public static File taskDownload(String uuid)
  {
    initialize();

    try
    {
      File zip = File.createTempFile("all", ".zip");

      String url = connector.getServerUrl() + "task/" + uuid + "/download/all.zip";
      logger.info("Downloading file from ODM [" + url + "].");
      FileUtils.copyURLToFile(new URL(url), zip, 20000, 0);

      return zip;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static NewResponse taskNew(ApplicationResource images, boolean isMultispectral)
  {
    initialize();

    try (CloseablePair filtered = filter(images))
    {
      if (filtered.getItemCount() > 0)
      {
        Part[] parts = new Part[2];

        parts[0] = new FilePart("images", filtered.getFile(), "application/octet-stream", "UTF-8");

        JSONArray arr = new JSONArray();

        JSONObject dsm = new JSONObject();
        dsm.put("name", "dsm");
        dsm.put("value", "true");
        arr.put(dsm);

        JSONObject dtm = new JSONObject();
        dtm.put("name", "dtm");
        dtm.put("value", "true");
        arr.put(dtm);

        if (isMultispectral)
        {
          JSONObject multispectral = new JSONObject();
          multispectral.put("name", "multispectral");
          multispectral.put("value", String.valueOf(isMultispectral));
          arr.put(multispectral);
        }

        parts[1] = new StringPart("options", arr.toString());

        HTTPResponse resp = connector.postAsMultipart("task/new", parts);

        return new NewResponse(resp);
      }
      else
      {
        throw new EmptyFileSetException();
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static InfoResponse taskInfo(String uuid)
  {
    initialize();

    HTTPResponse resp = connector.httpGet("task/" + uuid + "/info", new NameValuePair[] {});

    return new InfoResponse(resp);
  }

  public static CloseablePair filter(ApplicationResource archive) throws IOException
  {
    String extension = archive.getNameExtension();

    if (extension.equalsIgnoreCase("zip"))
    {
      return filterZipArchive(archive);
    }
    else if (extension.equalsIgnoreCase("gz"))
    {
      return filterTarGzArchive(archive);
    }

    throw new ProgrammingErrorException(new UnsupportedOperationException("Unsupported archive type [" + extension + "]"));
  }

  private static CloseablePair filterTarGzArchive(ApplicationResource archive) throws IOException
  {
    File zip = File.createTempFile("filtered", ".zip");
    int itemCount = 0;

    byte data[] = new byte[Util.BUFFER_SIZE];

    try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip))))
    {
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
            else if (!ext.equalsIgnoreCase("mp4") && UasComponentIF.isValidName(filename))
            {
              int count;

              zos.putNextEntry(new ZipEntry(filename));

              while ( ( count = tarIn.read(data, 0, Util.BUFFER_SIZE) ) != -1)
              {
                zos.write(data, 0, count);
              }

              zos.closeEntry();

              itemCount++;
            }

          }
        }
      }
    }

    return new CloseablePair(new CloseableFile(zip), itemCount);
  }

  private static CloseablePair filterZipArchive(ApplicationResource archive) throws IOException
  {
    File zip = File.createTempFile("filtered", ".zip");
    int itemCount = 0;

    try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip))))
    {
      byte[] buffer = new byte[Util.BUFFER_SIZE];

      try (ZipInputStream zis = new ZipInputStream(archive.openNewStream()))
      {
        ZipEntry entry;

        while ( ( entry = zis.getNextEntry() ) != null)
        {
          final String filename = entry.getName();
          final String ext = FilenameUtils.getExtension(filename);

          if (!ext.equalsIgnoreCase("mp4") && UasComponentIF.isValidName(filename))
          {
            int len;

            zos.putNextEntry(new ZipEntry(entry));

            while ( ( len = zis.read(buffer) ) > 0)
            {
              zos.write(buffer, 0, len);
            }

            zos.closeEntry();

            itemCount++;
          }
        }
      }
      catch (ZipException e)
      {
        throw new InvalidZipException();
      }

    }

    return new CloseablePair(new CloseableFile(zip), itemCount);
  }

}
