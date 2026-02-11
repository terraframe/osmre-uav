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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.orientechnologies.orient.core.id.ORID;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.odm.AutoscalerAwsConfigService;

/**
 * This patcher loops through ODMRuns in the database and calculates their 'instanceType' field based on the current autoscaler prod config file.
 */
public class ReportingServerMetricsPatcher
{
  public static void main(String[] args)
  {
    mainInRequest();
  }
  
  @Request
  public static void mainInRequest()
  {
    mainInTrans();
  }
  
//  @Transaction // Orientdb runs out of memory if we run it in a transaction
  public static void mainInTrans()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT @rid,in().fileSize FROM " + mdVertex.getDBClassName() + " WHERE instanceType IS NULL");

    final GraphQuery<Map<String,Object>> query = new GraphQuery<Map<String,Object>>(statement.toString());

    int count = 0;
    
    for (Map<String,Object> map : query.getResults())
    {
      ORID rid = (ORID) map.get("@rid");
      
      @SuppressWarnings("unchecked") List<Long> fileSizes = (List<Long>) map.get("in().fileSize");
      if (fileSizes == null || fileSizes.size() == 0) continue;
      
      int processedImageCount = fileSizes.size();
      int processedImageSizeMb = (int)(long)fileSizes.stream().map(s -> s == null ? 0L : s/1024L/1024L).reduce(0L, (a,b) -> a + b);
      
      // Estimate instanceType from the current autoscaling configuration. Historically this may not be accurate as the config drifts over time, but an estimate based on current values is better than nothing.
      String instanceType = null;
      if (processedImageCount > 0) {
        instanceType = AutoscalerAwsConfigService.autoscalerMappingForConfig(processedImageCount, processedImageSizeMb).getSlug();
      }
      if (StringUtils.isBlank(instanceType)) continue;
      
      // If we try to fetch an ODMRun object from the database the odm output causes max chunk read errors unfortunately
      GraphDBService service = GraphDBService.getInstance();
      GraphRequest request = service.getGraphDBRequest();
      OrientDBRequest orientDBRequest = (OrientDBRequest) request;
      
      HashMap<String,Object> params = new HashMap<String,Object>();
      params.put("rid", rid);
      params.put("instanceType", instanceType);
      service.command(orientDBRequest, "UPDATE odmr_un SET instanceType=:instanceType WHERE @rid=:rid", params);
      
      count++;
    }
    
    System.out.println("Successfully updated " + count + " odm run objects that did not previously have an instanceType.");
  }
}
