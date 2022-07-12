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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
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
import com.google.common.io.Files;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
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

  public static CloseableFile taskDownload(String uuid)
  {
    initialize();

    try
    {
      CloseableFile zip = new CloseableFile(File.createTempFile("all", ".zip"));

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
  
  /*
   * Uploads the zip to ODM using the init / upload / commit paradigm, which is recommended for large file uploads.
   * 
   * https://github.com/OpenDroneMap/NodeODM/blob/master/docs/index.adoc#post-tasknewinit
   */
  public static NewResponse taskNew(ApplicationResource images, boolean isMultispectral)
  {
    initialize();
    
    try (CloseablePair parent = filterAndExtract(images))
    {
      if (parent.getItemCount() > 0)
      {
        NewResponse resp = ODMFacade.taskNewInit(parent.getItemCount(), isMultispectral);
        
        if (resp.hasError() || resp.getHTTPResponse().isError())
        {
          ODMFacade.taskRemove(resp.getUUID());
          return resp;
        }
        
        String uuid = resp.getUUID();
        
        for (File child : parent.getFile().listFiles())
        {
          ODMResponse uploadResp = ODMFacade.taskNewUpload(uuid, new FileResource(child));
          
          if (uploadResp.hasError() || uploadResp.getHTTPResponse().isError())
          {
            ODMFacade.taskRemove(uuid);
            return new NewResponse(uploadResp.getHTTPResponse(), uuid);
          }
        }
        
        ODMResponse commitResp = ODMFacade.taskNewCommit(uuid);
        
        return new NewResponse(commitResp.getHTTPResponse(), uuid);
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
  
  public static NewResponse taskNewInit(int imagesCount, boolean isMultispectral)
  {
    initialize();
    
    Part[] parts = new Part[1];
    
    JSONArray arr = new JSONArray();
    
    // Use this tag to build a DSM (Digital Surface Model, ground + objects) using a progressive morphological filter. Check the --dem* parameters for finer tuning.
    JSONObject dsm = new JSONObject();
    dsm.put("name", "dsm");
    dsm.put("value", "true");
    arr.put(dsm);
    
    // Set this parameter if you want to generate a PNG rendering of the orthophoto.
    JSONObject orthoPng = new JSONObject();
    orthoPng.put("name", "orthophoto-png");
    orthoPng.put("value", "true");
    arr.put(orthoPng);

    // Use this tag to build a DTM (Digital Terrain Model, ground only) using a simple morphological filter. Check the --dem* and --smrf* parameters for finer tuning.
    JSONObject dtm = new JSONObject();
    dtm.put("name", "dtm");
    dtm.put("value", "true");
    arr.put(dtm);

    // Our custom Micasense multispectral parameter, which only works on our custom build of ODM.
    if (isMultispectral)
    {
      JSONObject multispectral = new JSONObject();
      multispectral.put("name", "multispectral");
      multispectral.put("value", String.valueOf(isMultispectral));
      arr.put(multispectral);
    }
    
    // Create Cloud-Optimized GeoTIFFs instead of normal GeoTIFFs
    JSONObject cog = new JSONObject();
    cog.put("name", "cog");
    cog.put("value", "true");
    arr.put(cog);
    
    // Another one of our custom parameters, which only exists on our custom build of NodeODM.
    JSONObject joImagesCount = new JSONObject();
    joImagesCount.put("name", "imagesCount");
    joImagesCount.put("value", imagesCount);
    arr.put(joImagesCount);

    parts[0] = new StringPart("options",  arr.toString(), "UTF-8");
    
    HTTPResponse resp = connector.postAsMultipart("task/new/init", parts);
    
    return new NewResponse(resp);
  }
  
  public static ODMResponse taskNewUpload(String uuid, ApplicationResource image)
  {
    initialize();
    
    Part[] parts = new Part[1];
    
    try (CloseableFile file = image.openNewFile())
    {
      parts[0] = new FilePart("images", file, "application/octet-stream", "UTF-8");
  
      HTTPResponse resp = connector.postAsMultipart("task/new/upload/" + uuid, parts);
  
      return new ODMResponse(resp);
    }
    catch (FileNotFoundException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public static ODMResponse taskNewCommit(String uuid)
  {
    initialize();
    
    Part[] parts = new Part[0];
    
    HTTPResponse resp = connector.postAsMultipart("task/new/commit/" + uuid, parts);

    return new ODMResponse(resp);
  }

  /*
   * taskNew was removed in favour of the init / upload / commit paradigm
   */
  /*
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
        
        JSONObject imagesCount = new JSONObject();
        imagesCount.put("name", "imagesCount");
        imagesCount.put("value", filtered.getItemCount());
        arr.put(imagesCount);

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
  */

  public static InfoResponse taskInfo(String uuid)
  {
    initialize();

    HTTPResponse resp = connector.httpGet("task/" + uuid + "/info", new NameValuePair[] {});

    return new InfoResponse(resp);
  }

  public static CloseablePair filterAndExtract(ApplicationResource archive) throws IOException
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
    CloseableFile parent = new CloseableFile(Files.createTempDir(), true);
    int itemCount = 0;

    byte data[] = new byte[Util.BUFFER_SIZE];

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

            File file = new File(parent, entry.getName());
            
            try (FileOutputStream fos = new FileOutputStream(file))
            {
              while ( ( count = tarIn.read(data, 0, Util.BUFFER_SIZE) ) != -1)
              {
                fos.write(data, 0, count);
              }
            }

            itemCount++;
          }

        }
      }
    }

    return new CloseablePair(parent, itemCount);
  }

  private static CloseablePair filterZipArchive(ApplicationResource archive) throws IOException
  {
    CloseableFile parent = new CloseableFile(Files.createTempDir(), true);
    int itemCount = 0;

    byte[] buffer = new byte[Util.BUFFER_SIZE];

    try (CloseableFile fArchive = archive.openNewFile())
    {
      try (ZipFile zipFile = new ZipFile(fArchive))
      {
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

        while ( entries.hasMoreElements() )
        {
          ZipArchiveEntry entry = entries.nextElement();
          final String filename = entry.getName();
          final String ext = FilenameUtils.getExtension(filename);

          if (!ext.equalsIgnoreCase("mp4") && UasComponentIF.isValidName(filename))
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
