package gov.geoplatform.uasdm.service;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.springframework.stereotype.Component;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdGraphClassQuery;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.SynchronizationEdge;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.service.LabeledPropertyGraphServiceIF;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;

@Component
public class IDMLabeledPropertyGraphService implements LabeledPropertyGraphServiceIF
{

  public static final String PREFIX = "ha_";

  public static final String SPLIT  = "__";

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
  public void postSynchronization(LabeledPropertyGraphSynchronization synchronization)
  {
    List<Site> sites = Site.getAll();

    for (Site site : sites)
    {
      site.assignHierarchyParents(synchronization);
    }
  }

  @Override
  public void createPublishJob(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing

  }

  @Override
  public void postCreate(LabeledPropertyGraphTypeVersion version)
  {
    if (SynchronizationEdge.get(version) == null)
    {
      MdVertex root = version.getRootType().getGraphMdVertex();
      MdVertexDAOIF site = MdVertexDAO.getMdVertexDAO(Site.CLASS);
      LabeledPropertyGraphType type = version.getGraphType();

      String code = type.getCode();
      String viewName = getTableName(code);
      
      LocalizedValue label = LocalizedValueConverter.convertNoAutoCoalesce(type.getDisplayLabel());

      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, root.getOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, site.getOid());
      LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, label);
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      LabeledPropertyGraphServiceIF.getInstance().assignPermissions(mdEdgeDAO);
      
      SynchronizationEdge edge = new SynchronizationEdge();
      edge.setVersion(version);
      edge.setGraphEdgeId(mdEdgeDAO.getOid());
      edge.apply();
    }
  }

  @Override
  public void postDelete(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing

  }

  @Override
  public void preDelete(LabeledPropertyGraphTypeVersion version)
  {
    SynchronizationEdge edge = SynchronizationEdge.get(version);

    if (edge != null)
    {
      edge.delete();
    }
  }

  @Override
  public String publish(LabeledPropertyGraphTypeVersion version)
  {
    // Do nothing
    return null;
  }

  public String getTableName(String className)
  {
    int count = 0;

    String name = PREFIX + count + SPLIT + className;

    if (name.length() > 25)
    {
      name = name.substring(0, 25);
    }

    while (isTableNameInUse(name))
    {
      count++;

      name = PREFIX + count + className;

      if (name.length() > 25)
      {
        name = name.substring(0, 25);
      }
    }

    return name;
  }

  private boolean isTableNameInUse(String name)
  {
    MdGraphClassQuery query = new MdGraphClassQuery(new QueryFactory());
    query.WHERE(query.getDbClassName().EQ(name));

    return query.getCount() > 0;
  }

}
