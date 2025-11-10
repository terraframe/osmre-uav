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
package gov.geoplatform.uasdm.controller;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.controller.body.CreateProductBody;
import gov.geoplatform.uasdm.controller.body.IdBody;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.request.ProductService;
import gov.geoplatform.uasdm.view.ODMRunView;
import gov.geoplatform.uasdm.view.ProductCriteria;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;

@RestController
@Validated
@RequestMapping("/api/product")
public class ProductController extends AbstractController
{
  @Autowired
  private ProductService service;

  @GetMapping("/get-odm-run")
  public ResponseEntity<String> getODMRun(@RequestParam(name = "artifactId") String artifactId)
  {
    ODMRunView response = service.getODMRunByArtifact(this.getSessionId(), artifactId);

    return ResponseEntity.ok(response.toJson().toString());
  }

  @GetMapping("/get-odm-all")
  public ResponseEntity<InputStreamResource> getAllZip(@RequestParam(name = "id") String id)
  {
    RemoteFileObject file = service.downloadAllZip(this.getSessionId(), id);

    return getRemoteFile(file);
  }

  @GetMapping("/get-all")
  public ResponseEntity<String> getAll(@RequestParam(name = "criteria") String criteria)
  {
    JSONArray response = service.getProducts(this.getSessionId(), ProductCriteria.deserialize(criteria));

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/detail")
  public ResponseEntity<String> detail(@RequestParam(name = "id") String id, @RequestParam(name = "pageNumber", defaultValue = "1") Integer pageNumber, @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize)
  {
    ProductDetailView detail = service.getProductDetail(this.getSessionId(), id, pageNumber, pageSize);

    return ResponseEntity.ok(detail.toJSON().toString());
  }

  @PostMapping("/toggle-publish")
  public ResponseEntity<String> togglePublish(@RequestBody IdBody body)
  {
    ProductView view = service.togglePublish(this.getSessionId(), body.getId());

    return ResponseEntity.ok(view.toJSON().toString());
  }

  @PostMapping("/toggle-lock")
  public ResponseEntity<Void> toggleLock(@RequestBody IdBody body)
  {
    service.toggleLock(this.getSessionId(), body.getId());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@RequestBody IdBody body)
  {
    this.service.remove(this.getSessionId(), body.getId());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/create")
  public ResponseEntity<String> create(@RequestBody CreateProductBody body)
  {
    ProductView view = service.create(this.getSessionId(), body.getCollectionId(), body.getProductName());

    return ResponseEntity.ok(view.toJSON().toString());
  }

  @GetMapping("/mappable-items")
  public ResponseEntity<String> getMappableItems(@RequestParam(name = "id") String id)
  {
    JSONArray response = service.getMappableItems(this.getSessionId(), id);

    return ResponseEntity.ok(response.toString());
  }
}
