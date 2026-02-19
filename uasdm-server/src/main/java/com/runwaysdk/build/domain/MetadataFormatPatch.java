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
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.SensorType;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;
import software.amazon.awssdk.utils.StringUtils;

public class MetadataFormatPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(MetadataFormatPatch.class);

  public static void main(String[] args)
  {
    try
    {
      new MetadataFormatPatch().run();
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
    Thread t = new Thread(new MetadataFormatPatch(), "MetadataFormatPatch");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    processCollections();

  }

  protected void processCollections()
  {
    List<Collection> collections = this.getCollections();

    logger.error("Patcher will migrate [" + collections.size() + "] existing collections to move the 'collection format' data over to the metadata.");

    int count = 0;
    for (Collection collection : collections)
    {
      if (processCollection(collection))
        count++;
    }

    logger.error("Successfully migrated " + count + " collections.");
  }

  @Transaction
  private boolean processCollection(Collection collection)
  {
    var metadata = collection.getMetadata().orElse(null);
    
    if (metadata != null && !StringUtils.isBlank(collection.getSCollectionFormat())) {
      metadata.setFormat(collection.getSCollectionFormat());
      metadata.apply();
      return true;
    }
    
    return false;
  }

  private List<Collection> getCollections()
  {
    final String collection0 = MdVertexDAO.getMdVertexDAO(Collection.CLASS).getDBClassName();
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COLLECTION_HAS_METADATA);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + collection0);
    builder.append(" WHERE out('" + mdEdge.getDBClassName() + "').size() > 0");

    final GraphQuery<Collection> query = new GraphQuery<Collection>(builder.toString());

    return query.getResults();
  }

}
