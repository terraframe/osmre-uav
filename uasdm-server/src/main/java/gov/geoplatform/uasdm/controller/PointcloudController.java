package gov.geoplatform.uasdm.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.remote.RemoteFileGetResponse;
import gov.geoplatform.uasdm.service.ProjectManagementService;

@Controller(url = "pointcloud")
public class PointcloudController
{
  public static final String       JSP_DIR   = "/WEB-INF/";

  public static final String       POTREE_JSP = "gov/osmre/uasdm/potree/potree.jsp";
  
  public static final String       POTREE_RESOURCES = "gov/osmre/uasdm/potree/potree";
  
  private ProjectManagementService service = new ProjectManagementService();
  
  @Endpoint(url = "resource\\/.*", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF resource(ClientRequestIF request, ServletRequestIF servletRequest)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile(".*pointcloud\\/resource\\/(.*)$", Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String resourcePath = matcher.group(1);
      
      ViewResponse resp = new ViewResponse(JSP_DIR + POTREE_RESOURCES + "/" + resourcePath);
      
      return resp;
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
  
  @Endpoint(url = ".+\\/potree$", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF potree(ClientRequestIF request, ServletRequestIF servletRequest)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile(".*pointcloud\\/(.+)\\/potree$", Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String componentId = matcher.group(1);
      
      ViewResponse resp = new ViewResponse(JSP_DIR + POTREE_JSP);
      
      resp.set("componentId", componentId);
      resp.set("productName", this.service.getComponentName(request.getSessionId(), componentId));
      
      return resp;
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
  
  @Endpoint(url = ".+\\/cloud\\.js$", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF cloudJs(ClientRequestIF request, ServletRequestIF servletRequest)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile(".*pointcloud\\/(.+)\\/cloud\\.js$", Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String componentId = matcher.group(1);
      
      return new RemoteFileGetResponse(this.service.download(request.getSessionId(), componentId, Product.ODM_ALL_DIR + "/potree/cloud.js"));
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
  
  @Endpoint(url = ".+\\/data\\/.*", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF data(ClientRequestIF request, ServletRequestIF servletRequest)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile(".*pointcloud\\/(.+)\\/data\\/(.*)$", Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String componentId = matcher.group(1);
      
      String dataPath = matcher.group(2);
      
      return new RemoteFileGetResponse(this.service.download(request.getSessionId(), componentId, Product.ODM_ALL_DIR + "/potree/data/" + dataPath));
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
}
