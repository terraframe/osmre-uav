package gov.geoplatform.uasdm.service;

import org.springframework.stereotype.Component;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.service.ClassificationObjectServiceIF;

@Component
public class IDMClassificationObjectService implements ClassificationObjectServiceIF
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
  public void validateName(String name)
  {
//    if (!MasterList.isValidName(name))
//    {
//      throw new InvalidMasterListCodeException("The geo object type code has an invalid character");
//    }
  }

}
