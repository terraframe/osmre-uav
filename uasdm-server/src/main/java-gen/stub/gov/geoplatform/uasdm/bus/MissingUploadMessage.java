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
package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class MissingUploadMessage extends MissingUploadMessageBase
{
  private static final long serialVersionUID = -495056580;

  public MissingUploadMessage()
  {
    super();
  }

  @Override
  public String getMessage()
  {
    return "No files have been uploaded for collection [" + this.getComponentLabel() + "]";
  }

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return this.getImageryComponent().getName();
  }

  public ImageryComponent getImageryComponent()
  {
    return (ImageryComponent) getComponentInstance();
  }

  public UasComponentIF getComponentInstance()
  {
    return ComponentFacade.getComponent(this.getComponent());
  }

  @Override
  public JSONObject getData()
  {
    JSONObject object = new JSONObject();
    object.put(COMPONENT, this.getComponent());

    return object;
  }

  public static void remove(UasComponentIF collection)
  {
    MissingUploadMessageQuery query = new MissingUploadMessageQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(collection.getOid()));

    try (OIterator<? extends MissingUploadMessage> iterator = query.getIterator())
    {
      iterator.getAll().forEach(message -> {
        message.delete();
      });
    }
  }

}
