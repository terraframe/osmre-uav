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
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class CollectionReportFixer implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionReportFixer.class);

  public static void main(String[] args)
  {
    try
    {
      new CollectionReportFixer().run();
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.shutdown();
      }

    }
  }

  public static void start()
  {
    Thread t = new Thread(new CollectionReportFixer(), "CollectionReportFixer");
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
    logger.error("Fixing collection reports for each collection.");
    
    this.getCollectionsToMigrate().forEach(col -> {
      List<CollectionReport> reports = CollectionReport.getForCollection(col);

      if (reports.size() == 0)
      {
        CollectionReport.create(col);
      }

      CollectionReport.updateSize(col);

      col.getProducts().forEach(product -> {
        CollectionReport.update((Product) product);
      });
    });

    logger.error("Fixing collection reports for each sensor.");

    Sensor.getAll().forEach(sensor -> {
      CollectionReport.update(sensor);
    });

    logger.error("Fixing collection reports for each platform.");
    
    Platform.getAllPlatforms().forEach(platform -> {
      CollectionReport.update(platform);
    });

    logger.error("Fixing collection reports for each uav.");
    UAV.getAll().forEach(uav -> {
      CollectionReport.update(uav);
    });
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
