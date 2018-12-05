package gov.geoplatform.uasdm;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.service.WorkflowService;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;

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
    List<SiteItem> children = this.service.getChildren(request.getSessionId(), id);

    return new RestBodyResponse(SiteItem.serialize(children));
  }

  @Endpoint(url = "roots", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getRoots(ClientRequestIF request)
  {
    List<SiteItem> children = this.service.getRoots(request.getSessionId());

    return new RestBodyResponse(SiteItem.serialize(children));
  }

  @Endpoint(url = "new-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newChild(ClientRequestIF request, @RequestParamter(name = "parentId") String parentId)
  {
    SiteItem item = this.service.newChild(request.getSessionId(), parentId);

    return new RestBodyResponse(item.toJSON());
  }

  @Endpoint(url = "apply-with-parent", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applyWithParent(ClientRequestIF request, @RequestParamter(name = "entity") String entity, @RequestParamter(name = "parentId") String parentId)
  {
    SiteItem item = SiteItem.deserialize(new JSONObject(entity));

    SiteItem result = this.service.applyWithParent(request.getSessionId(), item, parentId);

    return new RestBodyResponse(result.toJSON());
  }

  @Endpoint(url = "edit", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "id") String id)
  {
    SiteItem item = this.service.edit(request.getSessionId(), id);

    return new RestBodyResponse(item.toJSON());
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

  @Endpoint(url = "tasks", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTasks(ClientRequestIF request)
  {
    JSONObject response = new WorkflowService().getTasks(request.getSessionId());

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "items", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF items(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    if (key == null || key.length() == 0)
    {
      List<SiteItem> children = this.service.getChildren(request.getSessionId(), id);

      for (SiteItem child : children)
      {
        if (child.getType().equals("Collection") || child.getType().equals("Mission"))
        {
          child.setHasChildren(true);
        }
      }

      List<SiteObject> objects = this.service.getItems(request.getSessionId(), id, null);

      JSONArray response = SiteItem.serialize(children);

      for (SiteObject object : objects)
      {
        response.put(object.toJSON());
      }

      return new RestBodyResponse(response);
    }

    List<SiteObject> objects = this.service.getItems(request.getSessionId(), id, key);

    return new RestBodyResponse(SiteObject.serialize(objects));
  }

  @Endpoint(url = "download", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF download(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "key") String key)
  {
    return new S3GetResponse(this.service.download(request.getSessionId(), id, key));
  }
}
