package gov.geoplatform.uasdm.controller;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.request.ServletRequestIF;

import gov.geoplatform.uasdm.service.CloudOptimizedGeoTiffService;

@Controller(url = "cog")
public class CloudOptimizedGeoTiffController
{
  private static final Logger logger = LoggerFactory.getLogger(ProjectManagementController.class);

  private CloudOptimizedGeoTiffService service;
  
  public CloudOptimizedGeoTiffController()
  {
    this.service = new CloudOptimizedGeoTiffService();
  }
  
  /**
   * Path is the S3 path to the object, excluding any bucket information. An example of this is:
   * mysite/myproject/mymission/mycollection/ortho/odm_orthophoto.tif
   * 
   * For more information, view the public TiTiler docs at:
   * https://k67ob0ncba.execute-api.us-east-1.amazonaws.com
   * 
   * @param request
   * @param sRequest
   * @param path
   * @return
   */
  @Endpoint(url = "tilejson.json", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF tilejson(ClientRequestIF request, ServletRequestIF sRequest, @RequestParamter(name = "path") String path)
  {
    JSONObject tilejson = this.service.tilejson(request.getSessionId(), sRequest.getContextPath(), path);
    
    return new RestBodyResponse(tilejson);
  }
  
  /**
   * In keeping in alignment with the TiTiler API, we have embedded a few parameters into the pathing of this URL.
   * 
   * cog/tiles/{TileMatrixSetId}/{z}/{x}/{y}@{scale}x.{format}
   * 
   * We have one parameter which is non-standard to the TiTiler API, path. Path is the S3 path to the object, excluding
   * any bucket information. An example of this is: mysite/myproject/mymission/mycollection/ortho/odm_orthophoto.tif
   * 
   * The rest of the parameters for this method are directly proxied to TiTiler. You can find documentation for them on
   * our public TiTiler API, at:  https://k67ob0ncba.execute-api.us-east-1.amazonaws.com
   * 
   * @param request
   * @param servletRequest
   * @param path
   * @return
   */
  public static final String TILES_REGEX = "tiles\\/(.+\\/)?(\\d+)\\/(\\d+)\\/(\\d+)(@\\d+x)?(\\.[^?\\n\\r]+)?";
  @Endpoint(url = TILES_REGEX, method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF tiles(ClientRequestIF request, ServletRequestIF servletRequest, @RequestParamter(name = "path") String path)
  {
    String url = servletRequest.getRequestURI();
    
    Pattern pattern = Pattern.compile("\\/cog\\/" + TILES_REGEX, Pattern.CASE_INSENSITIVE);
    
    Matcher matcher = pattern.matcher(url);
    
    if (matcher.find())
    {
      String matrixSetId = matcher.group(1);
      String z = matcher.group(2);
      String x = matcher.group(3);
      String y = matcher.group(4);
      String scale = matcher.group(5);
      String format = matcher.group(6);
      
      if (x == null)
      {
        throw new RuntimeException("Missing required parameter: x.");
      }
      if (y == null)
      {
        throw new RuntimeException("Missing required parameter: y.");
      }
      if (z == null)
      {
        throw new RuntimeException("Missing required parameter: z.");
      }
      
      InputStream isTile = this.service.tiles(request.getSessionId(), path, matrixSetId, x, y, z, scale, format);
      
      return new InputStreamResponse(isTile, format);
    }
    else
    {
      throw new ProgrammingErrorException("The provided url is invalid.");
    }
  }
}
