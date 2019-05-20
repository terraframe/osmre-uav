/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package com.runwaysdk.patcher.domain;


import org.apache.log4j.Logger;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.bus.AllPrivilegeType;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.CollectionQuery;

public class CollectionPrivilegeMigration
{
  private static Logger logger = Logger.getLogger(CollectionPrivilegeMigration.class);
  
  public static void main(String[] args)
  {
    CollectionQuery cq = new CollectionQuery(new QueryFactory());   
    OIterator<? extends Collection> it = cq.getIterator();
    
    try
    {
      while (it.hasNext())
      {
        Collection col = it.next();
        
        if (col.getPrivilegeType() == null)
        {
          logger.info("Setting default value for privilege type of collection [" + col.getName() + "].");
          
          col.appLock();
          col.addPrivilegeType(AllPrivilegeType.OWNER);
          col.lock();
        }
      }
    }
    finally
    {
      it.close();
    }
  }
}
