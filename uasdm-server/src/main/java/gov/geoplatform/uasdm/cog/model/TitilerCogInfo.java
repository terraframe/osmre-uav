package gov.geoplatform.uasdm.cog.model;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TitilerCogInfo
{
  private List<Float> bounds;
  private Integer minzoom;
  private Integer maxzoom;
  private ArrayNode band_metadata;
  private ArrayNode band_descriptions;
  private String dtype;
  private String nodata_type;
  private List<String> colorinterp;
  private String driver;
  private Integer width;
  private Integer height;
  private List<Integer> overviews;
  private Integer count;
  
  public TitilerCogInfo()
  {
    
  }

  public List<Float> getBounds()
  {
    return bounds;
  }

  public void setBounds(List<Float> bounds)
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

  public ArrayNode getBand_metadata()
  {
    return band_metadata;
  }

  public void setBand_metadata(ArrayNode band_metadata)
  {
    this.band_metadata = band_metadata;
  }

  public ArrayNode getBand_descriptions()
  {
    return band_descriptions;
  }

  public void setBand_descriptions(ArrayNode band_descriptions)
  {
    this.band_descriptions = band_descriptions;
  }

  public String getDtype()
  {
    return dtype;
  }

  public void setDtype(String dtype)
  {
    this.dtype = dtype;
  }

  public String getNodata_type()
  {
    return nodata_type;
  }

  public void setNodata_type(String nodata_type)
  {
    this.nodata_type = nodata_type;
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

  public Integer getCount()
  {
    return count;
  }

  public void setCount(Integer count)
  {
    this.count = count;
  }
}
