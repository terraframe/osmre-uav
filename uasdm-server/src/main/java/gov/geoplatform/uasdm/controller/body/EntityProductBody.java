package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotBlank;

public class EntityProductBody
{
  @NotBlank
  private String id;

  @NotBlank
  private String productName;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
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
