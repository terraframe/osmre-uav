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
package gov.geoplatform.uasdm.bus;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.metadata.MdBusiness;

import gov.geoplatform.uasdm.model.ProjectIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Project extends ProjectBase implements ProjectIF
{
  private static final long serialVersionUID = 935245787;

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

  @Override
  public Boolean getRestricted()
  {
    return null;
  }

  @Override
  public String getShortName()
  {
    return null;
  }

  @Override
  public Date getSunsetDate()
  {
    return null;
  }
}
