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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.cog.model.TiTilerStacAssetInfo;
import gov.geoplatform.uasdm.cog.model.TiTilerStacBandStatistic;
import gov.geoplatform.uasdm.cog.model.TiTilerStacInfo;
import gov.geoplatform.uasdm.cog.model.TiTilerStacStatistics;
import gov.geoplatform.uasdm.cog.model.TiTillerBandMetadata;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;

public class StacTiTillerProxy extends TiTillerProxy
{
  private static final String URL    = "url";

  private static final String ASSETS = "assets";

  private String              url;

  private String              assets;

  private Boolean             multispectral;

  private Boolean             hillshade;

  public StacTiTillerProxy(String url, String assets)
  {
    this(url, assets, false, false);
  }

  public StacTiTillerProxy(String url, String assets, Boolean multispectral, Boolean hillshade)
  {
    this.url = url;
    this.assets = assets;
    this.multispectral = multispectral;
    this.hillshade = hillshade;
  }

  public InputStream tiles(String matrixSetId, Integer z, Integer x, Integer y, Integer scale, String format, MultiValueMap<String, String> queryParams)
  {
    Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
    parameters.put("url", Arrays.asList(this.url));
    parameters.put("assets", Arrays.asList(this.assets));

    passThroughParams(queryParams, parameters, new String[] { "algorithm", "buffer", "asset_bidx", "rescale", "resampling", "color_formula", "colormap_name", "colormap", "return_mask" });

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

    S3Utilities s3Utilities = S3Utilities.builder() //
        .region(Region.of(AppProperties.getBucketRegion())) //
        .build();
    S3Uri s3Uri = s3Utilities.parseUri(URI.create(this.url));

    // Update the download count
    // We know that the filename of the stac json is the product id. So parse
    // out that id, fetch the product, and increment the count
    String key = s3Uri.key().orElseThrow(() -> {
      GenericException ex = new GenericException();
      ex.setUserMessage("Unabled to parse key from url: " + this.url);

      return ex;
    });

    if (this.multispectral)
    {
      this.calculateAndRescaleMultiBands(parameters);
    }
    else if (this.hillshade)
    {
      parameters.put("algorithm", Arrays.asList("hillshade"));
      parameters.put("colormap_name", Arrays.asList("terrain"));
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

  protected void calculateAndRescaleSingleBand(Map<String, List<String>> parameters)
  {
    this.getStacInfo().ifPresent(info -> {
      TiTilerStacAssetInfo asset = info.getAsset(this.assets);

      final TiTillerBandMetadata metadata = asset.getBandMetadata().get(0);

      this.getStacStatistics().ifPresent(stats -> {

        // parameters.put("asset_bidx", Arrays.asList(this.assets + "|" +
        // String.valueOf(1)));

        List<TiTilerStacBandStatistic> bands = Arrays.asList( //
            stats.getAssetBand(this.assets + "_" + metadata.getName()) //
        );

        Optional<Double> max = bands.stream().filter(b -> b != null).map(b -> b.getMax()).reduce((a, b) -> Math.max(a, b));
        Optional<Double> min = bands.stream().filter(b -> b != null).map(b -> b.getMin()).reduce((a, b) -> Math.min(a, b));

        if (min.isPresent() && max.isPresent())
        {
          parameters.put("rescale", Arrays.asList(String.valueOf(min.get()) + "," + String.valueOf(max.get())));
        }
      });
    });
  }

  protected void calculateAndRescaleMultiBands(Map<String, List<String>> parameters)
  {
    this.getStacInfo().ifPresent(info -> {
      TiTilerStacAssetInfo asset = info.getAsset(this.assets);

      AtomicInteger redIdx = new AtomicInteger(asset.getColorinterp().indexOf("red"));
      AtomicInteger greenIdx = new AtomicInteger(asset.getColorinterp().indexOf("green"));
      AtomicInteger blueIdx = new AtomicInteger(asset.getColorinterp().indexOf("blue"));

      if (redIdx.intValue() != -1 && greenIdx.intValue() != -1 && blueIdx.intValue() != -1)
      {
        final TiTillerBandMetadata redMetadata = asset.getBandMetadata().get(redIdx.get());
        final TiTillerBandMetadata greenMetadata = asset.getBandMetadata().get(greenIdx.get());
        final TiTillerBandMetadata blueMetadata = asset.getBandMetadata().get(blueIdx.get());

        this.getStacStatistics().ifPresent(stats -> {

          parameters.put("asset_bidx", Arrays.asList(this.assets + "|" + String.valueOf(redIdx.intValue() + 1) + "," + String.valueOf(greenIdx.intValue() + 1) + "," + String.valueOf(blueIdx.intValue() + 1)));

          List<TiTilerStacBandStatistic> bands = Arrays.asList( //
              stats.getAssetBand(this.assets + "_" + redMetadata.getName()), //
              stats.getAssetBand(this.assets + "_" + greenMetadata.getName()), //
              stats.getAssetBand(this.assets + "_" + blueMetadata.getName()));

          Optional<Double> max = bands.stream().filter(b -> b != null).map(b -> b.getMax()).reduce((a, b) -> Math.max(a, b));
          Optional<Double> min = bands.stream().filter(b -> b != null).map(b -> b.getMin()).reduce((a, b) -> Math.min(a, b));

          if (min.isPresent() && max.isPresent())
          {
            parameters.put("rescale", Arrays.asList(String.valueOf(min.get()) + "," + String.valueOf(max.get())));
          }
        });
      }
    });
  }

  public Optional<TiTilerStacInfo> getStacInfo()
  {
    try
    {
      Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
      parameters.put(URL, Arrays.asList(url));
      parameters.put(ASSETS, Arrays.asList(this.assets));

      InputStream stream = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/stac/info", parameters);

      ObjectMapper mapper = new ObjectMapper();

      return Optional.ofNullable(mapper.readValue(stream, TiTilerStacInfo.class));
    }
    catch (URISyntaxException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public Optional<TiTilerStacStatistics> getStacStatistics()
  {
    try
    {
      Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
      parameters.put(URL, Arrays.asList(url));
      parameters.put(ASSETS, Arrays.asList(this.assets));

      try (InputStream stream = authenticatedInvokeURL(new URI(AppProperties.getTitilerUrl()), "/stac/statistics", parameters))
      {
        ObjectMapper mapper = new ObjectMapper();

        return Optional.ofNullable(mapper.readValue(stream, TiTilerStacStatistics.class));
      }
    }
    catch (URISyntaxException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

  }
}
