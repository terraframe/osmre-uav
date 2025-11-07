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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.controller.body.CollectionBody;
import gov.geoplatform.uasdm.controller.body.EntityArtifactBody;
import gov.geoplatform.uasdm.controller.body.EntityBody;
import gov.geoplatform.uasdm.controller.body.EntityItemBody;
import gov.geoplatform.uasdm.controller.body.EntityProductBody;
import gov.geoplatform.uasdm.controller.body.EntityWithParentBody;
import gov.geoplatform.uasdm.controller.body.ExcludeItemBody;
import gov.geoplatform.uasdm.controller.body.IdBody;
import gov.geoplatform.uasdm.controller.body.MetadataBody;
import gov.geoplatform.uasdm.controller.body.ParentIdBody;
import gov.geoplatform.uasdm.controller.body.ParentIdWithTypeBody;
import gov.geoplatform.uasdm.controller.body.RunProcessBody;
import gov.geoplatform.uasdm.controller.body.StandaloneProductBody;
import gov.geoplatform.uasdm.controller.body.TaskIdBody;
import gov.geoplatform.uasdm.controller.body.UploadArtifactFileBody;
import gov.geoplatform.uasdm.controller.body.UploadIdBody;
import gov.geoplatform.uasdm.model.InvalidRangeException;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.remote.BasicFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.service.WorkflowService;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.QuerySiteResult;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.TreeComponent;

@RestController
@Validated
@RequestMapping("/api/project")
public class ProjectManagementController extends AbstractController
{
  private static final Logger      logger = LoggerFactory.getLogger(ProjectManagementController.class);

  @Autowired
  private ProjectManagementService service;

  @GetMapping("/configuration")
  public ResponseEntity<String> configuration()
  {
    JSONObject configuration = this.service.configuration(this.getSessionId(), this.getRequest().getContextPath());

    return ResponseEntity.ok(configuration.toString());
  }

  @GetMapping("/get-default-run-config")
  public ResponseEntity<String> getDefaultRunConfig(@RequestParam(required = false, name = "collectionId") String collectionId)
  {
    String config = this.service.getDefaultRunConfig(this.getSessionId(), collectionId);

    return ResponseEntity.ok(config);
  }

  @GetMapping("/get-odm-run-by-task")
  public ResponseEntity<String> getODMRun(@RequestParam(required = false, name = "taskId") String taskId) throws IOException
  {
    return ResponseEntity.ok(service.getODMRunByTask(this.getSessionId(), taskId).toJson().toString());
  }

  @GetMapping("/get-configuration-by-task")
  public ResponseEntity<String> getConfigurationByTask(@RequestParam(required = false, name = "taskId") String taskId) throws IOException
  {
    ProcessConfiguration configuration = service.getConfigurationByTask(this.getSessionId(), taskId);

    return ResponseEntity.ok(configuration.toJson().toString());
  }

  @GetMapping("/get-children")
  public ResponseEntity<String> getChildren(@RequestParam(required = false, name = "id") String id)
  {
    List<TreeComponent> children = this.service.getChildren(this.getSessionId(), id);

    return ResponseEntity.ok(SiteItem.serialize(children).toString());
  }

