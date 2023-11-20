package gov.geoplatform.uasdm.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessService;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;

@Service
@Primary
public class IDMGeoObjectTypeSnapshotBusinessService extends GeoObjectTypeSnapshotBusinessService implements GeoObjectTypeSnapshotBusinessServiceIF
{
  @Override
  @Transaction
  public GeoObjectTypeSnapshot create(LabeledPropertyGraphTypeVersion version, JsonObject type)
  {
    GeoObjectTypeSnapshot snapshot = super.create(version, type);
    
    MdVertex component = snapshot.getGraphMdVertex();

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
