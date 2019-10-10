package gov.geoplatform.uasdm.controller;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.S3GetResponse;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.service.ProductService;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.service.WorkflowService;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.ProductView;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.TreeComponent;

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
  public ResponseIF getRoots(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "bounds") String bounds)
  {
    List<TreeComponent> roots = this.service.getRoots(request.getSessionId(), id, bounds);

    return new RestBodyResponse(SiteItem.serialize(roots));
  }

  @Endpoint(url = "metadata-options", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getMetadataOptions(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    return new RestBodyResponse(this.service.getMetadataOptions(request.getSessionId(), id));
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

            ProjectManagementController.this.service.downloadAll(sessionId, id, key, ostream);
          }
          catch (Exception e)
          {
            // Handle error message
          }
        }
      });
      thread.setDaemon(true);
      thread.start();

      return new InputStreamResponse(istream, "application/zip", name.toString().replaceAll("\\s+","") + ".zip");
    }
    catch (IOException e)
    {
      throw new RuntimeException("Test");
    }
  }

  @Endpoint(url = "run-ortho", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF runOrtho(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "excludes") String excludes)
  {
    this.service.runOrtho(request.getSessionId(), id, excludes);

    return new RestResponse();
  }

  @Endpoint(url = "submit-metadata", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF submitMetadata(ClientRequestIF request, @RequestParamter(name = "json") String json)
  {
    this.service.submitMetadata(request.getSessionId(), json);

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

  @Endpoint(url = "products", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF products(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    ProductService service = new ProductService();
    List<ProductView> products = service.getProducts(request.getSessionId(), id);

    return new RestBodyResponse(ProductView.serialize(products));
  }
}
