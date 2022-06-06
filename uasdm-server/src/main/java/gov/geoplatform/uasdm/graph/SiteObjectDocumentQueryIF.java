package gov.geoplatform.uasdm.graph;

import java.util.List;

import gov.geoplatform.uasdm.view.SiteObject;

public interface SiteObjectDocumentQueryIF
{

  public void setSkip(Long skip);

  public void setLimit(Long limit);

  public Long getCount();

  public List<SiteObject> getSiteObjects();

}
