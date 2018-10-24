package gov.osmre.uasdm;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ResponseIF;
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

}