  @GetMapping("/view")
  public ResponseEntity<String> view(@RequestParam(required = false, name = "id") String id) throws IOException
  {
    JSONObject response = this.service.view(this.getSessionId(), id);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/roots")
  public ResponseEntity<String> getRoots(@RequestParam(required = false, name = "conditions") String conditions, @RequestParam(required = false, name = "sort") String sort)
  {
    List<TreeComponent> roots = this.service.getRoots(this.getSessionId(), conditions, sort);

    return ResponseEntity.ok(SiteItem.serialize(roots).toString());
  }

  @GetMapping("/metadata-options")
  public ResponseEntity<String> getMetadataOptions(@RequestParam(required = false, name = "collectionId") String collectionId, @RequestParam(required = false, name = "productId") String productId)
  {
    return ResponseEntity.ok(this.service.getMetadataOptions(this.getSessionId(), collectionId, productId).toString());
  }

  @GetMapping("/uav-metadata")
  public ResponseEntity<String> getUAVMetadata(@RequestParam(required = false, name = "uavId") String uavId, @RequestParam(required = false, name = "sensorId") String sensorId)
  {
    return ResponseEntity.ok(this.service.getUAVMetadata(this.getSessionId(), uavId, sensorId).toString());
  }

  @PostMapping("/new-default-child")
  public ResponseEntity<String> newDefaultChild(@RequestBody @Valid ParentIdBody body)
  {
    SiteItem item = this.service.newDefaultChild(this.getSessionId(), body.getParentId());

    JSONObject response = new JSONObject();
    response.put("item", item.toJSON());
    response.put("attributes", AttributeType.toJSON(item.getAttributes()));

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/new-child")
  public ResponseEntity<String> newChild(@RequestBody @Valid ParentIdWithTypeBody body)
  {
    SiteItem item = this.service.newChild(this.getSessionId(), body.getParentId(), body.getType());

    JSONObject response = new JSONObject();
    response.put("item", item.toJSON());
    response.put("attributes", AttributeType.toJSON(item.getAttributes()));

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/apply-metadata")
  public ResponseEntity<Void> applyMetadata(@RequestBody @Valid MetadataBody body)
  {
    this.service.applyMetadata(this.getSessionId(), body.getSelection());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/create-collection")
  public ResponseEntity<String> createCollection(@RequestBody @Valid CollectionBody body)
  {
    String oid = this.service.createCollection(this.getSessionId(), body.getSelections());

    JSONObject response = new JSONObject();
    response.put("oid", oid);

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/create-standalone-product-group")
  public ResponseEntity<String> createStandaloneProductGroup(@RequestBody @Valid StandaloneProductBody body)
  {
    String oid = this.service.createStandaloneProductGroup(this.getSessionId(), body.getProductGroup());

    JSONObject response = new JSONObject();
    response.put("oid", oid);

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/apply-with-parent")
  public ResponseEntity<String> applyWithParent(@RequestBody @Valid EntityWithParentBody body)
  {
    SiteItem item = SiteItem.deserialize(body.getEntity());

    SiteItem result = this.service.applyWithParent(this.getSessionId(), item, body.getParentId());

    return ResponseEntity.ok(result.toJSON().toString());
  }

  @GetMapping("/download-all")
  public ResponseEntity<InputStreamResource> downloadAll(final @RequestParam(required = false, name = "id") String id, final @RequestParam(required = false, name = "key") String key)
  {

    final String sessionId = this.getSessionId();

    SiteItem item = this.service.get(sessionId, id);
    Object name = item.getValue(UasComponent.NAME);

    try
    {
      PipedInputStream istream = new PipedInputStream();
      PipedOutputStream ostream = new PipedOutputStream(istream);

      Thread thread = new Thread(new Runnable()
      {
        @Override
        @Request
        public void run()
        {
          try
          {

            ProjectManagementController.this.service.downloadAll(sessionId, id, key, ostream, true);
          }
          catch (Exception e)
          {
            logger.error("Error occurred while writing response to download-all request.", e);
          }
        }
      });
      thread.setDaemon(true);
      thread.start();

      return ResponseEntity.ok() //
          .header("Content-Type", "application/zip") //
          .header("Content-Disposition", "attachment; filename=\"" + name + ".zip\"") //
          .body(new InputStreamResource(istream));
    }
    catch (IOException e)
    {
      throw new RuntimeException("Test");
    }
  }

  @GetMapping("/download-odm-all")
  public ResponseEntity<InputStreamResource> downloadOdmAll(final @RequestParam(required = false, name = "colId") String colId)
  {
    final String sessionId = this.getSessionId();

    RemoteFileObject file = this.service.downloadOdmAll(sessionId, colId);

    return this.getRemoteFile(file);
  }

  @GetMapping("/download-report")
  public ResponseEntity<InputStreamResource> downloadReport(final @RequestParam(required = false, name = "colId") String colId, final @RequestParam(required = false, name = "productName") String productName, final @RequestParam(required = false, name = "folder") String folder)
  {
    final String sessionId = this.getSessionId();

    RemoteFileObject file = this.service.downloadReport(sessionId, colId, productName, folder);

    return this.getRemoteFile(file);
  }

  @PostMapping("/run-process")
  public ResponseEntity<String> runProcess(@RequestBody @Valid RunProcessBody body)
  {
    this.service.runOrtho(this.getSessionId(), body.getId(), body.getConfiguration());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/edit")
  public ResponseEntity<String> edit(@RequestBody @Valid IdBody body)
  {
    SiteItem item = this.service.edit(this.getSessionId(), body.getId());

    JSONObject response = new JSONObject();
    response.put("item", item.toJSON());
    response.put("attributes", AttributeType.toJSON(item.getAttributes()));

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/set-exclude")
  public ResponseEntity<String> setExclude(@RequestBody @Valid ExcludeItemBody body)
  {
    SiteObject object = this.service.setExclude(this.getSessionId(), body.getId(), body.getExclude());

    return ResponseEntity.ok(object.toJSON().toString());
  }

  @PostMapping("/update")
  public ResponseEntity<String> update(@RequestBody @Valid EntityBody body)
  {
    SiteItem item = SiteItem.deserialize(body.getEntity());

    SiteItem result = this.service.update(this.getSessionId(), item);

    return ResponseEntity.ok(result.toJSON().toString());
  }

  @PostMapping("/remove")
  public ResponseEntity<Void> remove(@RequestBody @Valid IdBody body)
  {
    this.service.remove(this.getSessionId(), body.getId());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/removeObject")
  public ResponseEntity<Void> removeObject(@RequestBody @Valid EntityItemBody body)
  {
    this.service.removeObject(this.getSessionId(), body.getId(), body.getKey());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/remove-upload-task")
  public ResponseEntity<Void> removeUploadTask(@RequestBody @Valid UploadIdBody body)
  {
    this.service.removeUploadTask(this.getSessionId(), body.getUploadId());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/remove-task")
  public ResponseEntity<Void> removeTask(@RequestBody @Valid TaskIdBody body)
  {
    this.service.removeTask(this.getSessionId(), body.getTaskId());

    return ResponseEntity.ok(null);
  }

  @GetMapping("/tasks-count")
  public ResponseEntity<String> getTasksCount(@RequestParam(required = false, name = "statuses") String statuses)
  {
    JSONObject response = new WorkflowService().getTasksCount(this.getSessionId(), statuses);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/tasks")
  public ResponseEntity<String> getTasks(@RequestParam(required = false, name = "statuses") String statuses, @RequestParam(required = false, name = "pageNumber") Integer pageNumber, @RequestParam(required = false, name = "pageSize") Integer pageSize, @RequestParam(required = false, name = "token") Integer token)
  {
    JSONObject response = new WorkflowService().getTasks(this.getSessionId(), statuses, pageNumber, pageSize, token);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/component-tasks")
  public ResponseEntity<String> getComponentTasks(@RequestParam(required = false, name = "componentId") String componentId, @RequestParam(required = false, name = "productId") String productId)
  {
    JSONArray response = new WorkflowService().getComponentTasks(this.getSessionId(), componentId, productId);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/task")
  public ResponseEntity<String> getTask(@RequestParam(required = false, name = "id") String id)
  {
    JSONObject response = new WorkflowService().getTask(this.getSessionId(), id);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/get-upload-task")
  public ResponseEntity<String> getUploadTask(@RequestParam(required = false, name = "uploadId") String uploadId)
  {
    JSONObject response = new WorkflowService().getUploadTask(this.getSessionId(), uploadId);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/get-messages")
  public ResponseEntity<String> getMessages(@RequestParam(required = false, name = "pageNumber") Integer pageNumber, @RequestParam(required = false, name = "pageSize") Integer pageSize)
  {
    JSONObject response = new WorkflowService().getMessages(this.getSessionId(), pageNumber, pageSize);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/search")
  public ResponseEntity<String> search(@RequestParam(required = false, name = "term") String term)
  {
    List<QueryResult> list = this.service.search(this.getSessionId(), term);

    return ResponseEntity.ok(QuerySiteResult.serialize(list).toString());
  }

  @GetMapping("/get-totals")
  public ResponseEntity<String> getTotals(@RequestParam(required = false, name = "text") String text, @RequestParam(required = false, name = "filters") String filters)
  {
    JSONArray result = this.service.getTotals(this.getSessionId(), text, filters);

    return ResponseEntity.ok(result.toString());
  }

  @GetMapping("/get-stac-items")
  public ResponseEntity<String> getStacItems(@RequestParam(required = false, name = "criteria") String criteria, @RequestParam(name = "pageSize", required = true) Integer pageSize, @RequestParam(name = "pageNumber", required = true) Integer pageNumber)
  {
    Page<StacItem> page = this.service.getItems(this.getSessionId(), criteria, pageSize, pageNumber);

    return ResponseEntity.ok(page.toJSON().toString());
  }

  @GetMapping("/items")
  public ResponseEntity<String> items(@RequestParam(required = false, name = "id") String id, @RequestParam(required = false, name = "key") String key, @RequestParam(required = false, name = "conditions") String conditions)
  {
    List<TreeComponent> children = this.service.items(this.getSessionId(), id, key, conditions);

    JSONArray response = SiteItem.serialize(children);

    return ResponseEntity.ok(response.toString());
  }

  @GetMapping("/objects-presigned")
  public ResponseEntity<String> objectsPresigned(@RequestParam(required = false, name = "id") String id, @RequestParam(required = false, name = "key") String key, @RequestParam(required = false, name = "pageNumber") Long pageNumber, @RequestParam(required = false, name = "pageSize") Long pageSize)
  {
    String response = this.service.getObjectsPresigned(this.getSessionId(), id, key, pageNumber, pageSize);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/objects")
  public ResponseEntity<String> objects(@RequestParam(required = false, name = "id") String id, @RequestParam(required = false, name = "key") String key, @RequestParam(required = false, name = "pageNumber") Long pageNumber, @RequestParam(required = false, name = "pageSize") Long pageSize)
  {
    String response = this.service.getObjects(this.getSessionId(), id, key, pageNumber, pageSize);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-artifacts")
  public ResponseEntity<String> getArtifacts(@RequestParam(required = false, name = "id") String id)
  {
    JSONArray response = this.service.getArtifacts(this.getSessionId(), id);

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/remove-artifacts")
  public ResponseEntity<String> removeArtifacts(@RequestBody @Valid EntityArtifactBody body)
  {
    JSONArray response = this.service.removeArtifacts(this.getSessionId(), body.getId(), body.getProductName(), body.getFolder());

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/remove-product")
  public ResponseEntity<Void> removeProduct(@RequestBody @Valid EntityProductBody body)
  {
    this.service.removeProduct(this.getSessionId(), body.getId(), body.getProductName());

    return ResponseEntity.ok(null);
  }

  @PostMapping("/set-primary-product")
  public ResponseEntity<String> setPrimaryProduct(@RequestBody @Valid EntityProductBody body)
  {
    this.service.setPrimaryProduct(this.getSessionId(), body.getId(), body.getProductName());

    return ResponseEntity.ok(null);
  }

  @GetMapping("/download")
  public ResponseEntity<InputStreamResource> download(@RequestParam(required = false, name = "id") String id, @RequestParam(required = false, name = "key") String key, @RequestHeader(name = "Range", required = false) String range)
  {
    // Handle range requests
    if (!StringUtils.isBlank(range))
    {
      try
      {
        final List<Range> ranges = Range.decodeRange(range);

        return this.getRemoteFile(this.service.download(this.getSessionId(), id, key, ranges));
      }
      catch (InvalidRangeException e)
      {
        return new ResponseEntity<InputStreamResource>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
      }
    }

    return this.getRemoteFile(this.service.download(this.getSessionId(), id, key, true));
  }

  @GetMapping("/downloadProductPreview")
  public ResponseEntity<InputStreamResource> downloadProductPreview(@RequestParam(required = false, name = "productId") String productId, @RequestParam(required = false, name = "artifactName") String artifactName)
  {
    InputStream istream = this.service.downloadProductPreview(this.getSessionId(), productId, artifactName);

    return ResponseEntity.ok() //
        .header("Content-Type", "image/png") //
        .header("Content-Disposition", "attachment; filename=\"" + artifactName + "_preview.png\"") //
        .body(new InputStreamResource(istream));
  }

  @GetMapping("/download-last")
  public ResponseEntity<InputStreamResource> downloadLast(@RequestParam(required = false, name = "id") String id, @RequestParam(required = false, name = "key") String key)
  {
    return this.getRemoteFile(this.service.downloadLast(this.getSessionId(), id, key, true));
  }

  @GetMapping("/download-file")
  public ResponseEntity<InputStreamResource> downloadFile(@RequestParam(required = false, name = "url") String url)
  {
    return this.getRemoteFile(this.service.proxyRemoteFile(this.getSessionId(), url));
  }

  @GetMapping("/features")
  public ResponseEntity<String> features(@RequestParam(required = false, name = "conditions") String conditions) throws IOException
  {
    JSONObject response = new JSONObject();
    response.put("features", this.service.features(this.getSessionId(), conditions));
    response.put("bbox", this.service.bbox(this.getSessionId()));

    return ResponseEntity.ok(response.toString());
  }

  @PostMapping("/upload")
  public ResponseEntity<String> upload(@Valid @ModelAttribute UploadArtifactFileBody body) throws IOException
  {
    MultipartFile file = body.getFile();

    try (InputStream stream = file.getInputStream())
    {
      final BasicFileMetadata metadata = new BasicFileMetadata(file.getContentType(), file.getSize());
      final String fileName = file.getName();

      JSONObject response = this.service.putFile(this.getSessionId(), body.getId(), body.getFolder(), body.getProductName(), fileName, metadata, stream);

      return ResponseEntity.ok(response.toString());
    }
  }
}
