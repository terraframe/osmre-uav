package gov.geoplatform.uasdm.model;

public interface DocumentIF
{

  public String getS3location();

  public String getName();

  public void addGeneratedProduct(ProductIF product);

  public void delete();

}
