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