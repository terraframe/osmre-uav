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
package gov.geoplatform.uasdm.cog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.request.ServletRequestIF;

@Controller(url = "stac")
public class StacTiTillerController
{
  private StacTiTilerService service;

  public StacTiTillerController()
  {
    this.service = new StacTiTilerService();
  }

  @Endpoint(url = "tilejson.json", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF tilejson(ClientRequestIF request, ServletRequestIF sRequest, @RequestParamter(name = "url", required = true) String url, @RequestParamter(name = "assets", required = true) String assets)
  {
    JSONObject tilejson = this.service.tilejson(request.getSessionId(), sRequest.getContextPath(), url, assets);

    return new RestBodyResponse(tilejson);
  }

  public static final String TILES_REGEX = "tiles\\/(.+\\/)?(\\d+)\\/(\\d+)\\/(\\d+)(@\\d+x)?(\\.[^?\\n\\r]+)?";

  @Endpoint(url = TILES_REGEX, method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF tiles(ClientRequestIF request, ServletRequestIF servletRequest, @RequestParamter(name = "url", required = true) String url, @RequestParamter(name = "assets", required = true) String assets)
  {
    String uri = servletRequest.getRequestURI();

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
      StringBuilder requestURL = new StringBuilder(servletRequest.getRequestURL().toString());
      String queryString = servletRequest.getQueryString();

      if (queryString == null) {
        fullUri = requestURL.toString();
      } else {
        fullUri = requestURL.append('?').append(queryString).toString();
      }
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(fullUri).build().getQueryParams();

      return this.service.tiles(request.getSessionId(), matrixSetId, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z), Integer.valueOf(scale), format, url, assets, queryParams);
    }
    else
    {
      throw new CogTileException("The provided url is invalid.");
    }
  }
}
