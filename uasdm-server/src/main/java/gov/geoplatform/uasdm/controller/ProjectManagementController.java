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
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.model.InvalidRangeException;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.remote.BasicFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileGetRangeResponse;
import gov.geoplatform.uasdm.remote.RemoteFileGetResponse;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.service.WorkflowService;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.QuerySiteResult;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.TreeComponent;

@Controller(url = "project")
public class ProjectManagementController
{
  public static final String       JSP_DIR   = "/WEB-INF/";

  public static final String       INDEX_JSP = "gov/osmre/uasdm/index.jsp";

  private static final Logger      logger    = LoggerFactory.getLogger(ProjectManagementController.class);

  private ProjectManagementService service;

  public ProjectManagementController()
  {
    this.service = new ProjectManagementService();
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF management()
  {
    return new ViewResponse("/index.html");
  }

  @Endpoint(method = ServletMethod.GET, url = "configuration")
  public ResponseIF configuration(ServletRequestIF request, ClientRequestIF cRequest, @RequestParamter(name = "id") String id)
  {
    JSONObject configuration = this.service.configuration(cRequest.getSessionId(), request.getContextPath());

    return new RestBodyResponse(configuration);
  }

  @Endpoint(url = "get-default-run-config", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getDefaultRunConfig(ClientRequestIF request, @RequestParamter(name = "collectionId") String collectionId)
  {
    String config = this.service.getDefaultRunConfig(request.getSessionId(), collectionId);

    return new RestBodyResponse(config);
  }

  @Endpoint(url = "get-odm-run-by-task", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getODMRun(ClientRequestIF request, @RequestParamter(name = "taskId") String taskId) throws IOException
  {
    return new RestBodyResponse(service.getODMRunByTask(request.getSessionId(), taskId));
  }

  @Endpoint(url = "get-configuration-by-task", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getConfigurationByTask(ClientRequestIF request, @RequestParamter(name = "taskId") String taskId) throws IOException
  {
    ProcessConfiguration configuration = service.getConfigurationByTask(request.getSessionId(), taskId);

    return new RestBodyResponse(configuration.toJson().toString());
  }

  @Endpoint(url = "get-children", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getChildren(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    List<TreeComponent> children = this.service.getChildren(request.getSessionId(), id);

    return new RestBodyResponse(SiteItem.serialize(children));
  }

  @Endpoint(url = "view", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF view(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    JSONObject response = this.service.view(request.getSessionId(), id);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "roots", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getRoots(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "conditions") String conditions, @RequestParamter(name = "sort") String sort)
  {
    List<TreeComponent> roots = this.service.getRoots(request.getSessionId(), id, conditions, sort);

    return new RestBodyResponse(SiteItem.serialize(roots));
  }

  @Endpoint(url = "metadata-options", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getMetadataOptions(ClientRequestIF request, @RequestParamter(name = "collectionId") String collectionId, @RequestParamter(name = "productId") String productId)
  {
    return new RestBodyResponse(this.service.getMetadataOptions(request.getSessionId(), collectionId, productId));
  }

  @Endpoint(url = "uav-metadata", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getUAVMetadata(ClientRequestIF request, @RequestParamter(name = "uavId") String uavId, @RequestParamter(name = "sensorId") String sensorId)
  {
    return new RestBodyResponse(this.service.getUAVMetadata(request.getSessionId(), uavId, sensorId));
  }

  @Endpoint(url = "new-default-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newDefaultChild(ClientRequestIF request, @RequestParamter(name = "parentId") String parentId)
  {
    SiteItem item = this.service.newDefaultChild(request.getSessionId(), parentId);

    RestResponse response = new RestResponse();
    response.set("item", item.toJSON());
    response.set("attributes", AttributeType.toJSON(item.getAttributes()));
    return response;
  }

  @Endpoint(url = "new-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newChild(ClientRequestIF request, @RequestParamter(name = "parentId") String parentId, @RequestParamter(name = "type") String childType)
  {
    SiteItem item = this.service.newChild(request.getSessionId(), parentId, childType);

    RestResponse response = new RestResponse();
    response.set("item", item.toJSON());
    response.set("attributes", AttributeType.toJSON(item.getAttributes()));
    return response;
  }

  @Endpoint(url = "apply-metadata", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applyMetadata(ClientRequestIF request, @RequestParamter(name = "selection") String selection)
  {
    this.service.applyMetadata(request.getSessionId(), selection);

    return new RestResponse();
  }

  @Endpoint(url = "create-collection", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF createCollection(ClientRequestIF request, @RequestParamter(name = "selections") String selections)
  {
    String oid = this.service.createCollection(request.getSessionId(), selections);

    RestResponse response = new RestResponse();
    response.set("oid", oid);

    return response;
  }

  @Endpoint(url = "create-standalone-product-group", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF createStandaloneProductGroup(ClientRequestIF request, @RequestParamter(name = "productGroup") String productGroupJson)
  {
    String oid = this.service.createStandaloneProductGroup(request.getSessionId(), productGroupJson);

    RestResponse response = new RestResponse();
    response.set("oid", oid);

    return response;
  }

  @Endpoint(url = "apply-with-parent", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applyWithParent(ClientRequestIF request, @RequestParamter(name = "entity") String entity, @RequestParamter(name = "parentId") String parentId)
  {
    SiteItem item = SiteItem.deserialize(new JSONObject(entity));

    SiteItem result = this.service.applyWithParent(request.getSessionId(), item, parentId);

    return new RestBodyResponse(result.toJSON());
  }

  @Endpoint(url = "download-all", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadAll(ClientRequestIF request, final @RequestParamter(name = "id") String id, final @RequestParamter(name = "key") String key)
  {
    final String sessionId = request.getSessionId();

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

      return new InputStreamResponse(istream, "application/zip", "\"" + name + ".zip" + "\"");
    }
    catch (IOException e)
    {
      throw new RuntimeException("Test");
    }
  }

  @Endpoint(url = "download-odm-all", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadOdmAll(ClientRequestIF request, final @RequestParamter(name = "colId") String colId)
  {
    final String sessionId = request.getSessionId();

    return new RemoteFileGetResponse(this.service.downloadOdmAll(sessionId, colId));
  }

  @Endpoint(url = "download-report", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadReport(ClientRequestIF request, final @RequestParamter(name = "colId") String colId, final @RequestParamter(name = "productName") String productName, final @RequestParamter(name = "folder") String folder)
  {
    final String sessionId = request.getSessionId();

    return new RemoteFileGetResponse(this.service.downloadReport(sessionId, colId, productName, folder));
  }

  @Endpoint(url = "run-process", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF runProcess(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "configuration") String configuration)
  {
    this.service.runOrtho(request.getSessionId(), id, configuration);

    return new RestResponse();
  }

  @Endpoint(url = "edit", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    SiteItem item = this.service.edit(request.getSessionId(), id);

    RestResponse response = new RestResponse();
    response.set("item", item.toJSON());
    response.set("attributes", AttributeType.toJSON(item.getAttributes()));
    return response;
  }

  @Endpoint(url = "set-exclude", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF setExclude(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "exclude") Boolean exclude)
  {
    SiteObject object = this.service.setExclude(request.getSessionId(), id, exclude);

    return new RestBodyResponse(object.toJSON());
  }

  @Endpoint(url = "update", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF update(ClientRequestIF request, @RequestParamter(name = "entity") String entity)
  {
    SiteItem item = SiteItem.deserialize(new JSONObject(entity));

    SiteItem result = this.service.update(request.getSessionId(), item);

    return new RestBodyResponse(result.toJSON());
  }

  @Endpoint(url = "remove", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    this.service.remove(request.getSessionId(), id);

    return new RestResponse();
  }

  @Endpoint(url = "removeObject", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeObject(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    this.service.removeObject(request.getSessionId(), id, key);

    return new RestResponse();
  }

  @Endpoint(url = "remove-upload-task", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeUploadTask(ClientRequestIF request, @RequestParamter(name = "uploadId") String uploadId)
  {
    this.service.removeUploadTask(request.getSessionId(), uploadId);

    return new RestResponse();
  }

  @Endpoint(url = "remove-task", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeTask(ClientRequestIF request, @RequestParamter(name = "taskId") String taskId)
  {
    this.service.removeTask(request.getSessionId(), taskId);

    return new RestResponse();
  }

  @Endpoint(url = "tasks-count", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTasksCount(ClientRequestIF request, @RequestParamter(name = "statuses") String statuses)
  {
    JSONObject response = new WorkflowService().getTasksCount(request.getSessionId(), statuses);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "tasks", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTasks(ClientRequestIF request, @RequestParamter(name = "statuses") String statuses, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "token") Integer token)
  {
    JSONObject response = new WorkflowService().getTasks(request.getSessionId(), statuses, pageNumber, pageSize, token);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "component-tasks", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getComponentTasks(ClientRequestIF request, @RequestParamter(name = "componentId") String componentId, @RequestParamter(name = "productId") String productId)
  {
    JSONArray response = new WorkflowService().getComponentTasks(request.getSessionId(), componentId, productId);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "task", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTask(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    JSONObject response = new WorkflowService().getTask(request.getSessionId(), id);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "get-upload-task", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getUploadTask(ClientRequestIF request, @RequestParamter(name = "uploadId") String uploadId)
  {
    JSONObject response = new WorkflowService().getUploadTask(request.getSessionId(), uploadId);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "get-messages", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getMessages(ClientRequestIF request, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize)
  {
    JSONObject response = new WorkflowService().getMessages(request.getSessionId(), pageNumber, pageSize);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "search", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF search(ClientRequestIF request, @RequestParamter(name = "term") String term)
  {
    List<QueryResult> list = this.service.search(request.getSessionId(), term);

    return new RestBodyResponse(QuerySiteResult.serialize(list));
  }

  @Endpoint(url = "get-totals", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTotals(ClientRequestIF request, @RequestParamter(name = "text") String text, @RequestParamter(name = "filters") String filters)
  {
    JSONArray result = this.service.getTotals(request.getSessionId(), text, filters);

    return new RestBodyResponse(result);
  }

  @Endpoint(url = "get-stac-items", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getStacItems(ClientRequestIF request, @RequestParamter(name = "criteria") String criteria, @RequestParamter(name = "pageSize", required = true) Integer pageSize, @RequestParamter(name = "pageNumber", required = true) Integer pageNumber)
  {
    Page<StacItem> page = this.service.getItems(request.getSessionId(), criteria, pageSize, pageNumber);

    return new RestBodyResponse(page.toJSON());
  }

  @Endpoint(url = "items", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF items(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key, @RequestParamter(name = "conditions") String conditions)
  {
    List<TreeComponent> children = this.service.items(request.getSessionId(), id, key, conditions);

    JSONArray response = SiteItem.serialize(children);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "objects-presigned", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF objectsPresigned(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key, @RequestParamter(name = "pageNumber") Long pageNumber, @RequestParamter(name = "pageSize") Long pageSize)
  {
    String response = this.service.getObjectsPresigned(request.getSessionId(), id, key, pageNumber, pageSize);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "objects", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF objects(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key, @RequestParamter(name = "pageNumber") Long pageNumber, @RequestParamter(name = "pageSize") Long pageSize)
  {
    String response = this.service.getObjects(request.getSessionId(), id, key, pageNumber, pageSize);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "get-artifacts", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getArtifacts(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    JSONArray response = this.service.getArtifacts(request.getSessionId(), id);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "remove-artifacts", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeArtifacts(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "productName") String productName, @RequestParamter(name = "folder") String folder)
  {
    JSONArray response = this.service.removeArtifacts(request.getSessionId(), id, productName, folder);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "remove-product", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeProduct(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "productName") String productName)
  {
    this.service.removeProduct(request.getSessionId(), id, productName);

    return new RestResponse();
  }

  @Endpoint(url = "set-primary-product", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF setPrimaryProduct(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "productName") String productName)
  {
    this.service.setPrimaryProduct(request.getSessionId(), id, productName);

    return new RestResponse();
  }

  @Endpoint(url = "download", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF download(ClientRequestIF request, ServletRequestIF sRequest, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    // Handle range requests
    if (sRequest.getHeader("Range") != null)
    {
      String range = sRequest.getHeader("Range");

      try
      {
        final List<Range> ranges = Range.decodeRange(range);

        return new RemoteFileGetRangeResponse(this.service.download(request.getSessionId(), id, key, ranges));
      }
      catch (InvalidRangeException e)
      {
        return new ErrorCodeResponse(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "Invalid range [" + e.getRange() + "]");
      }
    }

    return new RemoteFileGetResponse(this.service.download(request.getSessionId(), id, key, true));
  }

  @Endpoint(url = "downloadProductPreview", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadProductPreview(ClientRequestIF request, ServletRequestIF sRequest, @RequestParamter(name = "productId") String productId, @RequestParamter(name = "artifactName") String artifactName)
  {
    return new InputStreamResponse(this.service.downloadProductPreview(request.getSessionId(), productId, artifactName), "image/png", artifactName + "_preview.png");
  }

  @Endpoint(url = "download-last", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadLast(ClientRequestIF request, ServletRequestIF sRequest, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    return new RemoteFileGetResponse(this.service.downloadLast(request.getSessionId(), id, key, true));
  }

  @Endpoint(url = "download-file", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadFile(ClientRequestIF request, @RequestParamter(name = "url") String url)
  {
    return new RemoteFileGetResponse(this.service.proxyRemoteFile(request.getSessionId(), url));
  }

  @Endpoint(url = "features", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF features(ClientRequestIF request, @RequestParamter(name = "conditions") String conditions) throws IOException
  {
    RestResponse response = new RestResponse();
    response.set("features", this.service.features(request.getSessionId(), conditions));
    response.set("bbox", this.service.bbox(request.getSessionId()));

    return response;
  }

  @Endpoint(url = "upload", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF upload(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "folder") String folder, @RequestParamter(name = "productName") String productName, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException
  {
    try (InputStream stream = file.getInputStream())
    {
      final BasicFileMetadata metadata = new BasicFileMetadata(file.getContentType(), file.getSize());
      final String fileName = file.getFilename();

      JSONObject response = this.service.putFile(request.getSessionId(), id, folder, productName, fileName, metadata, stream);

      return new RestBodyResponse(response);
    }
  }
}
