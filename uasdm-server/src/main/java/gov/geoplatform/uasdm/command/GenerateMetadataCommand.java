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

import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;

public class GenerateMetadataCommand implements Command
{
  private CollectionIF collection;

  public GenerateMetadataCommand(CollectionIF collection)
  {
    this.collection = collection;
  }

  @Override
  public void doIt()
  {
    new MetadataXMLGenerator().generateAndUpload(this.collection);
  }

  @Override
  public void undoIt()
  {
    CollectionIF original = ComponentFacade.getCollection(this.collection.getOid());

    new MetadataXMLGenerator().generateAndUpload(original);
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
