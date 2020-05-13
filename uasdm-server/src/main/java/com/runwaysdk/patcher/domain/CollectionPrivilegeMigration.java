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
package com.runwaysdk.patcher.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.CollectionQuery;

public class CollectionPrivilegeMigration
{
  private static Logger logger = LoggerFactory.getLogger(CollectionPrivilegeMigration.class);

  public static void main(String[] args)
  {
    CollectionQuery cq = new CollectionQuery(new QueryFactory());

    try (OIterator<? extends Collection> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Collection col = it.next();

        if (col.getPrivilegeType() == null)
        {
          logger.info("Setting default value for privilege type of collection [" + col.getName() + "].");

          col.appLock();
          col.addPrivilegeType(AllPrivilegeType.AGENCY);
          col.lock();
        }
      }
    }
  }
}
