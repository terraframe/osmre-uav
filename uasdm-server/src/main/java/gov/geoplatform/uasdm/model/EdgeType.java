package gov.geoplatform.uasdm.model;

public interface EdgeType
{
  public static final String PRODUCT_HAS_DOCUMENT       = "gov.geoplatform.uasdm.graph.ProductHasDocument";

  public static final String COMPONENT_HAS_PRODUCT      = "gov.geoplatform.uasdm.graph.ComponentHasProduct";

  public static final String COMPONENT_HAS_DOCUMENT     = "gov.geoplatform.uasdm.graph.ComponentHasDocument";

  public static final String DOCUMENT_GENERATED_PRODUCT = "gov.geoplatform.uasdm.graph.DocumentGeneratedProduct";
}
