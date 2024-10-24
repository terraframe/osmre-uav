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

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import gov.geoplatform.uasdm.remote.RemoteFileGetResponse;
import gov.geoplatform.uasdm.service.ProductService;
import gov.geoplatform.uasdm.view.ProductCriteria;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;

@Controller(url = "product")
public class ProductController
{
  private ProductService service;

  public ProductController()
  {
    this.service = new ProductService();
  }

  @Endpoint(url = "get-odm-run", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getODMRun(ClientRequestIF request, @RequestParamter(name = "artifactId") String artifactId) throws IOException
  {
    return new RestBodyResponse(service.getODMRunByArtifact(request.getSessionId(), artifactId));
  }

  @Endpoint(url = "get-odm-all", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getAllZip(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    return new RemoteFileGetResponse(service.downloadAllZip(request.getSessionId(), id));
  }

  @Endpoint(url = "get-all", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getAll(ClientRequestIF request, @RequestParamter(name = "criteria") String criteria) throws IOException
  {
    JSONArray response = service.getProducts(request.getSessionId(), ProductCriteria.deserialize(criteria));

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "detail", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF detail(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize) throws IOException
  {
    pageNumber = pageNumber != null ? pageNumber : 1;
    pageSize = pageSize != null ? pageSize : 20;

    ProductDetailView detail = service.getProductDetail(request.getSessionId(), id, pageNumber, pageSize);

    return new RestBodyResponse(detail.toJSON());
  }

  @Endpoint(url = "toggle-publish", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF togglePublish(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    ProductView view = service.togglePublish(request.getSessionId(), id);

    return new RestBodyResponse(view.toJSON());
  }

  @Endpoint(url = "toggle-lock", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF toggleLock(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    service.toggleLock(request.getSessionId(), id);

    return new RestResponse();
  }

  @Endpoint(url = "remove", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    this.service.remove(request.getSessionId(), id);

    return new RestResponse();
  }

  @Endpoint(url = "create", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF create(ClientRequestIF request, @RequestParamter(name = "collectionId") String collectionId, @RequestParamter(name = "productName") String productName) throws IOException
  {
    ProductView view = service.create(request.getSessionId(), collectionId, productName);

    return new RestBodyResponse(view.toJSON());
  }

  @Endpoint(url = "mappable-items", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getMappableItems(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    JSONArray response = service.getMappableItems(request.getSessionId(), id);

    return new RestBodyResponse(response);
  }

}
