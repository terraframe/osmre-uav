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

import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.view.FlightMetadata;

public class PointOfContactMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(PointOfContactMigration.class);

  public static void main(String[] args)
  {
    new PointOfContactMigration().run();
  }

  public static void start()
  {
    Thread t = new Thread(new PointOfContactMigration(), "PointOfContactMigration");
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
    List<Collection> collections = this.getCollections();

    logger.info("Patcher will update [" + collections.size() + "] collections");

    for (Collection collection : collections)
    {
      try
      {

        FlightMetadata fMetadata = FlightMetadata.get(collection, Collection.RAW, collection.getFolderName() + MetadataXMLGenerator.FILENAME);

        if (fMetadata != null)
        {
          collection.setPocName(fMetadata.getName());
          collection.setPocEmail(fMetadata.getEmail());
          collection.apply();
        }
        else
        {
          logger.info("Unable to find metadata for " + collection.getName());
        }
      }
      catch (Exception e)
      {
        logger.info("Unable to update metadata for " + collection.getName(), e);
      }
    }
  }

  private List<Collection> getCollections()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
    final String collection0 = mdVertex.getDBClassName();
    final String pocName = mdVertex.definesAttribute(Collection.POCNAME).getColumnName();
    final String pocEmail = mdVertex.definesAttribute(Collection.POCEMAIL).getColumnName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + collection0);
    builder.append(" WHERE " + pocName + " IS NULL");
    builder.append(" OR " + pocEmail + " IS NULL");

    final GraphQuery<Collection> query = new GraphQuery<Collection>(builder.toString());

    return query.getResults();
  }

}
