package gov.geoplatform.uasdm.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessService;
import net.geoprism.registry.service.business.GraphTypeSnapshotBusinessServiceIF;

@Service
@Primary
public class IDMGraphTypeSnapshotBusinessService extends GraphTypeSnapshotBusinessService implements GraphTypeSnapshotBusinessServiceIF
{
  @Override
  protected void assignPermissions(ComponentIF component)
  {
    super.assignPermissions(component);
    
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
