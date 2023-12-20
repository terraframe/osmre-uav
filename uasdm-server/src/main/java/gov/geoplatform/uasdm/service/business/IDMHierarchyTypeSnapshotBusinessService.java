package gov.geoplatform.uasdm.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessService;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;

@Service
@Primary
public class IDMHierarchyTypeSnapshotBusinessService extends HierarchyTypeSnapshotBusinessService implements HierarchyTypeSnapshotBusinessServiceIF
{
  @Transaction
  @Override
  public HierarchyTypeSnapshot create(LabeledPropertyGraphTypeVersion version, JsonObject type, GeoObjectTypeSnapshot root)
  {
    HierarchyTypeSnapshot snapshot = super.create(version, type, root);

    MdEdge component = snapshot.getGraphMdEdge();

    RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO builderRole = RoleDAO.findRole(RoleConstants.DASHBOARD_BUILDER).getBusinessDAO();
    builderRole.grantPermission(Operation.READ, component.getOid());
    builderRole.grantPermission(Operation.READ_ALL, component.getOid());

    return snapshot;
  }
}
