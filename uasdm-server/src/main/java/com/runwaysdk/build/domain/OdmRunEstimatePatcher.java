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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.orientechnologies.orient.core.id.ORID;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.ODMRun;

public class OdmRunEstimatePatcher
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
  
//  @Transaction // Transaction is causing max chunk read errors apparently
  public static void mainInTrans()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT @rid,config FROM " + mdVertex.getDBClassName());

    final GraphQuery<Map<String,Object>> query = new GraphQuery<Map<String,Object>>(statement.toString());

    int count = 0;
    
    // If we try to fetch an ODMRun object from the database the odm output causes max chunk read errors unfortunately
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();
    OrientDBRequest orientDBRequest = (OrientDBRequest) request;
    
    for (Map<String,Object> values : query.getResults())
    {
      try
      {
        ORID rid = (ORID) values.get("@rid");
        String config = (String) values.get("config");
        
        if (config == null || config.length() == 0) continue;
        
        JSONObject json = new JSONObject(config);
        
        Set<String> setParams = new HashSet<String>();
        HashMap<String,Object> params = new HashMap<String,Object>();
        params.put("rid", rid);
        putParamIfExists("resolution", params, json, setParams);
        putParamIfExists("featureQuality", params, json, setParams);
        putParamIfExists("pcQuality", params, json, setParams);
        putParamIfExists("matcherNeighbors", params, json, setParams);
        putParamIfExists("minNumFeatures", params, json, setParams);
        putParamIfExists("videoResolution", params, json, setParams);
        putParamIfExists("videoLimit", params, json, setParams);
        putParamIfExists("radiometricCalibration", params, json, setParams);
        
        if (setParams.size() > 0) {
          String attrSetStatement = String.join(", ", setParams.stream().map(p -> p + "=:" + p).collect(Collectors.toList()));
          service.command(orientDBRequest, "UPDATE odmr_un SET " + attrSetStatement + " WHERE @rid=:rid", params);
          count++;
        }
      }
      catch (DataNotFoundException ex) {}
    }
    
    System.out.println("Successfully updated " + count + " odm run objects that did not previously have component references.");
  }
  
  private static void putParamIfExists(String name, HashMap<String,Object> params, JSONObject json, Set<String> setParams) {
    if (json != null && json.has(name) && !json.isNull(name)) {
      params.put(name, json.get(name));
      setParams.add(name);
    }
  }
}
