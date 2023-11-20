package gov.geoplatform.uasdm.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.service.business.ClassificationTypeBusinessService;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;

@Service
@Primary
public class IDMClassificationTypeBusinessService extends ClassificationTypeBusinessService implements ClassificationTypeBusinessServiceIF
{

  @Override
  @Transaction
  public ClassificationType apply(JsonObject json)
  {
    ClassificationType type = super.apply(json);

    assignPermissions(type.getMdEdge());
    assignPermissions(type.getMdVertex());

    return type;
  }

  private void assignPermissions(ComponentIF component)
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
}
