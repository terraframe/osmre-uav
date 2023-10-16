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
package gov.geoplatform.uasdm.cog;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.MultiValueMap;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.cog.model.TitilerCogBandStatistic;
import gov.geoplatform.uasdm.cog.model.TitilerCogInfo;
import gov.geoplatform.uasdm.cog.model.TitilerCogStatistics;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;

public class TiTillerProxy
{
  
  /**
   * -90 to 90 for latitude and -180 to 180 for longitude
   */
  public static class BBoxView
  {
    double minLat;
    double maxLat;
    double minLong;
    double maxLong;
    
    public BBoxView(double minLong, double minLat, double maxLong, double maxLat)
    {
      this.minLat = minLat;
      this.maxLat = maxLat;
      this.minLong = minLong;
      this.maxLong = maxLong;
    }
    
    public double getMinLat()
    {
      return minLat;
    }

    public void setMinLat(double minLat)
    {
      this.minLat = minLat;
    }

    public double getMaxLat()
    {
      return maxLat;
    }

    public void setMaxLat(double maxLat)
    {
      this.maxLat = maxLat;
    }

    public double getMinLong()
    {
      return minLong;
    }

    public void setMinLong(double minLong)
    {
      this.minLong = minLong;
    }

    public double getMaxLong()
    {
      return maxLong;
    }

    public void setMaxLong(double maxLong)
    {
      this.maxLong = maxLong;
    }

    /**
     * Perhaps Geoserver was returning bounding boxes of this format way back in the day? I dunno but
     * Mapbox expects [[long, lat], [long, lat]], and our front-end converts from this obscure format
     * to what Mapbox needs. Without patching a bunch of data though we're stuck with this internal format
     * on product.boundingBox.
     */
    public JSONArray toJSON()
    {
      JSONArray ja = new JSONArray();
      ja.put(this.minLong);
      ja.put(this.maxLong);
      ja.put(this.minLat);
      ja.put(this.maxLat);
      return ja;
    }
  }
  
