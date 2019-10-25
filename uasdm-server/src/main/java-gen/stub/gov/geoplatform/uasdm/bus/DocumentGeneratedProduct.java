package gov.geoplatform.uasdm.bus;

public class DocumentGeneratedProduct extends DocumentGeneratedProductBase
{
  public static final long serialVersionUID = 371773372;
  
  public DocumentGeneratedProduct(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public DocumentGeneratedProduct(gov.geoplatform.uasdm.bus.Document parent, gov.geoplatform.uasdm.bus.Product child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
