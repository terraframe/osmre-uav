/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.command;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.service.IndexService;

public class ReIndexStacItemCommand implements Command
{
  private ProductIF product;

  public ReIndexStacItemCommand(ProductIF product)
  {
    this.product = product;
  }

  @Override
  public void doIt()
  {
    IndexService.createStacItems(this.product);
  }

  @Override
  public void undoIt()
  {
    ProductIF original = ComponentFacade.getProduct(this.product.getOid());

    IndexService.createStacItems(original);
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
