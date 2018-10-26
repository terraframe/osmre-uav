package gov.osmre.uasdm;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;

@Controller(url = "project")
public class ProjectManagementController
{
  public static final String JSP_DIR   = "/WEB-INF/";

  public static final String INDEX_JSP = "gov/osmre/uasdm/index.jsp";

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF management()
  {
    return new ViewResponse(JSP_DIR + INDEX_JSP);
  }

  @Endpoint(url = "get-children", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getChildren(@RequestParamter(name = "id") String id)
  {
    JSONArray children = new JSONArray();
    children.put(this.toNode("2", "Child-1.1", false));
    children.put(this.toNode("3", "Child-1.2", false));

    return new RestBodyResponse(children);
  }

  @Endpoint(url = "roots", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getRoots()
  {
    JSONArray roots = new JSONArray();
    roots.put(this.toNode("1", "Root", true));

    return new RestBodyResponse(roots);
  }

  @Endpoint(url = "new-child", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newChild(@RequestParamter(name = "id") String id)
  {
    return new RestBodyResponse(this.toNode(UUID.randomUUID().toString(), "", false));
  }

  @Endpoint(url = "apply-with-parent", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applyWithParent(@RequestParamter(name = "entity") String entity, @RequestParamter(name = "parentId") String parentId)
  {
    JSONObject object = new JSONObject(entity);
    String id = object.getString("id");
    String name = object.getString("name");

    return new RestBodyResponse(this.toNode(id, name, false));
  }

  @Endpoint(url = "edit", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(@RequestParamter(name = "id") String id)
  {
    return new RestBodyResponse(this.toNode(id, "Test", true));
  }

  @Endpoint(url = "update", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF update(@RequestParamter(name = "entity") String entity)
  {
    JSONObject object = new JSONObject(entity);
    String id = object.getString("id");
    String name = object.getString("name");
    boolean hasChildren = object.getBoolean("hasChildren");

    return new RestBodyResponse(this.toNode(id, name, hasChildren));
  }

  @Endpoint(url = "remove", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(@RequestParamter(name = "id") String id)
  {
    return new RestResponse();
  }

  private JSONObject toNode(String oid, String name, boolean hasChildren)
  {
    JSONObject child = new JSONObject();
    child.put("id", oid);
    child.put("name", name);
    child.put("hasChildren", hasChildren);
    return child;
  }

}
