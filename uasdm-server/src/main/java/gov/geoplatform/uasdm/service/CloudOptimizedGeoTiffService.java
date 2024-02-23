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
package gov.geoplatform.uasdm.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.service.business.CloudOptimizedGeoTiffBusinessService;
import gov.geoplatform.uasdm.tile.TileServiceAuthenticator;
import gov.geoplatform.uasdm.tile.TileServiceAuthenticator.TileJsonRequest;
import gov.geoplatform.uasdm.tile.TileServiceAuthenticator.TilesRequest;

@Service
public class CloudOptimizedGeoTiffService
{
  @Autowired
  protected ApplicationContext applicationContext;
  
  @Autowired
  protected CloudOptimizedGeoTiffBusinessService service;
  
//  @Request(RequestType.SESSION)
  public ResponseEntity<?> tiles(String sessionId, String path, String matrixSetId, String x, String y, String z, String scale, String format, MultiValueMap<String, String> queryParams)
  {
    TilesRequest req = new TilesRequest();
    req.sessionId = sessionId;
    req.path = path;
    req.matrixSetId = matrixSetId;
    req.x = x;
    req.y = y;
    req.z = z;
    req.scale = scale;
    req.format = format;
    req.queryParams = queryParams;
    
    return service.tiles(sessionId, path, matrixSetId, x, y, z, scale, format, queryParams);
    
//    TileServiceAuthenticator authenticator = new TileServiceAuthenticator(applicationContext, req);
//    authenticator.authenticate();
//    
//    return (ResponseEntity<?>) authenticator.GetResponse();
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String path)
  {
    TileJsonRequest req = new TileJsonRequest();
    req.sessionId = sessionId;
    req.contextPath = contextPath;
    req.path = path;
    
    TileServiceAuthenticator authenticator = new TileServiceAuthenticator(applicationContext, req);
    authenticator.authenticate();
    
    return (JSONObject) authenticator.GetResponse();
  }
}
