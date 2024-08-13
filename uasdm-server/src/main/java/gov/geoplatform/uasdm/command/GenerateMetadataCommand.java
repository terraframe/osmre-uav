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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class GenerateMetadataCommand implements Command
{
  private static final Logger logger = LoggerFactory.getLogger(GenerateMetadataCommand.class);

  private UasComponentIF        component;
  
  private Product product;
  
  private CollectionMetadata metadata;

  public GenerateMetadataCommand(UasComponentIF component, Product product, CollectionMetadata metadata)
  {
    this.component = component;
    this.product = product;
    this.metadata = metadata;
  }

  @Override
  public void doIt()
  {
    try
    {
      new MetadataXMLGenerator().generateAndUpload(this.component, this.product, this.metadata);
    }
    catch (RuntimeException e)
    {
      logger.error("Error indexing stac item", e);

      throw e;
    }

  }

  @Override
  public void undoIt()
  {
    try
    {
      UasComponentIF original = ComponentFacade.getCollection(this.component.getOid());
      Product origProduct = this.product == null ? null : Product.get(this.product.getOid());
      CollectionMetadata origMeta = CollectionMetadata.get(this.metadata.getOid());

      new MetadataXMLGenerator().generateAndUpload(original, origProduct, origMeta);
    }
    catch (RuntimeException e)
    {
      logger.error("Error indexing stac item", e);

      throw e;
    }

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
