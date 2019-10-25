package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.metadata.MdBusiness;

import gov.geoplatform.uasdm.model.ProjectIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Project extends ProjectBase implements ProjectIF
{
  public static final long serialVersionUID = 935245787;

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
      return this.createImagery();
    }
    else
    {
      return this.createDefaultChild();
    }

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

  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    if (uasComponent instanceof Imagery)
    {
      return this.addImagery((Imagery) uasComponent);
    }
    else
    {
      return this.addSite((Site) uasComponent);
    }
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
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }
}
