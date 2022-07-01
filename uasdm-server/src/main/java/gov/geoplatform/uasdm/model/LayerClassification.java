package gov.geoplatform.uasdm.model;

import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;

public enum LayerClassification
{
  ORTHO(ImageryComponent.ORTHO + "/odm_orthophoto.tif"),
  DEM_DSM(ODMZipPostProcessor.DEM_GDAL + "/dsm.tif"),
  DEM_DTM(ODMZipPostProcessor.DEM_GDAL + "/dtm.tif");
  
  private String keyPath;
  
  private LayerClassification(String keyPath)
  {
    this.keyPath = keyPath;
  }

  public String getKeyPath()
  {
    return keyPath;
  }
}
