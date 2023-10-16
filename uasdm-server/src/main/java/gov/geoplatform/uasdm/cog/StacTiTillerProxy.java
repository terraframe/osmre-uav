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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.MultiValueMap;

import com.amazonaws.services.s3.AmazonS3URI;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.cog.model.TitilerCogBandStatistic;
import gov.geoplatform.uasdm.cog.model.TitilerCogInfo;
import gov.geoplatform.uasdm.cog.model.TitilerCogStatistics;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.s3.S3RemoteFileService;

public class StacTiTillerProxy extends TiTillerProxy
{

  private String url;

  private String assets;

  public StacTiTillerProxy(String url, String assets)
  {
    this.url = url;
    this.assets = assets;
  }

  public InputStream tiles(String matrixSetId, Integer z, Integer x, Integer y, Integer scale, String format, MultiValueMap<String, String> queryParams)
  {
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    parameters.put("url", Arrays.asList(this.url));
    parameters.put("assets", Arrays.asList(this.assets));
    
    passThroughParams(queryParams, parameters, new String[] {"asset_bidx", "rescale", "resampling", "color_formula", "colormap_name", "colormap", "return_mask"});

    try
    {
      URI endpoint = new URI(AppProperties.getTitilerUrl());
      String resourcePath = "/stac/tiles/" + matrixSetId + "/" + z + "/" + x + "/" + y + "@" + scale + "x";

      return authenticatedInvokeURL(endpoint, resourcePath, parameters);
    }
    catch (URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public JSONObject tilejson(String contextPath)
  {
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    
    // Update the download count
    // We know that the filename of the stac json is the product id. So parse out that id, fetch the product, and increment the count
    String key = new AmazonS3URI(this.url).getKey();
    String productId = key.substring(S3RemoteFileService.STAC_BUCKET.length() + 1, key.length() - 5);
    Product product = Product.get(productId);
    if (product != null)
    {
      UasComponent component = product.getComponent();
      if (component instanceof Collection)
      {
        CollectionReportFacade.updateDownloadCount((CollectionIF) component).doIt();
        
        addMultispectralRGBParamsForStac(product, (Collection) component, parameters);
      }
    }
    
    parameters.put("url", Arrays.asList(this.url));
    parameters.put("assets", Arrays.asList(this.assets));
    
    // These are the min and max zooms which Mapbox will allow
    parameters.put("minzoom", Arrays.asList("0"));
    parameters.put("maxzoom", Arrays.asList("24"));
    
    try
    {
      // We have to get the tilejson file from titiler and replace their urls
      // with our urls, since it can only be accessed through us by proxy.
      String sTileJson = IOUtils.toString(authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/stac/tilejson.json", parameters), StandardCharsets.UTF_8.name());

      JSONObject joTileJson = new JSONObject(sTileJson);

      JSONArray jaTiles = joTileJson.getJSONArray("tiles");
      for (int i = 0; i < jaTiles.length(); ++i)
      {
        String sTile = jaTiles.getString(i);

        String replacedPath = sTile.replace(AppProperties.getTitilerUrl(), contextPath);

        jaTiles.put(i, replacedPath);
      }

      return joTileJson;
    }
    catch (IOException | URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  protected void addMultispectralRGBParamsForStac(Product product, Collection collection, Map<String, List<String>> parameters)
  {
    if ((collection).isMultiSpectral()
        && assets.length() > 0 && assets.contains("odm_orthophoto"))
    {
      DocumentIF document = product.getMappableOrtho().get();
      
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
          
          parameters.put("asset_bidx", Arrays.asList(assets + "|" + String.valueOf(redIdx) + "," + String.valueOf(greenIdx) + "," + String.valueOf(blueIdx)));
          
          TitilerCogStatistics stats = this.getCogStatistics(document);
          TitilerCogBandStatistic redStat = stats.getBandStatistic(redIdx);
          TitilerCogBandStatistic greenStat = stats.getBandStatistic(greenIdx);
          TitilerCogBandStatistic blueStat = stats.getBandStatistic(blueIdx);
          
          Double min = Math.min(redStat.getMin(), Math.min(greenStat.getMin(), blueStat.getMin()));
          Double max = Math.max(redStat.getMax(), Math.max(greenStat.getMax(), blueStat.getMax()));

          // min = (min < 0) ? 0 : min; // TODO : No idea how the min value could be negative. But it's happening on my sample data and it doesn't render properly if it is.
          
          parameters.put("rescale", Arrays.asList(String.valueOf(min) + "," + String.valueOf(max)));
        }
      }
    }
  }

}
