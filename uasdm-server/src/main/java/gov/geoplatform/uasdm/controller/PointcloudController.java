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
import java.net.FileNameMap;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.PointcloudService;
import gov.geoplatform.uasdm.service.ProjectManagementService;

@Controller
@RequestMapping("/api/pointcloud")
public class PointcloudController extends AbstractController
{
  public static final String       JSP_DIR          = "/WEB-INF/";

  public static final String       POTREE_JSP       = "gov/osmre/uasdm/potree/potree.jsp";

  public static final String       POTREE_RESOURCES = "gov/osmre/uasdm/potree/potree";

  private PointcloudService        service          = new PointcloudService();

  private ProjectManagementService pService         = new ProjectManagementService();

  /**
   * Serves resource requests from the Potree Viewer for files additional
   * resources like CSS, javascript, files. These files are typically pulled
   * from the potree build directory, which is produced at build time.
   * 
   * @param request
   * @param servletRequest
   * @return
   * @throws IOException
   * @throws URISyntaxException
   */
  @GetMapping("/resource/**")
  public ResponseEntity<Resource> resource() throws IOException, URISyntaxException
  {
    HttpServletRequest request = this.getRequest();

    Pattern pattern = Pattern.compile(".*pointcloud\\/resource\\/(.*)$", Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(request.getRequestURI());

    if (matcher.find())
    {
      String resourcePath = matcher.group(1);

      URL url = this.getContext().getResource(JSP_DIR + POTREE_RESOURCES + "/" + resourcePath);
      Resource resource = new UrlResource(url.toURI());
      Path path = Paths.get(url.toURI());

      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      String contentType = fileNameMap.getContentTypeFor(path.getFileName().toString());

      return ResponseEntity.ok().header("Content-Type", contentType).body(resource);
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
  @GetMapping("/potree/{componentId}/{productName}")
  public ModelAndView potreeViewer(@PathVariable("componentId") String componentId, @PathVariable("productName") String productName)
  {
    String resource = this.service.getPointcloudResource(this.getSessionId(), componentId, productName);

    ModelAndView mav = new ModelAndView(JSP_DIR + POTREE_JSP);
    mav.addObject("componentId", componentId);
    mav.addObject("productName", productName);

    if (resource != null)
    {
      mav.addObject("pointcloudLoadPath", resource);
    }
    else
    {
      mav.addObject("noData", "true");
    }

    return mav;
  }

  /**
   * Serves requests for data by the Potree Viewer and fullfills the requests by
   * fetching data from S3.
   * 
   * @param request
   * @param servletRequest
   * @return
   */
  @GetMapping("/data/**")
  public ResponseEntity<InputStreamResource> data(@RequestHeader(name = "Range", required = false) String range)
  {
    Pattern pattern = Pattern.compile(".*pointcloud\\/data\\/([^\\/]+)(?:\\/legacypotree)?\\/(.*)$", Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(this.getRequest().getRequestURI());

    if (matcher.find())
    {
      String componentId = matcher.group(1);
      String dataPath = matcher.group(2);

      RemoteFileObject file = null;

      if (!StringUtils.isBlank(range))
      {
        file = this.pService.download(this.getSessionId(), componentId, dataPath, range);
      }
      else
      {
        file = this.pService.download(this.getSessionId(), componentId, dataPath, true);
      }

      RemoteFileMetadata metadata = file.getObjectMetadata();
      String contentDisposition = metadata.getContentDisposition();

      if (contentDisposition == null)
      {
        contentDisposition = "attachment; filename=\"" + file.getName() + "\"";
      }

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Content-Type", metadata.getContentType());
      httpHeaders.set("Content-Encoding", metadata.getContentEncoding());
      httpHeaders.set("Content-Disposition", contentDisposition);
      httpHeaders.set("Content-Length", Long.toString(metadata.getContentLength()));
      httpHeaders.set("ETag", metadata.getETag());

      if (metadata.getLastModified() != null)
      {
        httpHeaders.setDate("Last-Modified", Date.from(metadata.getLastModified()).getTime());
      }

      if (!StringUtils.isBlank(range))
      {
        httpHeaders.set("Cache-Control", "no-store");
      }

      return new ResponseEntity<InputStreamResource>(new InputStreamResource(file.getObjectContent()), httpHeaders, HttpStatus.OK);
    }
    else
    {
      throw new ProgrammingErrorException("Could not match regex against provided url.");
    }
  }
}