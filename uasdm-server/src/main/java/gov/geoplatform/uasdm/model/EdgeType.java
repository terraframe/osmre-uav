package gov.geoplatform.uasdm.model;

public interface EdgeType
{
  public static final String PRODUCT_HAS_DOCUMENT       = "gov.geoplatform.uasdm.graph.ProductHasDocument";

  public static final String COMPONENT_HAS_PRODUCT      = "gov.geoplatform.uasdm.graph.ComponentHasProduct";

  public static final String COMPONENT_HAS_DOCUMENT     = "gov.geoplatform.uasdm.graph.ComponentHasDocument";

  public static final String DOCUMENT_GENERATED_PRODUCT = "gov.geoplatform.uasdm.graph.DocumentGeneratedProduct";

  public static final String SITE_HAS_PROJECT           = "gov.geoplatform.uasdm.graph.SiteHasProject";

  public static final String PROJECT_HAS_MISSION        = "gov.geoplatform.uasdm.graph.ProjectHasMission";

  public static final String MISSION_HAS_COLLECTION     = "gov.geoplatform.uasdm.graph.MissionHasCollection";
}
