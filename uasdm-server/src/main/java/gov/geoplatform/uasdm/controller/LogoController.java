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
package gov.geoplatform.uasdm.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jakarta.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.runwaysdk.constants.DeployProperties;

import gov.geoplatform.uasdm.controller.body.LogoBody;
import net.geoprism.SystemLogoSingletonDTO;

@RestController
@Validated
@RequestMapping("/api/logo")
public class LogoController extends AbstractController
{
  @PostMapping("/apply")
  public ResponseEntity<Void> apply(@Valid @ModelAttribute LogoBody body) throws IOException
  {
    String oid = body.getOid();
    MultipartFile file = body.getFile();

    if (oid != null && oid.equals("banner"))
    {
      SystemLogoSingletonDTO.uploadBannerAndCache(this.getClientRequest(), file.getInputStream(), file.getName());
    }
    else
    {
      SystemLogoSingletonDTO.uploadMiniLogoAndCache(this.getClientRequest(), file.getInputStream(), file.getName());
    }

    return ResponseEntity.ok(null);
  }

  @GetMapping("/get-all")
  public ResponseEntity<String> getAll()
  {
    JSONObject banner = new JSONObject();
    banner.put("oid", "banner");
    banner.put("label", "Banner");
    banner.put("custom", SystemLogoSingletonDTO.getBannerFileFromCache(this.getClientRequest(), null) != null);

    JSONObject logo = new JSONObject();
    logo.put("oid", "logo");
    logo.put("label", "Logo");
    logo.put("custom", SystemLogoSingletonDTO.getMiniLogoFileFromCache(this.getClientRequest(), null) != null);

    JSONArray icons = new JSONArray();
    icons.put(banner);
    icons.put(logo);

    JSONObject object = new JSONObject();
    object.put("icons", icons);

    return ResponseEntity.ok(object.toString());
  }

  @GetMapping("/view")
  public ResponseEntity<InputStreamResource> view(@RequestParam(name = "oid", required = false) String oid) throws IOException
  {
    String path = null;

    if (oid != null && oid.equals("banner"))
    {
      path = SystemLogoSingletonDTO.getBannerFileFromCache(this.getClientRequest(), null);

      if (path == null)
      {
        path = DeployProperties.getDeployPath() + "/net/geoprism/images/splash_logo.png";
      }
    }
    else
    {
      path = SystemLogoSingletonDTO.getMiniLogoFileFromCache(this.getClientRequest(), null);

      if (path == null)
      {
        path = DeployProperties.getDeployPath() + "/net/geoprism/images/splash_logo_icon.png";
      }
    }

    File file = new File(path);
    String ext = FilenameUtils.getExtension(file.getName());

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Content-Type", "image/" + ext);

    return new ResponseEntity<InputStreamResource>(new InputStreamResource(new FileInputStream(file)), httpHeaders, HttpStatus.OK);
  }

  @PostMapping("/remove")
  public ResponseEntity<String> remove(@Valid @RequestBody OidBody body) throws IOException
  {
    String oid = body.getOid();

    if (oid != null && oid.equals("banner"))
    {
      SystemLogoSingletonDTO.removeBannerFileFromCache(this.getClientRequest(), null);
    }
    else
    {
      SystemLogoSingletonDTO.removeMiniLogoFileFromCache(this.getClientRequest(), null);
    }

    return ResponseEntity.ok(null);
  }

}
