package gov.geoplatform.uasdm.cog;

import java.io.InputStream;

import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;

public class CloudOptimizedGeoTiffService
{
  @Request(RequestType.SESSION)
  public InputStream tiles(String sessionId, String path, String matrixSetId, String x, String y, String z, String scale, String format)
  {
    DocumentIF document = Document.find(path);
    
    Product product = ((Document) document).getProductHasDocumentParentProducts().get(0);
    
    return new CloudOptimizedGeoTiff(product, document).tiles(matrixSetId, x, y, z, scale, format);
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String path)
  {
    DocumentIF document = Document.find(path);
    
    Product product = ((Document) document).getProductHasDocumentParentProducts().get(0);
    
    return new CloudOptimizedGeoTiff(product, document).tilejson(contextPath);
  }
}
