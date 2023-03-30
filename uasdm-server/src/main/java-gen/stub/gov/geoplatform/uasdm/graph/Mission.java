/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.MissionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.AdminCondition;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public class Mission extends MissionBase implements MissionIF
{
  public static final long    serialVersionUID   = -1962072709;

  private static final String PARENT_EDGE        = "gov.geoplatform.uasdm.graph.ProjectHasMission";

  private static final String CHILD_EDGE         = "gov.geoplatform.uasdm.graph.MissionHasCollection";

  public static final String  ACCESSIBLE_SUPPORT = "accessible_support";

  public Mission()
  {
    super();
  }

  @Override
  public Collection createDefaultChild()
  {
    Collection collection = new Collection();
    collection.addPrivilegeType(AllPrivilegeType.AGENCY);

    return collection;
  }

  @Override
  public String getSolrIdField()
  {
    return "missionId";
  }

  @Override
  public String getSolrNameField()
  {
    return "missionName";
  }

  @Override
  public List<AttributeType> attributes()
  {
    List<AttributeType> attributes = super.attributes();
    attributes.add(AttributeType.create(this.getMdAttributeDAO(Mission.CONTRACTINGOFFICE)));
    attributes.add(AttributeType.create(this.getMdAttributeDAO(Mission.VENDOR)));

    return attributes;
  }

  @Override
  protected MdEdgeDAOIF getParentMdEdge()
  {
    return MdEdgeDAO.getMdEdgeDAO(PARENT_EDGE);
  }

  @Override
  protected MdEdgeDAOIF getChildMdEdge()
  {
    return MdEdgeDAO.getMdEdgeDAO(CHILD_EDGE);
  }

  @Override
  protected String buildProductExpandClause()
  {
    return Mission.expandClause();
  }

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  @Override
  public void applyWithParent(UasComponentIF parent)
  {
    super.applyWithParent(parent);

    if (this.isNew())
    {
      this.createS3Folder(this.buildAccessibleSupportKey());
    }
  }

  @Transaction
  public void delete()
  {
    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildAccessibleSupportKey(), ACCESSIBLE_SUPPORT);
    }
  }

  private String buildAccessibleSupportKey()
  {
    return this.getS3location() + ACCESSIBLE_SUPPORT + "/";
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(String folder, Long pageNumber, Long pageSize)
  {
    List<SiteObject> objects = new LinkedList<SiteObject>();

    if (folder == null)
    {
      SiteObject object = new SiteObject();
      object.setId(this.getOid() + "-" + ACCESSIBLE_SUPPORT);
      object.setName(ACCESSIBLE_SUPPORT);
      object.setComponentId(this.getOid());
      object.setKey(this.buildAccessibleSupportKey());
      object.setType(SiteObject.FOLDER);

      objects.add(object);
    }
    else
    {
      return this.getSiteObjects(folder, objects, pageNumber, pageSize);
    }

    return new SiteObjectsResultSet(objects.size(), pageNumber, pageSize, objects, folder);
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }

  public static String expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.MISSION_HAS_COLLECTION);

    // return "OUT('" + mdEdge.getDBClassName() + "')." +
    // Collection.expandClause();
    return "OUT('" + mdEdge.getDBClassName() + "')";
  }
}
