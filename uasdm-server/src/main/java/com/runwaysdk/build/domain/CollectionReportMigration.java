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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Collection;

public class CollectionReportMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionReportMigration.class);

  public static void main(String[] args)
  {
    new CollectionReportMigration().run();
  }

  public static void start()
  {
    Thread t = new Thread(new CollectionReportMigration(), "CollectionReportMigration");
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
    List<Collection> collections = this.getCollectionsToMigrate();

    logger.info("Patcher will update [" + collections.size() + "] collections");

    for (Collection col : collections)
    {
      List<CollectionReport> reports = CollectionReport.getForCollection(col);

      if (reports.size() == 0)
      {
        CollectionReport.create(col);

        CollectionReport.updateSize(col);
      }
    }
  }

  private List<Collection> getCollectionsToMigrate()
  {
    MdVertexDAOIF collection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
    final String collection0 = collection.getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + collection0);

    final GraphQuery<Collection> query = new GraphQuery<Collection>(builder.toString());

    return query.getResults();
  }
}
