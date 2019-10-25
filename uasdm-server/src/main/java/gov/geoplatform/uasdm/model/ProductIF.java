package gov.geoplatform.uasdm.model;

import java.util.Date;
import java.util.List;

public interface ProductIF
{
  public String getOid();

  public String getName();

  public String getImageKey();

  public String getMapKey();

  public Date getLastUpdateDate();

  public String getBoundingBox();

  public void updateBoundingBox();

  public void clear();

  public void addDocuments(List<DocumentIF> documents);

  public UasComponentIF getComponent();

  public List<DocumentIF> getGeneratedFromDocuments();

  public void calculateKeys(List<UasComponentIF> components);
}
