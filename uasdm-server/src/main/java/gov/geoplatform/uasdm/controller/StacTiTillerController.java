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
import gov.geoplatform.uasdm.service.request.StacTiTilerService;

@Controller
@Validated
@RequestMapping("/stac")
public class StacTiTillerController extends AbstractController
{
  public static final String TILES_REGEX = "tiles\\/(.+\\/)?(\\d+)\\/(\\d+)\\/(\\d+)(@\\d+x)?(\\.[^?\\n\\r]+)?";

  @Autowired
  private StacTiTilerService service;

  @GetMapping("/tilejson.json")
  public ResponseEntity<String> tilejson( //
      @RequestParam(name = "url", required = true) String url, //
      @RequestParam(name = "assets", required = true) String assets, //
      @RequestParam(name = "multispectral", required = false, defaultValue = "false") Boolean multispectral, //
      @RequestParam(name = "hillshade", required = false, defaultValue = "false") Boolean hillshade //
  )
  {
    JSONObject tilejson = this.service.tilejson(this.getSessionId(), this.getRequest().getContextPath(), url, assets, multispectral, hillshade);

    return new ResponseEntity<String>(tilejson.toString(), HttpStatus.OK);
  }

  @GetMapping("/tiles/**")
  public ResponseEntity<?> tiles(@RequestParam(name = "url", required = true) String url, @RequestParam(name = "assets", required = true) String assets)
  {
    String uri = this.getRequest().getRequestURI();

    Pattern pattern = Pattern.compile("\\/stac\\/" + TILES_REGEX, Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(uri);

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
      StringBuilder requestURL = new StringBuilder(this.getRequest().getRequestURL().toString());
      String queryString = this.getRequest().getQueryString();

      if (queryString == null)
      {
        fullUri = requestURL.toString();
      }
      else
      {
        fullUri = requestURL.append('?').append(queryString).toString();
      }
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(fullUri).build().getQueryParams();

      return this.service.tiles(this.getSessionId(), matrixSetId, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z), Integer.valueOf(scale), format, url, assets, queryParams);
    }
    else
    {
      throw new CogTileException("The provided url is invalid.");
    }
  }
}
