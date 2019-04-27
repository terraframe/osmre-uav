package gov.geoplatform.uasdm;

import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.service.WorkflowService;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.TreeComponent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
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

@Controller(url = "project")
public class ProjectManagementController
{
  public static final String       JSP_DIR   = "/WEB-INF/";

  public static final String       INDEX_JSP = "gov/osmre/uasdm/index.jsp";

  private ProjectManagementService service;

  public ProjectManagementController()
  {
    this.service = new ProjectManagementService();
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF management()
  {
    return new ViewResponse(JSP_DIR + INDEX_JSP);
  }

  @Endpoint(url = "get-children", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getChildren(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    List<TreeComponent> children = this.service.getChildren(request.getSessionId(), id);

    return new RestBodyResponse(SiteItem.serialize(children));
  }

  @Endpoint(url = "roots", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getRoots(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    List<TreeComponent> roots = this.service.getRoots(request.getSessionId(), id);

    return new RestBodyResponse(SiteItem.serialize(roots));
  }

  @Endpoint(url = "new-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newChild(ClientRequestIF request, @RequestParamter(name = "parentId") String parentId)
  {
    SiteItem item = this.service.newChild(request.getSessionId(), parentId);

    RestResponse response = new RestResponse();
    response.set("item", item.toJSON());
    response.set("attributes", AttributeType.toJSON(item.getAttributes()));
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
  public ResponseIF downloadAll(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    // Ideally I should be able to write these bytes directly to the ServletOutputStream but we'll be dumb and waste memory with InputStreams because its supported by Runway mvc.
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    
    this.service.downloadAll(request.getSessionId(), id, key, bao);

    return new InputStreamResponse(new ByteArrayInputStream(bao.toByteArray()), "application/zip");
  }
  
  @Endpoint(url = "run-ortho", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF runOrtho(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    this.service.runOrtho(request.getSessionId(), id);

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

  @Endpoint(url = "remove-task", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeTask(ClientRequestIF request, @RequestParamter(name = "uploadId") String uploadId)
  {
    this.service.removeTask(request.getSessionId(), uploadId);

    return new RestResponse();
  }

  @Endpoint(url = "tasks", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTasks(ClientRequestIF request)
  {
    JSONObject response = new WorkflowService().getTasks(request.getSessionId());

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "task", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTask(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    JSONObject response = new WorkflowService().getTask(request.getSessionId(), id);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "missing-metadata", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getMissingMetadata(ClientRequestIF request)
  {
    JSONArray response = new WorkflowService().getMissingMetadata(request.getSessionId());

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "search", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF search(ClientRequestIF request, @RequestParamter(name = "term") String term)
  {
    List<QueryResult> list = this.service.search(request.getSessionId(), term);

    return new RestBodyResponse(QueryResult.serialize(list));
  }

  @Endpoint(url = "items", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF items(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    List<TreeComponent> children = this.service.items(request.getSessionId(), id, key);

    JSONArray response = SiteItem.serialize(children);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "download", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF download(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    return new S3GetResponse(this.service.download(request.getSessionId(), id, key));
  }

  @Endpoint(url = "features", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF features(ClientRequestIF request) throws IOException
  {
    RestResponse response = new RestResponse();
    response.set("features", this.service.features(request.getSessionId()));
    response.set("bbox", this.service.bbox(request.getSessionId()));

    return response;
  }
}
