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
package net.geoprism;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.DeployProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

@Controller(url = "logo")
public class LogoController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException
  {
    if (oid != null && oid.equals("banner"))
    {
      SystemLogoSingletonDTO.uploadBannerAndCache(request, file.getInputStream(), file.getFilename());
    }
    else
    {
      SystemLogoSingletonDTO.uploadMiniLogoAndCache(request, file.getInputStream(), file.getFilename());
    }

    return new RestBodyResponse("");
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getAll(ClientRequestIF request) throws JSONException
  {
    JSONObject banner = new JSONObject();
    banner.put("oid", "banner");
    banner.put("label", "Banner");
    banner.put("custom", SystemLogoSingletonDTO.getBannerFileFromCache(request, null) != null);

    JSONObject logo = new JSONObject();
    logo.put("oid", "logo");
    logo.put("label", "Logo");
    logo.put("custom", SystemLogoSingletonDTO.getMiniLogoFileFromCache(request, null) != null);

    JSONArray icons = new JSONArray();
    icons.put(banner);
    icons.put(logo);

    JSONObject object = new JSONObject();
    object.put("icons", icons);

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF view(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws IOException
  {
    String path = null;

    if (oid != null && oid.equals("banner"))
    {
      path = SystemLogoSingletonDTO.getBannerFileFromCache(request, null);

      if (path == null)
      {
        path = DeployProperties.getDeployPath() + "/net/geoprism/images/splash_logo.png";
      }
    }
    else
    {
      path = SystemLogoSingletonDTO.getMiniLogoFileFromCache(request, null);

      if (path == null)
      {
        path = DeployProperties.getDeployPath() + "/net/geoprism/images/splash_logo_icon.png";
      }
    }

    File file = new File(path);
    FileInputStream fis = new FileInputStream(file);

    String ext = FilenameUtils.getExtension(file.getName());

    return new InputStreamResponse(fis, "image/" + ext);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws IOException
  {
    if (oid != null && oid.equals("banner"))
    {
      SystemLogoSingletonDTO.removeBannerFileFromCache(request, null);
    }
    else
    {
      SystemLogoSingletonDTO.removeMiniLogoFileFromCache(request, null);
    }

    return new RestResponse();
  }

}
