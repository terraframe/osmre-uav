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
package gov.geoplatform.uasdm.test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.ProcessConfiguration.ProcessType;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;

public class TestProcessingRunInfo
{
  public static int COUNTING_SEQUENCE = 0;
  
  protected TestUasComponentInfo component;
  
  protected TestProductInfo product;
  
  protected ProcessConfiguration config;
  
  // ODMRuns don't have a great way to fetch them without referencing other objects (like the component or the product).
  // This mechanism allows us to uniquely identify and fetch an ODMRun purely based on what sequence the test harness had
  // constructed it.
  protected int constructSequence = COUNTING_SEQUENCE++;

  public TestProcessingRunInfo(TestCollectionInfo component, ProcessConfiguration config, TestProductInfo product)
  {
    this.component = component;
    this.config = config;
    this.product = product;
  }
  
  public TestProcessingRunInfo(TestCollectionInfo component, ProcessType tool, TestProductInfo product)
  {
    this.component = component;
    this.product = product;
    
    if (ProcessType.ODM.equals(tool)) {
      this.config = new ODMProcessConfiguration();
    } else if (ProcessType.LIDAR.equals(tool)) {
      this.config = new LidarProcessConfiguration();
    } else {
      throw new UnsupportedOperationException(tool.name());
    }
    
    this.config.setProductName(product.getProductName());
  }

  public ODMRun apply()
  {
    UasComponent component = this.component.getServerObject();
    
    ODMRun odmRun = new ODMRun();
//  odmRun.setWorkflowTask(task);
//  odmRun.setConfig(task.getConfigurationJson());
    odmRun.setRunStart(Date.from(dateFromSequence(constructSequence).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    odmRun.setComponent(component);
    odmRun.setConfig(this.config.toJson().toString());
    odmRun.apply();
    
    this.product.getServerInputDocuments().forEach(doc -> odmRun.addODMRunInputParent(doc).apply());
    
    for (Document document : this.product.getServerOutputDocuments())
    {
      odmRun.addODMRunOutputChild(document).apply();
      
      if (document.getName().equals("report.pdf"))
      {
        odmRun.setReport(document);
      }
    }
    
    return odmRun;
  }
  
  public TestProductInfo getProduct()
  {
    return this.product;
  }
  
  public ODMRun getServerObject()
  {
//    return ODMRun.getGeneratingRun(this.product.getOutputDocuments().get(0).getServerObject());
    return getOdmRun(dateFromSequence(constructSequence));
  }

  public void delete()
  {
    var server = this.getServerObject();

    if (server != null)
    {
      server.delete();
    }
  }
  
  public static LocalDate dateFromSequence(int seq)
  {
    return LocalDate.of(2000 + seq, 1, 1);
  }
  
  /**
   * The assumption here is that the start date is unique. This won't be true in production but we can make it true in our test harness.
   * 
   * @param runStart
   * @return
   */
  public static ODMRun getOdmRun(LocalDate runStart)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);
    final String attr = mdVertex.definesAttribute(ODMRun.RUNSTART).getColumnName();
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + attr + " = :start");

    final GraphQuery<ODMRun> query = new GraphQuery<ODMRun>(statement.toString());
    query.setParameter("start", runStart);
    
    return query.getSingleResult();
  }
}
