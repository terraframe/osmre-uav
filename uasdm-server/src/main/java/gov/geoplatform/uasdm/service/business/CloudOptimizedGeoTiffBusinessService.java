/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.service.business;

import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;

import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.web.json.JSONRunwayExceptionDTO;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.cog.CogTileException;
import gov.geoplatform.uasdm.cog.TiTillerProxy;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import net.geoprism.security.UnauthorizedAccessException;

@Service
public class CloudOptimizedGeoTiffBusinessService
{
  private TiTillerProxy proxy = new TiTillerProxy();

  // @Request(RequestType.SESSION)
  public ResponseEntity<?> tiles(String sessionId, String path, String matrixSetId, String x, String y, String z, String scale, String format, MultiValueMap<String, String> queryParams)
  {
    try
    {
      String url = null;

      List<String> urls = queryParams.get("url");

      if (urls.size() > 0)
      {
        url = URLDecoder.decode(urls.get(0), "UTF-8");
      }

      if (url == null || !url.startsWith("s3://" + AppProperties.getPublicBucketName()))
      {
        validateAccess(sessionId, path);
      }

      InputStream inputStream = proxy.tiles(path, matrixSetId, x, y, z, scale, format, queryParams);
      InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", "image/" + format);
      // httpHeaders.setContentLength(contentLengthOfStream);

      return new ResponseEntity<InputStreamResource>(inputStreamResource, httpHeaders, HttpStatus.OK);
    }
    catch (Throwable t)
    {
      CogTileException cte = ( t instanceof CogTileException ) ? (CogTileException) t : new CogTileException(t);

      // We don't want this exception to be thrown into Runway's request
      // handling aspects because they will log the exception.
      // And since this service throws so dang many errors I'm worried it will
      // flood the logs.
      // return new ErrorRestResponse(new
      // JSONRunwayExceptionDTO(cte).getJSON());
      return new ResponseEntity<String>(new JSONRunwayExceptionDTO(cte).getJSON().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Request(RequestType.SESSION)
  private void validateAccess(String sessionId, String path)
  {
    if (UserDAOIF.PUBLIC_USER_ID.equals(Session.getCurrentSession().getUser().getOid()))
    {
      throw new UnauthorizedAccessException();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String path)
  {
    DocumentIF document = Document.find(path);
    boolean isPublished = isPublished(document);

    if (UserDAOIF.PUBLIC_USER_ID.equals(Session.getCurrentSession().getUser().getOid()) && !isPublished)
    {
      throw new UnauthorizedAccessException();
    }

    // Little bit of a hack, but we need the proxy to add api to the replaced
    // paths, since we're fed through a spring controller. Other controllers
    // might not...
    if (contextPath.endsWith("/"))
    {
      contextPath = contextPath + "api";
    }
    else
    {
      contextPath = contextPath + "/api";
    }

    return proxy.tilejson(document, contextPath, isPublished);
  }

  private boolean isPublished(DocumentIF document)
  {
    Document doc = (Document) document;
    List<Product> prods = doc.getProductHasDocumentParentProducts();

    if (prods.size() > 0)
      return prods.get(0).isPublished();
    else
      return false;
  }
}
