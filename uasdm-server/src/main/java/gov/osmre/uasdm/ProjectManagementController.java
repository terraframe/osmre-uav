package gov.osmre.uasdm;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
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

  private JSONObject toNode(String oid, String name, boolean hasChildren)
  {
    JSONObject child = new JSONObject();
    child.put("id", oid);
    child.put("name", name);
    child.put("hasChildren", hasChildren);
    return child;
  }

}
