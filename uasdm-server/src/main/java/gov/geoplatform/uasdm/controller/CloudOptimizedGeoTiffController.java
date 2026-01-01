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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import gov.geoplatform.uasdm.cog.CogTileException;
import gov.geoplatform.uasdm.service.CloudOptimizedGeoTiffService;
import gov.geoplatform.uasdm.view.TileAccessControl;

@Controller
@Validated
@RequestMapping("/api/cog")
public class CloudOptimizedGeoTiffController extends AbstractController
{
  @Autowired
  private CloudOptimizedGeoTiffService service;

  /**
   * Path is the S3 path to the object, excluding any bucket information. An
   * example of this is:
   * mysite/myproject/mymission/mycollection/ortho/odm_orthophoto.tif
   * 
   * For more information, view the public TiTiler docs at:
   * https://k67ob0ncba.execute-api.us-east-1.amazonaws.com
   * 
   * @param request
   * @param sRequest
   * @param path
   * @return
   */
  @GetMapping("/tilejson.json")
  public ResponseEntity<String> tilejson(HttpServletRequest request, @RequestParam(required = true, name = "path") String path)
  {
    JSONObject tilejson = this.service.tilejson(getSessionId(), request.getContextPath(), path, this.getAccessControl(request));

    return new ResponseEntity<String>(tilejson.toString(), HttpStatus.OK);
  }

  /**
   * In keeping in alignment with the TiTiler API, we have embedded a few
   * parameters into the pathing of this URL.
   * 
   * cog/tiles/{TileMatrixSetId}/{z}/{x}/{y}@{scale}x.{format}
   * 
   * We have one parameter which is non-standard to the TiTiler API, path. Path
   * is the S3 path to the object, excluding any bucket information. An example
   * of this is:
   * mysite/myproject/mymission/mycollection/ortho/odm_orthophoto.tif
   * 
   * The rest of the parameters for this method are directly proxied to TiTiler.
   * You can find documentation for them on our public TiTiler API, at:
   * https://k67ob0ncba.execute-api.us-east-1.amazonaws.com
   * 
   * @param request
   * @param servletRequest
   * @param path
   * @return
   */
  public static final String TILES_REGEX = "tiles\\/(.+\\/)?(\\d+)\\/(\\d+)\\/(\\d+)(@\\d+x)?(\\.[^?\\n\\r]+)?";

  @GetMapping("/tiles/**")
  public ResponseEntity<?> tiles(HttpServletRequest request, @RequestParam(required = true, name = "path") String path)
  {
    String url = request.getRequestURI();

    Pattern pattern = Pattern.compile("\\/cog\\/" + TILES_REGEX, Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(url);

    if (matcher.find())
    {
      String matrixSetId = matcher.group(1);
      String z = matcher.group(2);
      String x = matcher.group(3);
      String y = matcher.group(4);
      String scale = matcher.group(5);
      String format = matcher.group(6);

      if (x == null)
      {
        throw new CogTileException("Missing required parameter: x.");
      }
      if (y == null)
      {
        throw new CogTileException("Missing required parameter: y.");
      }
      if (z == null)
      {
        throw new CogTileException("Missing required parameter: z.");
      }
      if (format == null)
      {
        format = "tif";
      }
      if (matrixSetId != null && matrixSetId.endsWith("/"))
      {
        matrixSetId = matrixSetId.substring(0, matrixSetId.length() - 1);
      }
      if (scale != null && scale.startsWith("@"))
      {
        scale = scale.substring(1);
      }
      if (scale != null && scale.endsWith("x"))
      {
        scale = scale.substring(0, scale.length() - 1);
      }

      String fullUri;
      StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
      String queryString = request.getQueryString();

      if (queryString == null)
      {
        fullUri = requestURL.toString();
      }
      else
      {
        fullUri = requestURL.append('?').append(queryString).toString();
      }
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(fullUri).build().getQueryParams();

      return this.service.tiles(getSessionId(), path, matrixSetId, x, y, z, scale, format, queryParams, getAccessControl(request));
    }
    else
    {
      throw new CogTileException("The provided url is invalid.");
    }
  }

  private TileAccessControl getAccessControl(HttpServletRequest request)
  {
    HttpSession session = request.getSession();

    TileAccessControl control = (TileAccessControl) session.getAttribute("access-control");

    if (control == null)
    {
      control = new TileAccessControl();

      session.setAttribute("access-control", control);
    }

    return control;
  }
}
