package gov.geoplatform.uasdm.cog.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TiTilerStacAssetInfo
{
  private List<Double>                      bounds;

  private Integer                           minzoom;

  private Integer                           maxzoom;

  @JsonProperty("band_metadata")
  private List<TiTillerStacBandMetadata>    bandMetadata;

  @JsonProperty("band_descriptions")
  private List<TiTillerStacBandDescription> bandDescriptions;

  private String                            dtype;

  @JsonProperty("nodata_type")
  private String                            nodataType;

  private List<String>                      colorinterp;

  private String                            driver;

  private List<Float>                       scales;

  private List<Float>                       offsets;

  private Integer                           count;

  private Integer                           width;

  private Integer                           height;

  private List<Integer>                     overviews;

  public TiTilerStacAssetInfo()
  {

  }

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

  public List<TiTillerStacBandMetadata> getBandMetadata()
  {
    return bandMetadata;
  }

  public void setBandMetadata(List<TiTillerStacBandMetadata> bandMetadata)
  {
    this.bandMetadata = bandMetadata;
  }

  public List<TiTillerStacBandDescription> getBandDescriptions()
  {
    return bandDescriptions;
  }

  public void setBandDescriptions(List<TiTillerStacBandDescription> bandDescriptions)
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

}
