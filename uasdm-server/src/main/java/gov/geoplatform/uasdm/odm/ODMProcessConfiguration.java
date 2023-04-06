package gov.geoplatform.uasdm.odm;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.view.RequestParserIF;

public class ODMProcessConfiguration
{
  public static enum Quality {
    ULTRA("ultra"), HIGH("high"), MEDIUM("medium"), LOW("low"), LOWEST("lowest");

    private String code;

    private Quality(String code)
    {
      this.code = code;
    }

    public String getCode()
    {
      return code;
    }
  }

  private boolean    includeGeoLocationFile;

  private String     outFileNamePrefix;

  private BigDecimal resolution;

  /*
   * video-resolution <positive integer>
   * 
   * The maximum output resolution of extracted video frames in pixels. Default:
   * 4000
   */
  private Integer    videoResolution;

  /*
   * matcher-neighbors <positive integer>
   * 
   * Perform image matching with the nearest images based on GPS exif data. Set
   * to 0 to match by triangulation. Default: 0
   */
  private Integer    matcherNeighbors;

  /*
   * min-num-features <integer>
   * 
   * Minimum number of features to extract per image. More features can be
   * useful for finding more matches between images, potentially allowing the
   * reconstruction of areas with little overlap or insufficient features. More
   * features also slow down processing. Default: 10000
   */
  private Integer    minNumFeatures;

  /*
   * pc-quality ultra | high | medium | low | lowest
   * 
   * Set point cloud quality. Higher quality generates better, denser point
   * clouds, but requires more memory and takes longer. Each step up in quality
   * increases processing time roughly by a factor of 4x.. Default: medium
   */
  private Quality    pcQuality;

  public ODMProcessConfiguration()
  {
    this("");
  }

  public ODMProcessConfiguration(String outFileNamePrefix)
  {
    this.includeGeoLocationFile = false;
    this.outFileNamePrefix = outFileNamePrefix;
    this.resolution = new BigDecimal(5);
    this.matcherNeighbors = Integer.valueOf(0);
    this.minNumFeatures = Integer.valueOf(10000);
    this.videoResolution = Integer.valueOf(4000);
    this.pcQuality = Quality.MEDIUM;
  }

  public boolean isIncludeGeoLocationFile()
  {
    return includeGeoLocationFile;
  }

  public void setIncludeGeoLocationFile(boolean includeGeoLocationFile)
  {
    this.includeGeoLocationFile = includeGeoLocationFile;
  }

  public String getOutFileNamePrefix()
  {
    return outFileNamePrefix;
  }

  public void setOutFileNamePrefix(String outFileNamePrefix)
  {
    this.outFileNamePrefix = outFileNamePrefix;
  }

  public BigDecimal getResolution()
  {
    return resolution;
  }

  public void setResolution(BigDecimal resolution)
  {
    this.resolution = resolution;
  }

  public Integer getVideoResolution()
  {
    return videoResolution;
  }

  public void setVideoResolution(Integer videoResolution)
  {
    this.videoResolution = videoResolution;
  }

  public Integer getMatcherNeighbors()
  {
    return matcherNeighbors;
  }

  public void setMatcherNeighbors(Integer matcherNeighbors)
  {
    this.matcherNeighbors = matcherNeighbors;
  }

  public Integer getMinNumFeatures()
  {
    return minNumFeatures;
  }

  public void setMinNumFeatures(Integer minNumFeatures)
  {
    this.minNumFeatures = minNumFeatures;
  }

  public Quality getPcQuality()
  {
    return pcQuality;
  }

  public void setPcQuality(Quality pcQuality)
  {
    this.pcQuality = pcQuality;
  }

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("includeGeoLocationFile", this.includeGeoLocationFile);
    object.addProperty("outFileNamePrefix", this.outFileNamePrefix);
    object.addProperty("resolution", this.resolution.toString());
    object.addProperty("matcherNeighbors", this.matcherNeighbors);
    object.addProperty("minNumFeatures", this.minNumFeatures);
    object.addProperty("pcQuality", this.pcQuality.name());

    return object;
  }

  public static ODMProcessConfiguration parse(String jsonString)
  {
    JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

    ODMProcessConfiguration configuration = new ODMProcessConfiguration();

    if (object.has("includeGeoLocationFile"))
    {
      JsonElement element = object.get("includeGeoLocationFile");

      if (!element.isJsonNull())
      {
        configuration.setIncludeGeoLocationFile(object.get("includeGeoLocationFile").getAsBoolean());
      }
    }

    if (object.has("outFileNamePrefix"))
    {
      JsonElement element = object.get("outFileNamePrefix");

      if (!element.isJsonNull())
      {
        configuration.setOutFileNamePrefix(element.getAsString());
      }
    }

    if (object.has("resolution"))
    {
      JsonElement element = object.get("resolution");

      if (!element.isJsonNull())
      {
        configuration.setResolution(new BigDecimal(element.getAsString()));
      }
    }

    if (object.has("matcherNeighbors"))
    {
      JsonElement element = object.get("matcherNeighbors");

      if (!element.isJsonNull())
      {
        configuration.setMatcherNeighbors(Integer.valueOf(object.get("matcherNeighbors").getAsInt()));
      }
    }

    if (object.has("minNumFeatures"))
    {
      JsonElement element = object.get("minNumFeatures");

      if (!element.isJsonNull())
      {
        configuration.setMinNumFeatures(Integer.valueOf(object.get("minNumFeatures").getAsInt()));
      }
    }

    if (object.has("pcQuality"))
    {
      JsonElement element = object.get("pcQuality");

      if (!element.isJsonNull())
      {
        configuration.setPcQuality(Quality.valueOf(object.get("pcQuality").getAsString()));
      }
    }

    return configuration;
  }

  public static ODMProcessConfiguration parse(RequestParserIF parser)
  {
    ODMProcessConfiguration configuration = new ODMProcessConfiguration();

    if (!StringUtils.isEmpty(parser.getCustomParams().get("outFileName")))
    {
      String outFileNamePrefix = parser.getCustomParams().get("outFileName");
      configuration.setOutFileNamePrefix(outFileNamePrefix);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("includeGeoLocationFile")))
    {
      Boolean includeGeoLocationFile = Boolean.valueOf(parser.getCustomParams().get("includeGeoLocationFile"));
      configuration.setIncludeGeoLocationFile(includeGeoLocationFile);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("resolution")))
    {
      BigDecimal resolution = new BigDecimal(parser.getCustomParams().get("resolution"));
      configuration.setResolution(resolution);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("matcherNeighbors")))
    {
      Integer matcherNeighbors = Integer.valueOf(parser.getCustomParams().get("matcherNeighbors"));
      configuration.setMatcherNeighbors(matcherNeighbors);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("minNumFeatures")))
    {
      Integer minNumFeatures = Integer.valueOf(parser.getCustomParams().get("minNumFeatures"));
      configuration.setMinNumFeatures(minNumFeatures);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get("pcQuality")))
    {
      Quality pcQuality = Quality.valueOf(parser.getCustomParams().get("pcQuality"));
      configuration.setPcQuality(pcQuality);
    }

    return configuration;
  }
}
