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

import org.json.JSONObject;
import org.springframework.util.MultiValueMap;

import com.runwaysdk.mvc.ErrorRestResponse;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.web.json.JSONRunwayExceptionDTO;

public class StacTiTilerService
{
  public ResponseIF tiles(String sessionId, String matrixSetId, Integer x, Integer y, Integer z, Integer scale, String format, String url, String assets, MultiValueMap<String, String> queryParams)
  {
    try
    {
      StacTiTillerProxy proxy = new StacTiTillerProxy(url, assets);

      return new InputStreamResponse(proxy.tiles(matrixSetId, z, x, y, scale, format, queryParams), "image/" + format);
    }
    catch (Throwable t)
    {
      CogTileException cte = ( t instanceof CogTileException ) ? (CogTileException) t : new CogTileException(t);

      // We don't want this exception to be thrown into Runway's request
      // handling aspects because they will log the exception.
      // And since this service throws so dang many errors I'm worried it will
      // flood the logs.
      return new ErrorRestResponse(new JSONRunwayExceptionDTO(cte).getJSON());
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String url, String assets)
  {
    StacTiTillerProxy proxy = new StacTiTillerProxy(url, assets);

    return proxy.tilejson(contextPath);
  }
}
