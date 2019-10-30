package gov.geoplatform.uasdm.model;

import java.util.List;

import org.json.JSONObject;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.Sensor;

public interface CollectionIF extends UasComponentIF
{
  public void addPrivilegeType(AllPrivilegeType privilegeType);

  public List<AllPrivilegeType> getPrivilegeType();

  public Integer getImageWidth();

  public Integer getImageHeight();

  public void setMetadataUploaded(Boolean metadataUploaded);

  public void apply();

  public Sensor getSensor();

  public JSONObject toMetadataMessage();

  public void appLock();
}
