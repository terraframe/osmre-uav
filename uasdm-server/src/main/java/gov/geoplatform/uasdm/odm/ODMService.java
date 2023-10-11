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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileValidator;

public class ODMService implements ODMServiceIF
{
  private static HTTPConnector connector;

  private static final Logger  logger = LoggerFactory.getLogger(ODMService.class);

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

    Response resp = connector.httpGet("task/" + uuid + "/output", new ArrayList<NameValuePair>());

    return new HttpTaskOutputResponse(resp);
  }

  @Override
  public TaskRemoveResponse taskRemove(String uuid)
  {
    initialize();

    List<NameValuePair> nvp = new ArrayList<NameValuePair>();
    nvp.add(new BasicNameValuePair("uuid", uuid));

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
  public NewResponse taskNew(ApplicationResource images, boolean isMultispectral, ODMProcessConfiguration configuration, Collection col, AbstractWorkflowTask task)
  {
    initialize();
    
    try (ODMProcessingPayload payload = ODMFacade.filterAndExtract(images, configuration, col))
    {
      if (payload.getImageCount() > 0)
      {
        if (configuration.isIncludeGeoLocationFile() && StringUtils.isEmpty(payload.getGeoLocationFile()))
        {
          throw new GeoLocationFileMissingException(configuration.getGeoLocationFileName());
        }
        else if (configuration.isIncludeGeoLocationFile())
        {
          GeoLocationFileValidator.validate(configuration.getGeoLocationFormat(), payload, task);
        }
        
        HttpNewResponse resp = this.taskNewInit(payload.getImageCount(), isMultispectral, configuration);

        if (resp.hasError() || resp.getHTTPResponse().isError())
        {
          this.taskRemove(resp.getUUID());
          return resp;
        }

        String uuid = resp.getUUID();

        for (File child : payload.getFile().listFiles())
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
    catch (IOException | CsvValidationException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public HttpNewResponse taskNewInit(int imagesCount, boolean isMultispectral, ODMProcessConfiguration configuration)
  {
    initialize();

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

    if (configuration.getRadiometricCalibration() != null)
    {
      JSONObject param = new JSONObject();
      param.put("name", "radiometric-calibration");
      param.put("value", configuration.getRadiometricCalibration().getCode());
      arr.put(param);
    }

    // Create Cloud-Optimized GeoTIFFs instead of normal GeoTIFFs
    JSONObject cog = new JSONObject();
    cog.put("name", "cog");
    cog.put("value", "true");
    arr.put(cog);

    // Another one of our custom parameters, which only exists on our custom
    // build of NodeODM.
    /*
    JSONObject joImagesCount = new JSONObject();
    joImagesCount.put("name", "imagesCount");
    joImagesCount.put("value", imagesCount);
    arr.put(joImagesCount);
    */

    if (configuration.getResolution() != null)
    {
      JSONObject demResolution = new JSONObject();
      demResolution.put("name", "dem-resolution");
      demResolution.put("value", configuration.getResolution().floatValue());
      arr.put(demResolution);

      JSONObject orthoResolution = new JSONObject();
      orthoResolution.put("name", "orthophoto-resolution");
      orthoResolution.put("value", configuration.getResolution().floatValue());
      arr.put(orthoResolution);
    }

    if (configuration.getMatcherNeighbors() != null)
    {
      JSONObject param = new JSONObject();
      param.put("name", "matcher-neighbors");
      param.put("value", configuration.getMatcherNeighbors());
      arr.put(param);
    }

    if (configuration.getMinNumFeatures() != null)
    {
      JSONObject param = new JSONObject();
      param.put("name", "min-num-features");
      param.put("value", configuration.getMinNumFeatures());
      arr.put(param);
    }

    if (configuration.getPcQuality() != null)
    {
      JSONObject param = new JSONObject();
      param.put("name", "pc-quality");
      param.put("value", configuration.getPcQuality().getCode());
      arr.put(param);
    }

    if (configuration.getFeatureQuality() != null)
    {
      JSONObject param = new JSONObject();
      param.put("name", "feature-quality");
      param.put("value", configuration.getFeatureQuality().getCode());
      arr.put(param);
    }
    
    if (configuration.getVideoResolution() != null)
    {
      JSONObject param = new JSONObject();
      param.put("name", "video-resolution");
      param.put("value", configuration.getVideoResolution());
      arr.put(param);
    }
    
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addTextBody("options", arr.toString());

    Response resp = connector.postAsMultipart("task/new/init", builder.build());

    return new HttpNewResponse(resp);
  }

  @Override
  public ODMResponse taskNewUpload(String uuid, ApplicationResource image)
  {
    initialize();

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    try (CloseableFile file = image.openNewFile())
    {
      builder.addBinaryBody("images", file, ContentType.APPLICATION_OCTET_STREAM, image.getName());

      Response resp = connector.postAsMultipart("task/new/upload/" + uuid, builder.build());

      return new HttpODMResponse(resp);
    }
  }

  @Override
  public ODMResponse taskNewCommit(String uuid)
  {
    initialize();

    Response resp = connector.postAsMultipart("task/new/commit/" + uuid, MultipartEntityBuilder.create().build());

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

    Response resp = connector.httpGet("task/" + uuid + "/info", new ArrayList<NameValuePair>());

    return new HttpInfoResponse(resp);
  }
}
