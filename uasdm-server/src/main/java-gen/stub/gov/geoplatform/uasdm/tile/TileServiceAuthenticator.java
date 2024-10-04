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
package gov.geoplatform.uasdm.tile;

import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;

import com.runwaysdk.business.rbac.Authenticate;

import gov.geoplatform.uasdm.service.business.CloudOptimizedGeoTiffBusinessService;

public class TileServiceAuthenticator extends TileServiceAuthenticatorBase
{
  @SuppressWarnings("unused")
  private static final long                 serialVersionUID = -24070010;

  private ApplicationContext                applicationContext;

  private TileServiceAuthenticatedRequestIF tileRequest;

  private Object                            response;

  public TileServiceAuthenticator(ApplicationContext applicationContext, TileServiceAuthenticatedRequestIF tileRequest)
  {
    super();
    this.applicationContext = applicationContext;
    this.tileRequest = tileRequest;
  }

  @Authenticate
  @Override
  public void authenticate()
  {
    if ("tiles".equals(tileRequest.method()))
    {
      TilesRequest request = (TilesRequest) tileRequest;
      response = this.applicationContext.getBean(CloudOptimizedGeoTiffBusinessService.class).tiles(request.sessionId, request.path, request.matrixSetId, request.x, request.y, request.z, request.scale, request.format, request.queryParams);
    }
    else if ("tilejson".equals(tileRequest.method()))
    {
      TileJsonRequest request = (TileJsonRequest) tileRequest;
      response = this.applicationContext.getBean(CloudOptimizedGeoTiffBusinessService.class).tilejson(request.sessionId, request.contextPath, request.path);
    }
  }

  public Object GetResponse()
  {
    return response;
  }

  public interface TileServiceAuthenticatedRequestIF
  {
    public String method();
  }

  public static class TilesRequest implements TileServiceAuthenticatedRequestIF
  {
    public String                        sessionId;

    public String                        path;

    public String                        matrixSetId;

    public String                        x;

    public String                        y;

    public String                        z;

    public String                        scale;

    public String                        format;

    public MultiValueMap<String, String> queryParams;

    public String method()
    {
      return "tiles";
    }
  }

  public static class TileJsonRequest implements TileServiceAuthenticatedRequestIF
  {
    public String sessionId;

    public String contextPath;

    public String path;

    public String method()
    {
      return "tilejson";
    }
  }
}
