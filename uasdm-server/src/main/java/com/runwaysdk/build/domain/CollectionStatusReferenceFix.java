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
package com.runwaysdk.build.domain;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.CollectionStatusQuery;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class CollectionStatusReferenceFix
{
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }

  @Request
  public static void doIt() throws InterruptedException
  {
    CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());
    try (OIterator<? extends CollectionStatus> it = query.getIterator())
    {
      while (it.hasNext())
      {
        CollectionStatus cs = it.next();
        
        String componentId = cs.getComponent();
        
        UasComponentIF component = ComponentFacade.getComponent(componentId);
        
        if (component == null)
        {
          cs.delete();
        }
      }
    }
  }
}
