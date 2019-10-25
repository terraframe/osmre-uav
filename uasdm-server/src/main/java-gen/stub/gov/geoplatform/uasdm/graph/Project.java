package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.system.metadata.MdBusiness;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.Imagery;
import gov.geoplatform.uasdm.model.ProjectIF;

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
//      return this.createImagery();
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

}
