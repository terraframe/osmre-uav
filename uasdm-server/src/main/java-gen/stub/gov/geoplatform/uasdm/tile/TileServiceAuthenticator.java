package gov.geoplatform.uasdm.tile;

import org.springframework.context.ApplicationContext;
import org.springframework.util.MultiValueMap;

import com.runwaysdk.business.rbac.Authenticate;

import gov.geoplatform.uasdm.service.business.CloudOptimizedGeoTiffBusinessService;

public class TileServiceAuthenticator extends TileServiceAuthenticatorBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -24070010;
  
  protected ApplicationContext applicationContext;
  
  protected TileServiceAuthenticatedRequestIF tileRequest;
  
  protected Object response;
  
  public TileServiceAuthenticator(ApplicationContext applicationContext, TileServiceAuthenticatedRequestIF tileRequest)
  {
    super();
    this.applicationContext = applicationContext;
    this.tileRequest = tileRequest;
  }
  
  @Authenticate
  @Override
  public void authenticate()
  {
    if ("tiles".equals(tileRequest.method()))
    {
      TilesRequest request = (TilesRequest) tileRequest;
      response = this.applicationContext.getBean(CloudOptimizedGeoTiffBusinessService.class).tiles(request.sessionId, request.path, request.matrixSetId, request.x, request.y, request.z, request.scale, request.format, request.queryParams);
    }
    else if ("tilejson".equals(tileRequest.method()))
    {
      TileJsonRequest request = (TileJsonRequest) tileRequest;
      response = this.applicationContext.getBean(CloudOptimizedGeoTiffBusinessService.class).tilejson(request.sessionId, request.contextPath, request.path);
    }
  }
  
  public Object GetResponse()
  {
    return response;
  }
  
  public interface TileServiceAuthenticatedRequestIF
  {
    public String method();
  }
  
  public static class TilesRequest implements TileServiceAuthenticatedRequestIF
  {
    public String sessionId;
    public String path;
    public String matrixSetId;
    public String x;
    public String y;
    public String z;
    public String scale;
    public String format;
    public MultiValueMap<String, String> queryParams;
    
    public String method() { return "tiles"; }
  }
  
  public static class TileJsonRequest implements TileServiceAuthenticatedRequestIF
  {
    public String sessionId;
    public String contextPath;
    public String path;
    
    public String method() { return "tilejson"; }
  }
}
