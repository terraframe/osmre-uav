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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.service.IndexService;

public class IndexDeleteStacCommand implements Command
{
  private Logger    log = LoggerFactory.getLogger(IndexDeleteStacCommand.class);

  private ProductIF product;

  public IndexDeleteStacCommand(ProductIF product)
  {
    this.product = product;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Deleting the stac item from the index for the product [" + this.product.getOid() + "]");

    try
    {
      IndexService.removeStacItems(product);
    }
    catch (RuntimeException e)
    {
      log.error("Error indexing stac item", e);

      throw e;
    }

  }

  /**
   * Executes the undo in this Command, and closes the connection.
   */
  public void undoIt()
  {
    try
    {
      IndexService.createStacItems(product);
    }
    catch (RuntimeException e)
    {
      log.error("Error indexing stac item", e);

      throw e;
    }

  }

  /**
   * Returns a human readable string describing what this command is trying to
   * do.
   * 
   * @return human readable string describing what this command is trying to do.
   */
  public String doItString()
  {
    return null;
  }

  public String undoItString()
  {
    return null;
  }

  /*
   * Indicates if this Command deletes something.
   * 
   * @return <code><b>true</b></code> if this Command deletes something.
   */
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void doFinally()
  {
  }

}
