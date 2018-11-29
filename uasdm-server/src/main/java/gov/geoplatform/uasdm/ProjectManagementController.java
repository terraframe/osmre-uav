package gov.geoplatform.uasdm;

import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;

import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteItem;

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
    List<WorkflowTask> tasks = this.service.getTasks(request.getSessionId());

    return new RestBodyResponse(WorkflowTask.serialize(tasks));
  }
}
