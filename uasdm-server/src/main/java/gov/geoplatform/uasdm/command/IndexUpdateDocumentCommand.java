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
package gov.geoplatform.uasdm.command;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;

public class IndexUpdateDocumentCommand implements Command
{
  private UasComponentIF component;

  private boolean        isNameModified;

  public IndexUpdateDocumentCommand(UasComponentIF component, boolean isNameModified)
  {
    this.component = component;
    this.isNameModified = isNameModified;
  }

  @Override
  public void doIt()
  {
    if (component.isPrivate())
    {
      component.getProducts().forEach(product -> {
        IndexService.removeStacItems(product);
      });
    }

    IndexService.updateComponent(this.component, isNameModified);
  }

  @Override
  public void undoIt()
  {
    UasComponentIF original = ComponentFacade.getComponent(this.component.getOid());

    IndexService.updateComponent(original, isNameModified);
  }

  @Override
  public void doFinally()
  {
  }

  @Override
  public String doItString()
  {
    return "";
  }

  @Override
  public String undoItString()
  {
    return "";
  }

  @Override
  public boolean isUndoable()
  {
    return false;
  }

}
