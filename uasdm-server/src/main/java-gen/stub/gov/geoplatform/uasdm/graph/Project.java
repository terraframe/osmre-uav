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
import com.runwaysdk.system.metadata.MdBusiness;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProjectIF;
import gov.geoplatform.uasdm.view.AttributeType;

public class Project extends ProjectBase implements ProjectIF
{
  public static final long    serialVersionUID = -400233138;

  private static final String PARENT_EDGE      = "gov.geoplatform.uasdm.graph.SiteHasProject";

  private static final String CHILD_EDGE       = "gov.geoplatform.uasdm.graph.ProjectHasMission";

  public Project()
  {
    super();
  }

  @Override
  public Mission createDefaultChild()
  {
    return this.createMission();
  }

  public Mission createMission()
  {
    return new Mission();
  }

  public Imagery createImagery()
  {
    return new Imagery();
  }

  @Override
  protected String buildProductExpandClause()
  {
    return Project.expandClause();
  }

  @Override
  public List<AttributeType> attributes()
  {
    List<AttributeType> attributes = super.attributes();
    attributes.add(AttributeType.create(this.getMdAttributeDAO(Project.SHORTNAME)));
    attributes.add(AttributeType.create(this.getMdAttributeDAO(Project.RESTRICTED)));
    attributes.add(AttributeType.create(this.getMdAttributeDAO(Project.SUNSETDATE)));

    return attributes;
  }

  /**
   * Create the child of the given type.
   * 
   * @param return
   *          the child of the given type. It assumes the type is valid. It is
   *          the type name of the Runway {@link MdBusiness}.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  @Override
  public UasComponent createChild(String typeName)
  {
    MdBusiness imageryMdBusiness = MdBusiness.getMdBusiness(Imagery.CLASS);

    if (typeName != null && typeName.equals(imageryMdBusiness.getTypeName()))
    {
      // return this.createImagery();
      return null;
    }
    else
    {
      return this.createDefaultChild();
    }

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
  public String getSolrIdField()
  {
    return "projectId";
  }

  @Override
  public String getSolrNameField()
  {
    return "projectName";
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }

  public static String expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PROJECT_HAS_MISSION);

    return "OUT('" + mdEdge.getDBClassName() + "')." + Mission.expandClause();
  }

}
