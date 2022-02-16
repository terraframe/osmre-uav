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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.BureauQuery;

public class GraphBureauMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(GraphBureauMigration.class);

  public static void main(String[] args)
  {
    new GraphBureauMigration().run();
  }

  public static void start()
  {
    Thread t = new Thread(new MissingRawDocumentFixer(), "CollectionReportMigration");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    transaction();
  }

  @Transaction
  protected void transaction()
  {
    BureauQuery query = new BureauQuery(new QueryFactory());

    try (OIterator<? extends Bureau> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        Bureau bureau = iterator.next();
        
        gov.geoplatform.uasdm.graph.Bureau existingBureau = gov.geoplatform.uasdm.graph.Bureau.getBySource(bureau);
        if(existingBureau == null)
        {
          gov.geoplatform.uasdm.graph.Bureau.create(bureau);
        }
      }
    }
  }
}
