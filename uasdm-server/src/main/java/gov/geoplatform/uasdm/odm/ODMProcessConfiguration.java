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
package gov.geoplatform.uasdm.odm;

import java.math.BigDecimal;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import me.desair.tus.server.upload.UploadInfo;

public class ODMProcessConfiguration implements ProcessConfiguration
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

  public static enum RadiometricCalibration {
    NONE("none"), CAMERA("camera"), CAMERA_SUN("camera+sun");

    private String code;

    private RadiometricCalibration(String code)
    {
      this.code = code;
    }

    public String getCode()
    {
      return code;
    }
  }

  public static enum FileFormat {
    ODM, RX1R2;
  }

  public static final String     INCLUDE_GEO_LOCATION_FILE         = "includeGeoLocationFile";

  public static final String     GEO_LOCATION_FORMAT               = "geoLocationFormat";

  public static final String     GEO_LOCATION_FILE_NAME            = "geoLocationFileName";

  public static final String     OUT_FILE_NAME_PREFIX              = "outFileNamePrefix";

  public static final String     RESOLUTION                        = "resolution";

  public static final String     MATCHING_NEIGHBORS                = "matcherNeighbors";

  public static final String     MIN_NUM_FEATURES                  = "minNumFeatures";

  public static final String     PC_QUALITY                        = "pcQuality";

  public static final String     FEATURE_QUALITY                   = "featureQuality";

  public static final String     RADIOMETRIC_CALIBRATION           = "radiometricCalibration";

  public static final String     INCLUDE_GROUND_CONTROL_POINT_FILE = "includeGroundControlPointFile";

  public static final String     GROUND_CONTROL_POINT_FILE_NAME    = "GroundControlPointFileName";

  public static final String     PRODUCT_NAME                      = "productName";

  public static final String     PROCESS_PT_CLOUD                  = "processPtcloud";

  public static final String     PROCESS_DEM                       = "processDem";

  public static final String     PROCESS_ORTHO                     = "processOrtho";
  
  public static final String     VIDEO_RESOLUTION                  = "videoResolution";
  
  public static final String     VIDEO_LIMIT                       = "videoLimit";

  private boolean                includeGeoLocationFile;

  private FileFormat             geoLocationFormat;

  private String                 geoLocationFileName;

  private String                 outFileNamePrefix;

  private boolean                includeGroundControlPointFile;

  private String                 groundControlPointFileName;

  private BigDecimal             resolution;
  
  /*
   * Maximum number of frames to extract from video files for processing. Set to 0 for no limit. Default: 500
   */
  private Integer                videoLimit;

  /*
   * video-resolution <positive integer>
   * 
   * The maximum output resolution of extracted video frames in pixels. Default:
   * 4000
   */
  private Integer                videoResolution;

  /*
   * matcher-neighbors <positive integer>
   * 
   * Perform image matching with the nearest images based on GPS exif data. Set
   * to 0 to match by triangulation. Default: 0
   */
  private Integer                matcherNeighbors;

  /*
   * min-num-features <integer>
   * 
   * Minimum number of features to extract per image. More features can be
   * useful for finding more matches between images, potentially allowing the
   * reconstruction of areas with little overlap or insufficient features. More
   * features also slow down processing. Default: 10000
   */
  private Integer                minNumFeatures;

  /*
   * pc-quality <ultra | high | medium | low | lowest>
   * 
   * Set point cloud quality. Higher quality generates better, denser point
   * clouds, but requires more memory and takes longer. Each step up in quality
   * increases processing time roughly by a factor of 4x.
   * 
   * Default: medium
   */
  private Quality                pcQuality;

  /*
   * feature-quality <ultra | high | medium | low | lowest>
   * 
   * Set feature extraction quality. Higher quality generates better features,
   * but requires more memory and takes longer.
   * 
   * Default: high
   */
  private Quality                featureQuality;

  /*
   * radiometric-calibration <none | camera | camera+sun>
   * 
   * Set the radiometric calibration to perform on images. When processing
   * multispectral and thermal images you should set this option to obtain
   * reflectance/temperature values (otherwise you will get digital number
   * values). [camera] applies black level, vignetting, row gradient
   * gain/exposure compensation (if appropriate EXIF tags are found) and
   * computes absolute temperature values. [camera+sun] is experimental, applies
   * all the corrections of [camera], plus compensates for spectral radiance
   * registered via a downwelling light sensor (DLS) taking in consideration the
   * angle of the sun.
   * 
   * default: none
   */
  private RadiometricCalibration radiometricCalibration;

  private String                 productName;

  private Boolean                processPtcloud;

  private Boolean                processDem;

  private Boolean                processOrtho;

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
    this.pcQuality = Quality.MEDIUM;
    this.featureQuality = Quality.HIGH;
    this.geoLocationFormat = FileFormat.RX1R2;
    this.geoLocationFileName = Product.GEO_LOCATION_FILE;
    this.radiometricCalibration = RadiometricCalibration.NONE;
    this.includeGroundControlPointFile = false;
    this.groundControlPointFileName = "gcp_list.txt";
    this.productName = Long.valueOf(System.currentTimeMillis()).toString();
  }

  @Override
  public ProcessType getType()
  {
    return ProcessType.ODM;
  }

  public String getProductName()
  {
    return productName;
  }

  public void setProductName(String productName)
  {
    this.productName = productName;
  }

  public RadiometricCalibration getRadiometricCalibration()
  {
    return radiometricCalibration;
  }

  public void setRadiometricCalibration(RadiometricCalibration radiometricCalibration)
  {
    this.radiometricCalibration = radiometricCalibration;
  }

  public Quality getFeatureQuality()
  {
    return featureQuality;
  }

  public void setFeatureQuality(Quality featureQuality)
  {
    this.featureQuality = featureQuality;
  }

  public FileFormat getGeoLocationFormat()
  {
    return geoLocationFormat;
  }

  public void setGeoLocationFormat(FileFormat geoLocationFormat)
  {
    this.geoLocationFormat = geoLocationFormat;
  }

  public boolean isIncludeGeoLocationFile()
  {
    return includeGeoLocationFile;
  }

  public void setIncludeGeoLocationFile(boolean includeGeoLocationFile)
  {
    this.includeGeoLocationFile = includeGeoLocationFile;
  }

  public String getGeoLocationFileName()
  {
    return geoLocationFileName;
  }

  public void setGeoLocationFileName(String geoLocationFileName)
  {
    this.geoLocationFileName = geoLocationFileName;
  }

  public boolean isIncludeGroundControlPointFile()
  {
    return includeGroundControlPointFile;
  }

  public void setIncludeGroundControlPointFile(boolean includeGroundControlPointFile)
  {
    this.includeGroundControlPointFile = includeGroundControlPointFile;
  }

  public String getGroundControlPointFileName()
  {
    return groundControlPointFileName;
  }

  public void setGroundControlPointFileName(String groundControlPointFileName)
  {
    this.groundControlPointFileName = groundControlPointFileName;
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
  
  public Integer getVideoLimit()
  {
    return videoLimit;
  }

  public void setVideoLimit(Integer videoLimit)
  {
    this.videoLimit = videoLimit;
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

  public Boolean getProcessPtcloud()
  {
    return processPtcloud;
  }

  public void setProcessPtcloud(Boolean processPtcloud)
  {
    this.processPtcloud = processPtcloud;
  }

  public Boolean getProcessDem()
  {
    return processDem;
  }

  public void setProcessDem(Boolean processDem)
  {
    this.processDem = processDem;
  }

  public Boolean getProcessOrtho()
  {
    return processOrtho;
  }

  public void setProcessOrtho(Boolean processOrtho)
  {
    this.processOrtho = processOrtho;
  }

  @Override
  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty(TYPE, this.getType().name());
    object.addProperty(INCLUDE_GEO_LOCATION_FILE, this.includeGeoLocationFile);
    object.addProperty(GEO_LOCATION_FORMAT, this.geoLocationFormat.toString());
    object.addProperty(GEO_LOCATION_FILE_NAME, this.geoLocationFileName);
    object.addProperty(OUT_FILE_NAME_PREFIX, this.outFileNamePrefix);
    object.addProperty(RESOLUTION, this.resolution.toString());
    object.addProperty(MATCHING_NEIGHBORS, this.matcherNeighbors);
    object.addProperty(MIN_NUM_FEATURES, this.minNumFeatures);
    object.addProperty(PC_QUALITY, this.pcQuality.name());
    object.addProperty(FEATURE_QUALITY, this.featureQuality.name());
    object.addProperty(RADIOMETRIC_CALIBRATION, this.radiometricCalibration.name());
    object.addProperty(INCLUDE_GROUND_CONTROL_POINT_FILE, this.includeGroundControlPointFile);
    object.addProperty(GROUND_CONTROL_POINT_FILE_NAME, this.groundControlPointFileName);
    object.addProperty(PRODUCT_NAME, this.productName);
    object.addProperty(VIDEO_RESOLUTION, videoResolution);
    object.addProperty(VIDEO_LIMIT, videoLimit);

    return object;
  }

  public static ODMProcessConfiguration parse(UploadInfo uploadInfo)
  {
    Map<String, String> metadata = uploadInfo.getMetadata();

    ODMProcessConfiguration configuration = new ODMProcessConfiguration();

    if (metadata.containsKey(PRODUCT_NAME))
    {
      configuration.setProductName(metadata.get(PRODUCT_NAME));
    }

    if (metadata.containsKey(INCLUDE_GEO_LOCATION_FILE))
    {
      configuration.setIncludeGeoLocationFile(Boolean.valueOf(metadata.get(INCLUDE_GEO_LOCATION_FILE)));
    }

    if (metadata.containsKey(GEO_LOCATION_FORMAT))
    {
      configuration.setGeoLocationFormat(FileFormat.valueOf(metadata.get(GEO_LOCATION_FORMAT)));
    }

    if (metadata.containsKey(GEO_LOCATION_FILE_NAME))
    {
      configuration.setGeoLocationFileName(metadata.get(GEO_LOCATION_FILE_NAME));
    }

    if (metadata.containsKey(INCLUDE_GROUND_CONTROL_POINT_FILE))
    {
      configuration.setIncludeGroundControlPointFile(Boolean.valueOf(metadata.get(INCLUDE_GROUND_CONTROL_POINT_FILE)));
    }

    if (metadata.containsKey(GROUND_CONTROL_POINT_FILE_NAME))
    {
      configuration.setGroundControlPointFileName(metadata.get(GROUND_CONTROL_POINT_FILE_NAME));
    }

    if (metadata.containsKey(OUT_FILE_NAME_PREFIX))
    {
      configuration.setOutFileNamePrefix(metadata.get(OUT_FILE_NAME_PREFIX));
    }

    if (metadata.containsKey(RESOLUTION))
    {
      configuration.setResolution(new BigDecimal(metadata.get(RESOLUTION)));
    }
    
    if (metadata.containsKey(VIDEO_RESOLUTION))
    {
      configuration.setVideoResolution(Integer.parseInt(metadata.get(VIDEO_RESOLUTION)));
    }
    
    if (metadata.containsKey(VIDEO_LIMIT))
    {
      configuration.setVideoLimit(Integer.parseInt(metadata.get(VIDEO_LIMIT)));
    }

    if (metadata.containsKey(MATCHING_NEIGHBORS))
    {
      configuration.setMatcherNeighbors(Integer.valueOf(metadata.get(MATCHING_NEIGHBORS)));
    }

    if (metadata.containsKey(MIN_NUM_FEATURES))
    {
      configuration.setMinNumFeatures(Integer.valueOf(metadata.get(MIN_NUM_FEATURES)));
    }

    if (metadata.containsKey(PC_QUALITY))
    {
      configuration.setPcQuality(Quality.valueOf(metadata.get(PC_QUALITY)));
    }

    if (metadata.containsKey(FEATURE_QUALITY))
    {
      configuration.setFeatureQuality(Quality.valueOf(metadata.get(FEATURE_QUALITY)));
    }

    if (metadata.containsKey(RADIOMETRIC_CALIBRATION))
    {
      configuration.setRadiometricCalibration(RadiometricCalibration.valueOf(metadata.get(RADIOMETRIC_CALIBRATION)));
    }

    configuration.setProcessOrtho(Boolean.valueOf(metadata.getOrDefault(PROCESS_ORTHO, "false")));
    configuration.setProcessDem(Boolean.valueOf(metadata.getOrDefault(PROCESS_DEM, "false")));
    configuration.setProcessPtcloud(Boolean.valueOf(metadata.getOrDefault(PROCESS_PT_CLOUD, "false")));

    return configuration;
  }

  public static ODMProcessConfiguration parse(String jsonString)
  {
    JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

    ODMProcessConfiguration configuration = new ODMProcessConfiguration();

    if (object.has(PRODUCT_NAME))
    {
      JsonElement element = object.get(PRODUCT_NAME);

      if (!element.isJsonNull())
      {
        configuration.setProductName(object.get(PRODUCT_NAME).getAsString());
      }
    }

    if (object.has(INCLUDE_GEO_LOCATION_FILE))
    {
      JsonElement element = object.get(INCLUDE_GEO_LOCATION_FILE);

      if (!element.isJsonNull())
      {
        configuration.setIncludeGeoLocationFile(object.get(INCLUDE_GEO_LOCATION_FILE).getAsBoolean());
      }
    }

    if (object.has(GEO_LOCATION_FORMAT))
    {
      JsonElement element = object.get(GEO_LOCATION_FORMAT);

      if (!element.isJsonNull())
      {
        configuration.setGeoLocationFormat(FileFormat.valueOf(object.get(GEO_LOCATION_FORMAT).getAsString()));
      }
    }

    if (object.has(GEO_LOCATION_FILE_NAME))
    {
      JsonElement element = object.get(GEO_LOCATION_FILE_NAME);

      if (!element.isJsonNull())
      {
        configuration.setGeoLocationFileName(object.get(GEO_LOCATION_FILE_NAME).getAsString());
      }
    }

    if (object.has(INCLUDE_GROUND_CONTROL_POINT_FILE))
    {
      JsonElement element = object.get(INCLUDE_GROUND_CONTROL_POINT_FILE);

      if (!element.isJsonNull())
      {
        configuration.setIncludeGroundControlPointFile(object.get(INCLUDE_GROUND_CONTROL_POINT_FILE).getAsBoolean());
      }
    }

    if (object.has(GROUND_CONTROL_POINT_FILE_NAME))
    {
      JsonElement element = object.get(GROUND_CONTROL_POINT_FILE_NAME);

      if (!element.isJsonNull())
      {
        configuration.setGroundControlPointFileName(object.get(GROUND_CONTROL_POINT_FILE_NAME).getAsString());
      }
    }

    if (object.has(OUT_FILE_NAME_PREFIX))
    {
      JsonElement element = object.get(OUT_FILE_NAME_PREFIX);

      if (!element.isJsonNull())
      {
        configuration.setOutFileNamePrefix(element.getAsString());
      }
    }

    if (object.has(RESOLUTION))
    {
      JsonElement element = object.get(RESOLUTION);

      if (!element.isJsonNull())
      {
        configuration.setResolution(new BigDecimal(element.getAsString()));
      }
    }
    
    if (object.has(VIDEO_RESOLUTION) && !object.get(VIDEO_RESOLUTION).isJsonNull())
    {
      configuration.setVideoResolution(object.get(VIDEO_RESOLUTION).getAsInt());
    }
    
    if (object.has(VIDEO_LIMIT) && !object.get(VIDEO_LIMIT).isJsonNull())
    {
      configuration.setVideoLimit(object.get(VIDEO_LIMIT).getAsInt());
    }

    if (object.has(MATCHING_NEIGHBORS))
    {
      JsonElement element = object.get(MATCHING_NEIGHBORS);

      if (!element.isJsonNull())
      {
        configuration.setMatcherNeighbors(Integer.valueOf(object.get(MATCHING_NEIGHBORS).getAsInt()));
      }
    }

    if (object.has(MIN_NUM_FEATURES))
    {
      JsonElement element = object.get(MIN_NUM_FEATURES);

      if (!element.isJsonNull())
      {
        configuration.setMinNumFeatures(Integer.valueOf(object.get(MIN_NUM_FEATURES).getAsInt()));
      }
    }

    if (object.has(PC_QUALITY))
    {
      JsonElement element = object.get(PC_QUALITY);

      if (!element.isJsonNull())
      {
        configuration.setPcQuality(Quality.valueOf(object.get(PC_QUALITY).getAsString()));
      }
    }

    if (object.has(FEATURE_QUALITY))
    {
      JsonElement element = object.get(FEATURE_QUALITY);

      if (!element.isJsonNull())
      {
        configuration.setFeatureQuality(Quality.valueOf(object.get(FEATURE_QUALITY).getAsString()));
      }
    }

    if (object.has(RADIOMETRIC_CALIBRATION))
    {
      JsonElement element = object.get(RADIOMETRIC_CALIBRATION);

      if (!element.isJsonNull())
      {
        configuration.setRadiometricCalibration(RadiometricCalibration.valueOf(object.get(RADIOMETRIC_CALIBRATION).getAsString()));
      }
    }

    if (object.has(PROCESS_ORTHO))
    {
      JsonElement element = object.get(PROCESS_ORTHO);

      if (!element.isJsonNull())
      {
        configuration.setProcessOrtho(object.get(PROCESS_ORTHO).getAsBoolean());
      }
    }

    if (object.has(PROCESS_DEM))
    {
      JsonElement element = object.get(PROCESS_DEM);

      if (!element.isJsonNull())
      {
        configuration.setProcessDem(object.get(PROCESS_DEM).getAsBoolean());
      }
    }

    if (object.has(PROCESS_PT_CLOUD))
    {
      JsonElement element = object.get(PROCESS_PT_CLOUD);

      if (!element.isJsonNull())
      {
        configuration.setProcessPtcloud(object.get(PROCESS_PT_CLOUD).getAsBoolean());
      }
    }

    return configuration;
  }
}
