package gov.geoplatform.uasdm.model;

import java.util.List;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import net.geoprism.GeoprismUser;

public interface CollectionIF extends UasComponentIF
{
  public void addPrivilegeType(AllPrivilegeType privilegeType);

  public List<AllPrivilegeType> getPrivilegeType();
}
