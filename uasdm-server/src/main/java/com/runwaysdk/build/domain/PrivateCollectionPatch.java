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
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.command.ReIndexStacItemCommand;
import gov.geoplatform.uasdm.controller.PointcloudController;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class PrivateCollectionPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(PrivateCollectionPatch.class);

  public static void main(String[] args)
  {
    try
    {
      new PrivateCollectionPatch().run();
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.finish();
        CacheShutdown.shutdown();
      }
    }

  }

  public static void start()
  {
    Thread t = new Thread(new PrivateCollectionPatch(), "PrivateCollectionPatch");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    transaction();

    logger.error("Transaction committed");
  }

  @Transaction
  protected void transaction()
  {
    this.getComponents().forEach(component -> {
      component.setIsPrivate(false);
      component.apply(false);
    });
  }

  private List<UasComponent> getComponents()
  {
    final String product0 = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + product0);
    builder.append(" WHERE isPrivate IS NULL");

    final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(builder.toString());

    return query.getResults();
  }

}
