package gov.geoplatform.uasdm.service;

import org.springframework.stereotype.Component;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.service.LabeledPropertyGraphServiceIF;
import net.geoprism.rbac.RoleConstants;

@Component
public class IDMLabeledPropertyGraphService implements LabeledPropertyGraphServiceIF
{

  @Override
  public void assignPermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());
    
    RoleDAO builderRole = RoleDAO.findRole(RoleConstants.DASHBOARD_BUILDER).getBusinessDAO();
    builderRole.grantPermission(Operation.READ, component.getOid());
    builderRole.grantPermission(Operation.READ_ALL, component.getOid());
  }
  
  @Override
  public void postSynchronization(LabeledPropertyGraphTypeVersion version)
  {
    // TODO use geometry calculations to assign sites to the lowest level of the hierarchy
  }

  @Override
  public void createPublishJob(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing

  }

  @Override
  public void postCreate(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing

  }

  @Override
  public void postDelete(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing

  }

  @Override
  public void preDelete(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing

  }

  @Override
  public String publish(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing
    return null;
  }

}
