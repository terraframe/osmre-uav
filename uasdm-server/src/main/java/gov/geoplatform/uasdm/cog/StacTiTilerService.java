package gov.geoplatform.uasdm.cog;

import org.json.JSONObject;

import com.runwaysdk.mvc.ErrorRestResponse;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.web.json.JSONRunwayExceptionDTO;

public class StacTiTilerService
{
  @Request(RequestType.SESSION)
  public ResponseIF tiles(String sessionId, String matrixSetId, Integer x, Integer y, Integer z, Integer scale, String format, String url, String assets)
  {
    try
    {
      StacTiTillerProxy proxy = new StacTiTillerProxy(url, assets);

      return new InputStreamResponse(proxy.tiles(matrixSetId, z, x, y, scale, format), "image/" + format);
    }
    catch (Throwable t)
    {
      CogTileException cte = ( t instanceof CogTileException ) ? (CogTileException) t : new CogTileException(t);

      // We don't want this exception to be thrown into Runway's request
      // handling aspects because they will log the exception.
      // And since this service throws so dang many errors I'm worried it will
      // flood the logs.
      return new ErrorRestResponse(new JSONRunwayExceptionDTO(cte).getJSON());
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String url, String assets)
  {
    StacTiTillerProxy proxy = new StacTiTillerProxy(url, assets);

    return proxy.tilejson(contextPath);
  }
}
