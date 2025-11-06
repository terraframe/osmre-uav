package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotEmpty;

public class CreateProductBody
{
  @NotEmpty
  private String collectionId;

  @NotEmpty
  private String productName;

  public String getCollectionId()
  {
    return collectionId;
  }

  public void setCollectionId(String collectionId)
  {
    this.collectionId = collectionId;
  }

  public String getProductName()
  {
    return productName;
  }

  public void setProductName(String productName)
  {
    this.productName = productName;
  }

}