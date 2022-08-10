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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.odm.ODMFacade.CloseablePair;

public class ODMService implements ODMServiceIF
{
  private static HTTPConnector connector;

  private static final Logger logger = LoggerFactory.getLogger(ODMService.class);

  public synchronized void initialize()
  {
    if (connector != null)
    {
      return;
    }
    else
    {
      connector = new HTTPConnector();

      // connector.setCredentials(AppProperties.getOdmUsername(),
      // AppProperties.getOdmPassword());
      connector.setServerUrl(AppProperties.getOdmUrl());
    }
  }

  // public void options()
  // {
  // initialize();
  //
  // HTTPResponse resp = connector.httpGet("options", new NameValuePair[] {});
  //
  // System.out.println(resp.getResponse());
  // }

  @Override
  public TaskOutputResponse taskOutput(String uuid)
  {
    initialize();

    Response resp = connector.httpGet("task/" + uuid + "/output", new NameValuePair[] {});

    return new HttpTaskOutputResponse(resp);
  }

  @Override
  public TaskRemoveResponse taskRemove(String uuid)
  {
    initialize();

    NameValuePair[] nvp = new NameValuePair[1];
    nvp[0] = new NameValuePair("uuid", uuid);

    Response resp = connector.httpPost("task/remove", nvp);

    return new HttpTaskRemoveResponse(resp);
  }

  @Override
  public CloseableFile taskDownload(String uuid)
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
   * Uploads the zip to ODM using the init / upload / commit paradigm, which is
   * recommended for large file uploads.
   * 
   * https://github.com/OpenDroneMap/NodeODM/blob/master/docs/index.adoc#post-
   * tasknewinit
   */
  @Override
  public NewResponse taskNew(ApplicationResource images, boolean isMultispectral)
  {
    initialize();

    try (CloseablePair parent = ODMFacade.filterAndExtract(images))
    {
      if (parent.getItemCount() > 0)
      {
        HttpNewResponse resp = this.taskNewInit(parent.getItemCount(), isMultispectral);

        if (resp.hasError() || resp.getHTTPResponse().isError())
        {
          this.taskRemove(resp.getUUID());
          return resp;
        }

        String uuid = resp.getUUID();

        for (File child : parent.getFile().listFiles())
        {
          ODMResponse uploadResp = this.taskNewUpload(uuid, new FileResource(child));

          if (uploadResp.hasError() || uploadResp.getHTTPResponse().isError())
          {
            this.taskRemove(uuid);
            return new HttpNewResponse(uploadResp.getHTTPResponse(), uuid);
          }
        }

        ODMResponse commitResp = this.taskNewCommit(uuid);

        return new HttpNewResponse(commitResp.getHTTPResponse(), uuid);
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

  @Override
  public HttpNewResponse taskNewInit(int imagesCount, boolean isMultispectral)
  {
    initialize();

    Part[] parts = new Part[1];

    JSONArray arr = new JSONArray();

    // Use this tag to build a DSM (Digital Surface Model, ground + objects)
    // using a progressive morphological filter. Check the --dem* parameters for
    // finer tuning.
    JSONObject dsm = new JSONObject();
    dsm.put("name", "dsm");
    dsm.put("value", "true");
    arr.put(dsm);

    // Set this parameter if you want to generate a PNG rendering of the
    // orthophoto.
    JSONObject orthoPng = new JSONObject();
    orthoPng.put("name", "orthophoto-png");
    orthoPng.put("value", "true");
    arr.put(orthoPng);

    // Use this tag to build a DTM (Digital Terrain Model, ground only) using a
    // simple morphological filter. Check the --dem* and --smrf* parameters for
    // finer tuning.
    JSONObject dtm = new JSONObject();
    dtm.put("name", "dtm");
    dtm.put("value", "true");
    arr.put(dtm);

    // Our custom Micasense multispectral parameter, which only works on our
    // custom build of ODM.
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

    // Another one of our custom parameters, which only exists on our custom
    // build of NodeODM.
    JSONObject joImagesCount = new JSONObject();
    joImagesCount.put("name", "imagesCount");
    joImagesCount.put("value", imagesCount);
    arr.put(joImagesCount);

    parts[0] = new StringPart("options", arr.toString(), "UTF-8");

    Response resp = connector.postAsMultipart("task/new/init", parts);

    return new HttpNewResponse(resp);
  }

  @Override
  public ODMResponse taskNewUpload(String uuid, ApplicationResource image)
  {
    initialize();

    Part[] parts = new Part[1];

    try (CloseableFile file = image.openNewFile())
    {
      parts[0] = new FilePart("images", file, "application/octet-stream", "UTF-8");

      Response resp = connector.postAsMultipart("task/new/upload/" + uuid, parts);

      return new HttpODMResponse(resp);
    }
    catch (FileNotFoundException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public ODMResponse taskNewCommit(String uuid)
  {
    initialize();

    Part[] parts = new Part[0];

    Response resp = connector.postAsMultipart("task/new/commit/" + uuid, parts);

    return new HttpODMResponse(resp);
  }

  /*
   * taskNew was removed in favour of the init / upload / commit paradigm
   */
  /*
   * public NewResponse taskNew(ApplicationResource images, boolean
   * isMultispectral) { initialize();
   * 
   * try (CloseablePair filtered = filter(images)) { if (filtered.getItemCount()
   * > 0) { Part[] parts = new Part[2];
   * 
   * parts[0] = new FilePart("images", filtered.getFile(),
   * "application/octet-stream", "UTF-8");
   * 
   * JSONArray arr = new JSONArray();
   * 
   * JSONObject dsm = new JSONObject(); dsm.put("name", "dsm"); dsm.put("value",
   * "true"); arr.put(dsm);
   * 
   * JSONObject dtm = new JSONObject(); dtm.put("name", "dtm"); dtm.put("value",
   * "true"); arr.put(dtm);
   * 
   * if (isMultispectral) { JSONObject multispectral = new JSONObject();
   * multispectral.put("name", "multispectral"); multispectral.put("value",
   * String.valueOf(isMultispectral)); arr.put(multispectral); }
   * 
   * JSONObject imagesCount = new JSONObject(); imagesCount.put("name",
   * "imagesCount"); imagesCount.put("value", filtered.getItemCount());
   * arr.put(imagesCount);
   * 
   * parts[1] = new StringPart("options", arr.toString());
   * 
   * HTTPResponse resp = connector.postAsMultipart("task/new", parts);
   * 
   * return new NewResponse(resp); } else { throw new EmptyFileSetException(); }
   * } catch (IOException e) { throw new ProgrammingErrorException(e); } }
   */

  @Override
  public InfoResponse taskInfo(String uuid)
  {
    initialize();

    Response resp = connector.httpGet("task/" + uuid + "/info", new NameValuePair[] {});

    return new HttpInfoResponse(resp);
  }
}
