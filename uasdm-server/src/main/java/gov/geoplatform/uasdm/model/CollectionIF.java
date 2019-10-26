package gov.geoplatform.uasdm.model;

import java.util.List;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;

public interface CollectionIF extends UasComponentIF
{
  public void addPrivilegeType(AllPrivilegeType privilegeType);

  public List<AllPrivilegeType> getPrivilegeType();

  public Integer getImageWidth();

  public Integer getImageHeight();

  public void setMetadataUploaded(Boolean metadataUploaded);

  public void apply();
}
