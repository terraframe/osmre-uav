package gov.geoplatform.uasdm.cog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Response;
import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;

public class CloudOptimizedGeoTiff
{
  
  public static class BBoxView
  {
    String minx;
    String maxx;
    String miny;
    String maxy;
    
    public BBoxView(String minx, String maxx, String miny, String maxy)
    {
      this.minx = minx;
      this.maxx = maxx;
      this.miny = miny;
      this.maxy = maxy;
    }
    
    public JSONArray toJSON()
    {
      JSONArray ja = new JSONArray();
      ja.put(this.minx);
      ja.put(this.maxx);
      ja.put(this.miny);
      ja.put(this.maxy);
      return ja;
    }
  }
  
  private ProductIF product;
  
  private DocumentIF document;
  
  public CloudOptimizedGeoTiff(ProductIF product, DocumentIF document)
  {
    this.product = product;
    this.document = document;
  }
  
  public BBoxView getBoundingBox()
  {
    try
    {
      InputStream bboxStream = null;
      
      if (this.product.isPublished())
      {
        String url = AppProperties.getTitilerPublicUrl() + "/cog/bounds?url=" + "s3://" + AppProperties.getPublicBucketName() + "/" + this.document.getS3location();
        
        bboxStream = new URL(url).openStream();
      }
      else
      {
        Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
        parameters.put("url", Arrays.asList("s3://" + AppProperties.getBucketName() + "/" + this.document.getS3location()));
        
        bboxStream = authenticatedInvokeURL(new URI(AppProperties.getTitilerPrivateUrl()), "/cog/bounds", parameters);
      }
      
      JSONArray jaBbox = new JSONArray(IOUtils.toString(bboxStream, "UTF-8"));
      
      return new BBoxView(jaBbox.getString(0), jaBbox.getString(1), jaBbox.getString(2), jaBbox.getString(3));
    }
    catch(IOException | URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public InputStream tiles(String matrixSetId, String x, String y, String z, String scale, String format)
  {
    final String layerS3Uri = "s3://" + AppProperties.getBucketName() + "/" + this.document.getS3location();
    
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    parameters.put("url", Arrays.asList(layerS3Uri));
    
    try
    {
      InputStream isTile = authenticatedInvokeURL(new URI(AppProperties.getTitilerPrivateUrl()), "/cog/tiles/" + matrixSetId + "/" + z + "/" + x + "/" + y + "@" + scale + "x", parameters);
      
      return isTile;
    }
    catch (URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public JSONObject tilejson(String contextPath)
  {
    final String layerS3Uri = "s3://" + AppProperties.getBucketName() + "/" + this.document.getS3location();
    
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    parameters.put("url", Arrays.asList(layerS3Uri));
    
    try
    {
      // We have to get the tilejson file from titiler and replace their urls with our urls, since it can only be accessed through us by proxy.
      String sTileJson = IOUtils.toString(authenticatedInvokeURL(new URI(AppProperties.getTitilerPrivateUrl()), "/cog/tilejson.json", parameters), "UTF-8");
      
      JSONObject joTileJson = new JSONObject(sTileJson);
      
      JSONArray jaTiles = joTileJson.getJSONArray("tiles");
      for (int i = 0; i < jaTiles.length(); ++i)
      {
        String sTile = jaTiles.getString(i);
        
        String replacedPath = sTile.replace(AppProperties.getTitilerPrivateUrl(), contextPath);
        
        String pathEncoded = URLEncoder.encode(this.document.getS3location(), "UTF-8");
        
        if (replacedPath.contains("?"))
        {
          replacedPath = replacedPath + "&path=" + pathEncoded;
        }
        else
        {
          replacedPath = replacedPath + "?path=" + pathEncoded;
        }
        
        jaTiles.put(i, replacedPath);
      }
      
      return joTileJson;
    }
    catch (IOException | URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public ProductIF getProduct()
  {
    return product;
  }

  public void setProduct(ProductIF product)
  {
    this.product = product;
  }

  public DocumentIF getDocument()
  {
    return document;
  }

  public void setDocument(DocumentIF document)
  {
    this.document = document;
  }
  
  /**
   * Invokes a HTTPS endpoint hosted by AWS API Gateway with authentication provided by AWS IAM access key credentials.
   * 
   * Special thanks to:
   * - Piotr Filipowicz @ https://inspeerity.com/blog/how-to-call-aws-api-gateway-from-the-java-code
   * - amihaiemil @ https://stackoverflow.com/questions/35985931/how-to-generate-signature-in-aws-from-java
   * 
   * @param uri
   * @return String The body of the response
   * @throws AmazonServiceException If something goes wrong while invoking the remote endpoint.
   */
  public InputStream authenticatedInvokeURL(URI endpoint, String resourcePath, Map<String, List<String>> parameters)
  {
    final BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    
    // Instantiate the request
    com.amazonaws.Request<Void> request = new DefaultRequest<Void>("execute-api");
    request.setHttpMethod(HttpMethodName.GET);
    request.setEndpoint(endpoint);
    request.setResourcePath(resourcePath);
    request.setParameters(parameters);

    // Sign it...
    AWS4Signer signer = new AWS4Signer();
    signer.setRegionName(AppProperties.getBucketRegion());
    signer.setServiceName(request.getServiceName());
    signer.sign(request, awsCreds);

    // Execute it and get the response...
    Response<InputStream> rsp = new AmazonHttpClient(new ClientConfiguration())
        .requestExecutionBuilder()
        .executionContext(new ExecutionContext(true))
        .request(request)
        .errorResponseHandler(new HttpResponseHandler<SdkBaseException>() {
            @Override
            public SdkBaseException handle(HttpResponse response) throws Exception {
                AmazonServiceException ase = new AmazonServiceException(response.getStatusText());
                ase.setStatusCode(response.getStatusCode());
                return ase;
            }
            @Override
            public boolean needsConnectionLeftOpen() {
                return false;
            }
        })
        .execute(new HttpResponseHandler<InputStream>() {
          @Override
          public InputStream handle(HttpResponse response) throws Exception {
              return response.getContent();
          }
          @Override
          public boolean needsConnectionLeftOpen() {
              return true;
          }
        });
    
    return rsp.getHttpResponse().getContent();
  }
  
}
