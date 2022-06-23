package gov.geoplatform.uasdm.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.controller.CloudOptimizedGeoTiffController;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.model.DocumentIF;

public class CloudOptimizedGeoTiffService
{
  @Request(RequestType.SESSION)
  public InputStream tiles(String sessionId, String path, String matrixSetId, String x, String y, String z, String scale, String format)
  {
    DocumentIF document = this.getDocumentFromPath(path);
    
    String sUrl = AppProperties.getTitilerPrivateUrl();
    
    final String layerS3Uri = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
    
    sUrl = sUrl + "/cog/tiles/" + matrixSetId + "/" + z + "/" + x + "/" + y + "@" + scale + "x" + "?url=" + layerS3Uri;
    
    try
    {
      URL url = new URL(sUrl);
      
      InputStream isTile = new BufferedInputStream(url.openStream());
      
      return isTile;
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject tilejson(String sessionId, String contextPath, String path)
  {
    DocumentIF document = this.getDocumentFromPath(path);
    
    final String layerS3Uri = "s3://" + AppProperties.getBucketName() + "/" + document.getS3location();
    
    String sUrl = AppProperties.getTitilerPrivateUrl();
    
    sUrl = sUrl + "/cog/tilejson.json?url=" + layerS3Uri;
    
    try
    {
      // We have to get the tilejson file from titiler and replace their urls with our urls, since it can only be accessed through us by proxy.
      URL url = new URL(sUrl);
      
      String sTileJson = IOUtils.toString(new BufferedInputStream(url.openStream()), "UTF-8");
      
      JSONObject joTileJson = new JSONObject(sTileJson);
      
      JSONArray jaTiles = joTileJson.getJSONArray("tiles");
      for (int i = 0; i < jaTiles.length(); ++i)
      {
        String sTile = jaTiles.getString(i);
        
        Pattern pattern = Pattern.compile("\\/cog\\/" + CloudOptimizedGeoTiffController.TILES_REGEX);
        Matcher matcher = pattern.matcher(sTile);
        
        if (matcher.matches())
        {
          String matrixSetId = matcher.group(1);
          String z = matcher.group(2);
          String x = matcher.group(3);
          String y = matcher.group(4);
          String scale = matcher.group(5);
          String format = matcher.group(6);
          
          String pathEncoded = URLEncoder.encode(path, "UTF-8");
          String encodedUrl = URLEncoder.encode(layerS3Uri, "UTF-8");
          jaTiles.put(i, contextPath + "/cog/tiles/" + matrixSetId + "/" + z + "/" + x + "/" + y + "@" + scale + "x" + format + "?path=" + pathEncoded + "scale=" + scale + "&url=" + encodedUrl);
        }
      }
      
      return joTileJson;
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  private DocumentIF getDocumentFromPath(String path)
  {
    return Document.find(path);
  }
}
