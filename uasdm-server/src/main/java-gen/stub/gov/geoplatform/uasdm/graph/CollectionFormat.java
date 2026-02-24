package gov.geoplatform.uasdm.graph;

public enum CollectionFormat {
  STILL_IMAGERY_RGB,
  STILL_THERMAL_RGB,
  STILL_RADIOMETRIC,
  STILL_MULTISPECTRAL,
  VIDEO_RGB,
  VIDEO_THERMAL_RGB,
  VIDEO_RADIOMETRIC,
  VIDEO_MULTISPECTRAL,
  LIDAR;
  
  public static final String[] SUPPORTED_VIDEO_EXTENSIONS = new String[] { "mp4" };
  
  public boolean isMultispectral() {
    return name().toLowerCase().contains("multispectral");
  }
  public boolean isRadiometric() {
    return name().toLowerCase().contains("radiometric");
  }
  public boolean isLidar() {
    return name().toLowerCase().contains("lidar");
  }
  public boolean isRGB() {
    return name().toLowerCase().contains("rgb");
  }
  public boolean isVideo() {
    return name().toLowerCase().contains("video");
  }
  public boolean isStill() {
    return name().toLowerCase().contains("still");
  }
}