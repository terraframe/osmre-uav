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

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.service.IndexService;

public class ODMRunEdgeFix implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(ODMRunEdgeFix.class);

  public static void main(String[] args)
  {
    new ODMRunEdgeFix().run();
  }

  public static void start()
  {
    Thread t = new Thread(new ODMRunEdgeFix(), "ODMRunEdgeFix");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    transaction();
    
    IndexService.shutdown();
  }

  @Transaction
  protected void transaction()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.ODM_RUN_OUTPUT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE in('" + mdEdge.getDBClassName() + "').size() > 1");

    final GraphQuery<Document> query = new GraphQuery<Document>(statement.toString());

    query.getResults().forEach(result -> {
      ODMRun run = result.getGeneratingODMRun();

      result.getODMRunOutputParentODMRuns().stream().filter(existing -> !existing.getOid().equals(run.getOid())).forEach(existing -> {
        result.removeODMRunOutputParent(existing);
      });

    });

  }
}
