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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;

public class IndexCreateDocumentCommand implements Command
{
  private static final Logger  logger = LoggerFactory.getLogger(IndexCreateDocumentCommand.class);

  private List<UasComponentIF> ancestors;

  private UasComponentIF       component;

  public IndexCreateDocumentCommand(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    this.ancestors = ancestors;
    this.component = component;
  }

  @Override
  public void doIt()
  {
    try
    {
      IndexService.createDocument(this.ancestors, this.component);
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
      IndexService.deleteDocuments(this.component.getSolrIdField(), this.component.getOid());
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
