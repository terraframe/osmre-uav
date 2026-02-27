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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.uasdm.controller.body.IdBody;
import gov.geoplatform.uasdm.service.request.ImageSetService;
import gov.geoplatform.uasdm.view.CollectionCriteria;
import gov.geoplatform.uasdm.view.ComponentImageSetView;
import gov.geoplatform.uasdm.view.AssignImageSetView;
import gov.geoplatform.uasdm.view.ImageSetView;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/image-set")
public class ImageSetController extends AbstractController
{
  public static class RemoveImageBody
  {
    @NotBlank
    private String id;

    @NotBlank
    private String imageId;

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getImageId()
    {
      return imageId;
    }

    public void setImageId(String imageId)
    {
      this.imageId = imageId;
    }
  }

  @Autowired
  private ImageSetService service;

  @GetMapping("/get-all")
  public ResponseEntity<List<ComponentImageSetView>> getAll(@RequestParam(name = "criteria") String criteria)
  {
    List<ComponentImageSetView> response = service.getAll(this.getSessionId(), CollectionCriteria.deserialize(criteria));

    return ResponseEntity.ok(response);
  }

  @GetMapping("/list")
  public ResponseEntity<List<ImageSetView>> list(@RequestParam(name = "collectionId") String collectionId)
  {
    List<ImageSetView> response = service.list(this.getSessionId(), collectionId);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get")
  public ResponseEntity<ImageSetView> get(@RequestParam(name = "oid") String oid)
  {
    ImageSetView response = service.get(this.getSessionId(), oid);

    return ResponseEntity.ok(response);
  }

  // @GetMapping("/detail")
  // public ResponseEntity<String> detail(@RequestParam(name = "id") String id,
  // @RequestParam(name = "pageNumber", defaultValue = "1") Integer pageNumber,
  // @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize)
  // {
  // ProductDetailView detail = service.getProductDetail(this.getSessionId(),
  // id, pageNumber, pageSize);
  //
  // return ResponseEntity.ok(detail.toJSON().toString());
  // }

  @PostMapping("/toggle-publish")
  public ResponseEntity<ImageSetView> togglePublish(@RequestBody IdBody body)
  {
    ImageSetView view = service.togglePublish(this.getSessionId(), body.getId());

    return ResponseEntity.ok(view);
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

  @PostMapping("/remove-image")
  public ResponseEntity<ImageSetView> removeImage(@RequestBody RemoveImageBody body)
  {
    ImageSetView set =this.service.removeImage(this.getSessionId(), body.getId(), body.getImageId());

    return ResponseEntity.ok(set);
  }

  // @PostMapping("/remove-file")
  // public ResponseEntity<Void> remove(@RequestBody IdBody body)
  // {
  // this.service.remove(this.getSessionId(), body.getId());
  //
  // return ResponseEntity.ok(null);
  // }

  @PostMapping("/create")
  public ResponseEntity<ImageSetView> create(@RequestBody AssignImageSetView body)
  {
    ImageSetView view = service.create(this.getSessionId(), body);

    return ResponseEntity.ok(view);
  }
}
