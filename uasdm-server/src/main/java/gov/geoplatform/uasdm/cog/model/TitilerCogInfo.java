/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.cog.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TitilerCogInfo
{
  private List<Double>                  bounds;

  private Integer                       minzoom;

  private Integer                       maxzoom;

  @JsonProperty("band_metadata")
  private List<TiTillerBandMetadata>    bandMetadata;

  @JsonProperty("band_descriptions")
  private List<TiTillerBandDescription> bandDescriptions;

  private String                        dtype;

  @JsonProperty("nodata_type")
  private String                        nodataType;

  private List<String>                  colorinterp;

  private String                        driver;

  private List<Float>                   scales;

  private List<Float>                   offsets;

  private Integer                       count;

  private Integer                       width;

  private Integer                       height;

  private List<Integer>                 overviews;

  private Object                        nodata_value;

  public List<Double> getBounds()
  {
    return bounds;
  }

  public void setBounds(List<Double> bounds)
  {
    this.bounds = bounds;
  }

  public Integer getMinzoom()
  {
    return minzoom;
  }

  public void setMinzoom(Integer minzoom)
  {
    this.minzoom = minzoom;
  }

  public Integer getMaxzoom()
  {
    return maxzoom;
  }

  public void setMaxzoom(Integer maxzoom)
  {
    this.maxzoom = maxzoom;
  }

  public List<TiTillerBandMetadata> getBandMetadata()
  {
    return bandMetadata;
  }

  public void setBandMetadata(List<TiTillerBandMetadata> bandMetadata)
  {
    this.bandMetadata = bandMetadata;
  }

  public List<TiTillerBandDescription> getBandDescriptions()
  {
    return bandDescriptions;
  }

  public void setBandDescriptions(List<TiTillerBandDescription> bandDescriptions)
  {
    this.bandDescriptions = bandDescriptions;
  }

  public String getDtype()
  {
    return dtype;
  }

  public void setDtype(String dtype)
  {
    this.dtype = dtype;
  }

  public String getNodataType()
  {
    return nodataType;
  }

  public void setNodataType(String nodataType)
  {
    this.nodataType = nodataType;
  }

  public List<String> getColorinterp()
  {
    return colorinterp;
  }

  public void setColorinterp(List<String> colorinterp)
  {
    this.colorinterp = colorinterp;
  }

  public String getDriver()
  {
    return driver;
  }

  public void setDriver(String driver)
  {
    this.driver = driver;
  }

  public List<Float> getScales()
  {
    return scales;
  }

  public void setScales(List<Float> scales)
  {
    this.scales = scales;
  }

  public List<Float> getOffsets()
  {
    return offsets;
  }

  public void setOffsets(List<Float> offsets)
  {
    this.offsets = offsets;
  }

  public Integer getCount()
  {
    return count;
  }

  public void setCount(Integer count)
  {
    this.count = count;
  }

  public Integer getWidth()
  {
    return width;
  }

  public void setWidth(Integer width)
  {
    this.width = width;
  }

  public Integer getHeight()
  {
    return height;
  }

  public void setHeight(Integer height)
  {
    this.height = height;
  }

  public List<Integer> getOverviews()
  {
    return overviews;
  }

  public void setOverviews(List<Integer> overviews)
  {
    this.overviews = overviews;
  }

  public Object getNodata_value()
  {
    return nodata_value;
  }

  public void setNodata_value(Object nodata_value)
  {
    this.nodata_value = nodata_value;
  }

}
