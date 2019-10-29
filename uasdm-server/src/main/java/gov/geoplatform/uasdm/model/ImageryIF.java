package gov.geoplatform.uasdm.model;

public interface ImageryIF extends ImageryComponent
{

  public String buildGeoRefKey();

  public void setImageHeight(Integer height);

  public void setImageWidth(Integer width);

  public void apply();

}
