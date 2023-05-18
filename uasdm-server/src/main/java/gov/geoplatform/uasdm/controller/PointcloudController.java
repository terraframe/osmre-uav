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

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileGetResponse;
import gov.geoplatform.uasdm.service.ProjectManagementService;

@Controller(url = "pointcloud")
public class PointcloudController
{
  public static final String       JSP_DIR   = "/WEB-INF/";

  public static final String       POTREE_JSP = "gov/osmre/uasdm/potree/potree.jsp";
  
  public static final String       POTREE_RESOURCES = "gov/osmre/uasdm/potree/potree";
  
  /**
   * Support for the "legacy" older 'potree' data format, as opposed to the newer 'entwine' format. Older versions of ODM
   * used to generate pointcloud data in this format. 
   */
  public static final String       LEGACY_POTREE_SUPPORT = Product.ODM_ALL_DIR + "/potree";
  
  private ProjectManagementService service = new ProjectManagementService();
  
  /**
   * Serves resource requests from the Potree Viewer for files additional resources like CSS, javascript, files.
   * These files are typically pulled from the potree build directory, which is produced at build time.
   * 
   * @param request
   * @param servletRequest
   * @return
   */
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
  
  /**
   * Primary endpoint which serves up the Potree Viewer JSP page.
   * 
   * @param request
   * @param servletRequest
   * @return
   */
  @Endpoint(url = ".+\\/potree$", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF potreeViewer(ClientRequestIF request, ServletRequestIF servletRequest)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile(".*pointcloud\\/(.+)\\/potree$", Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String componentId = matcher.group(1);
      
      ViewResponse resp = new ViewResponse(JSP_DIR + POTREE_JSP);
      
      UasComponent component = this.service.getComponent(request.getSessionId(), componentId);
      
      resp.set("componentId", componentId);
      resp.set("productName", component.getName());
      
      if (RemoteFileFacade.objectExists(component.getS3location() + ODMZipPostProcessor.POTREE + "/ept.json"))
      {
        resp.set("pointcloudLoadPath", "ept.json");
      }
      else if (RemoteFileFacade.objectExists(component.getS3location() + ODMZipPostProcessor.POTREE + "/metadata.json"))
      {
        resp.set("pointcloudLoadPath", "metadata.json");
      }
      else if (RemoteFileFacade.objectExists(component.getS3location() + LEGACY_POTREE_SUPPORT + "/cloud.js"))
      {
        resp.set("pointcloudLoadPath", "legacypotree/cloud.js");
      }
      else
      {
        resp.set("noData", "true");
      }
      
      return resp;
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
  
  /**
   * Serves requests for data by the Potree Viewer and fullfills the requests by fetching data from S3.
   * 
   * @param request
   * @param servletRequest
   * @return
   */
  @Endpoint(url = ".+\\/data\\/.*", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF data(ClientRequestIF request, ServletRequestIF servletRequest)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile(".*pointcloud\\/([^\\/]+)\\/data(?:\\/legacypotree)?\\/(.*)$", Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String componentId = matcher.group(1);
      
      String dataPath = matcher.group(2);
      
      if (url.contains("legacypotree/"))
      {
        return new RemoteFileGetResponse(this.service.download(request.getSessionId(), componentId, LEGACY_POTREE_SUPPORT + "/" + dataPath, true));
      }
      else
      {
        return new RemoteFileGetResponse(this.service.download(request.getSessionId(), componentId, ODMZipPostProcessor.POTREE + "/" + dataPath, true));
      }
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
}