  public TitilerCogStatistics getCogStatistics(DocumentIF document)
  {
    try
    {
      InputStream stream = null;
      
      String tifUrl = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
      
      Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
      parameters.put("url", Arrays.asList(tifUrl));
      
      stream = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/cog/statistics", parameters);
      
      return new TitilerCogStatistics(IOUtils.toString(stream, "UTF-8"));
    }
    catch(URISyntaxException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public TitilerCogInfo getCogInfo(DocumentIF document)
  {
    try
    {
      InputStream stream = null;
      
      String tifUrl = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
      
      Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
      parameters.put("url", Arrays.asList(tifUrl));
      
      stream = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/cog/info", parameters);
      
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(stream, TitilerCogInfo.class);
    }
    catch(URISyntaxException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public InputStream getCogPreview(ProductIF product, DocumentIF document, CogPreviewParams params)
  {
    try
    {
      InputStream stream = null;
      
      String tifUrl;
      
      if (product.isPublished())
      {
        tifUrl = "s3://" + AppProperties.getPublicBucketName() + "/" + document.getS3location();
      }
      else
      {
        tifUrl = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
      }
      
      Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
      parameters.put("url", Arrays.asList(tifUrl));
      
      params.addParameters(parameters);
      
      addMultispectralRGBParams(document, parameters);
      
      stream = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/cog/preview", parameters);
      
      return stream;
    }
    catch(URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public BBoxView getBoundingBox(ProductIF product, DocumentIF document)
  {
    try
    {
      InputStream bboxStream = null;
      
      String tifUrl;
      
      if (product.isPublished())
      {
        tifUrl = "s3://" + AppProperties.getPublicBucketName() + "/" + document.getS3location();
      }
      else
      {
        tifUrl = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
      }
      
      Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
      parameters.put("url", Arrays.asList(tifUrl));
      
      bboxStream = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/cog/bounds", parameters);
      
      String sBbox = IOUtils.toString(bboxStream, StandardCharsets.UTF_8.name());
      
      // {"bounds":[-111.12441929215683,39.32065294027042,-111.12343879151011,39.32106566894064]}
      JSONArray jaBbox = new JSONObject(sBbox).getJSONArray("bounds");
      
      return new BBoxView(jaBbox.getDouble(0), jaBbox.getDouble(1), jaBbox.getDouble(2), jaBbox.getDouble(3));
    }
    catch(IOException | URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public InputStream tiles(DocumentIF document, String matrixSetId, String x, String y, String z, String scale, String format, MultiValueMap<String, String> queryParams)
  {
    final String layerS3Uri = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
    
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    parameters.put("url", Arrays.asList(layerS3Uri));
    
    passThroughParams(queryParams, parameters, new String[] {"bidx", "rescale", "resampling", "color_formula", "colormap_name", "colormap", "return_mask"});
    
    try
    {
      InputStream isTile = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/cog/tiles/" + matrixSetId + "/" + z + "/" + x + "/" + y + "@" + scale + "x", parameters);
      
      return isTile;
    }
    catch (URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  protected void passThroughParams(MultiValueMap<String, String> queryParams, Map<String, List<String>> parameters, String[] passThroughParams)
  {
    for (Entry<String, List<String>> entry : queryParams.entrySet())
    {
      if (ArrayUtils.contains(passThroughParams, entry.getKey()))
      {
        List<String> encoded = entry.getValue();
        
        List<String> decoded = new ArrayList<String>();
        for (String val : encoded)
        {
          try
          {
            decoded.add(URLDecoder.decode(val, "UTF-8"));
          }
          catch (UnsupportedEncodingException e)
          {
            throw new ProgrammingErrorException(e);
          }
        }
        
        parameters.put(entry.getKey(), decoded);
      }
    }
  }
  
  public JSONObject tilejson(DocumentIF document, String contextPath)
  {
    UasComponentIF component = document.getComponent();
    if (component instanceof CollectionIF)
    {
      CollectionReportFacade.updateDownloadCount((CollectionIF) component).doIt();
    }
    
    final String layerS3Uri = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
    
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    parameters.put("url", Arrays.asList(layerS3Uri));
    
    // These are the min and max zooms which Mapbox will allow
    parameters.put("minzoom", Arrays.asList("0"));
    parameters.put("maxzoom", Arrays.asList("24"));
    
    addMultispectralRGBParams(document, parameters);
    
    try
    {
      // We have to get the tilejson file from titiler and replace their urls with our urls, since it can only be accessed through us by proxy.
      String sTileJson = IOUtils.toString(authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/cog/tilejson.json", parameters), StandardCharsets.UTF_8.name());
      
      JSONObject joTileJson = new JSONObject(sTileJson);
      
      JSONArray jaTiles = joTileJson.getJSONArray("tiles");
      for (int i = 0; i < jaTiles.length(); ++i)
      {
        String sTile = jaTiles.getString(i);
        
        String replacedPath = sTile.replace(AppProperties.getTitilerUrl(), contextPath);
        
        String pathEncoded = URLEncoder.encode(document.getS3location(), StandardCharsets.UTF_8.name());
        
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

  protected void addMultispectralRGBParams(DocumentIF document, Map<String, List<String>> parameters)
  {
    if (((gov.geoplatform.uasdm.graph.Collection)document.getComponent()).isMultiSpectral()
        && document.getS3location().matches(Product.MAPPABLE_ORTHO_REGEX))
    {
      TitilerCogInfo info = this.getCogInfo(document);
      if (info != null)
      {
        int redIdx = info.getColorinterp().indexOf("red");
        int greenIdx = info.getColorinterp().indexOf("green");
        int blueIdx = info.getColorinterp().indexOf("blue");
        
        if (redIdx != -1 && greenIdx != -1 && blueIdx != -1)
        {
          redIdx++;
          greenIdx++;
          blueIdx++;
          
          parameters.put("bidx", Arrays.asList(new String[] {String.valueOf(redIdx),String.valueOf(greenIdx),String.valueOf(blueIdx)}));
          
          TitilerCogStatistics stats = this.getCogStatistics(document);
          TitilerCogBandStatistic redStat = stats.getBandStatistic(redIdx);
          TitilerCogBandStatistic greenStat = stats.getBandStatistic(greenIdx);
          TitilerCogBandStatistic blueStat = stats.getBandStatistic(blueIdx);
          
          Double min = Math.min(redStat.getMin(), Math.min(greenStat.getMin(), blueStat.getMin()));
          Double max = Math.max(redStat.getMax(), Math.max(greenStat.getMax(), blueStat.getMax()));

//          min = (min < 0) ? 0 : min; // TODO : No idea how the min value could be negative. But it's happening on my sample data and it doesn't render properly if it is.
          
          parameters.put("rescale", Arrays.asList(String.valueOf(min) + "," + String.valueOf(max)));
        }
      }
    }
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
    if (endpoint.toString().contains("titiler.xyz"))
    {
      // Titiler testing endpoint. Does not require authentication.
      
      try
      {
        String url = endpoint.toString() + resourcePath;
        
        url += "?";
        
        List<String> encodedParams = new ArrayList<>();
        
        for (Entry<String, List<String>> entry : parameters.entrySet())
        {
          for (String value : entry.getValue())
          {
            if (entry.getKey().equals("url"))
            {
              encodedParams.add(entry.getKey() + "=" + URLEncoder.encode(value.replace("osmre-uas-dev", "osmre-uas-dev-public"), "UTF-8"));
            }
            else
            {
              encodedParams.add(entry.getKey() + "=" + URLEncoder.encode(value, "UTF-8"));
            }
          }
        }
        
        url += StringUtils.join(encodedParams, "&");
        
        return new URL(url).openStream();
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
    else
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
                  String msg = response.getStatusText();
                  
                  if (response.getContent() != null)
                  {
                    msg = msg + ": " + IOUtils.toString(response.getContent(), "UTF-8");
                  }
                
                  AmazonServiceException ase = new AmazonServiceException(msg);
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
  

}
