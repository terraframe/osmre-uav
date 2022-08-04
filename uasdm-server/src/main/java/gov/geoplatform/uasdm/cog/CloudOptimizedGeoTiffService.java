package gov.geoplatform.uasdm.cog;

import org.json.JSONObject;

import com.runwaysdk.mvc.ErrorRestResponse;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.web.json.JSONRunwayExceptionDTO;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;

public class CloudOptimizedGeoTiffService
{
  private TiTillerProxy proxy = new TiTillerProxy();
  
  @Request(RequestType.SESSION)
  public ResponseIF tiles(String sessionId, String path, String matrixSetId, String x, String y, String z, String scale, String format)
  {
    try
    {
      DocumentIF document = Document.find(path);
      
      return new InputStreamResponse(proxy.tiles(document, matrixSetId, x, y, z, scale, format), "image/" + format);
    }
    catch (Throwable t)
    {
      CogTileException cte = (t instanceof CogTileException) ? (CogTileException) t : new CogTileException(t);
      
      // We don't want this exception to be thrown into Runway's request handling aspects because they will log the exception.
      // And since this service throws so dang many errors I'm worried it will flood the logs.
      return new ErrorRestResponse(new JSONRunwayExceptionDTO(cte).getJSON());
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String path)
  {
    DocumentIF document = Document.find(path);
    
    return proxy.tilejson(document, contextPath);
  }
}
