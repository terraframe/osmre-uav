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

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;

public class CloudOptimizedGeoTiffService
{
  private TiTillerProxy proxy = new TiTillerProxy();
  
  @Request(RequestType.SESSION)
  public ResponseIF tiles(String sessionId, String path, String matrixSetId, String x, String y, String z, String scale, String format, MultiValueMap<String, String> queryParams)
  {
    try
    {
      DocumentIF document = Document.find(path);
      
      return new InputStreamResponse(proxy.tiles(document, matrixSetId, x, y, z, scale, format, queryParams), "image/" + format);
    }
    catch (Throwable t)
    {
      CogTileException cte = (t instanceof CogTileException) ? (CogTileException) t : new CogTileException(t);
      
      // We don't want this exception to be thrown into Runway's request handling aspects because they will log the exception.
      // And since this service throws so dang many errors I'm worried it will flood the logs.
      return new ErrorRestResponse(new JSONRunwayExceptionDTO(cte).getJSON());
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String path)
  {
    DocumentIF document = Document.find(path);
    
    return proxy.tilejson(document, contextPath);
  }
}
