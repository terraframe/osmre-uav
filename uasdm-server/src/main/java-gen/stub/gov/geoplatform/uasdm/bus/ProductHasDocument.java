package gov.geoplatform.uasdm.bus;

public class ProductHasDocument extends ProductHasDocumentBase
{
  private static final long serialVersionUID = 576174881;

  public ProductHasDocument(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }

  public ProductHasDocument(gov.geoplatform.uasdm.bus.Product parent, gov.geoplatform.uasdm.bus.Document child)
  {
    this(parent.getOid(), child.getOid());
  }

}
