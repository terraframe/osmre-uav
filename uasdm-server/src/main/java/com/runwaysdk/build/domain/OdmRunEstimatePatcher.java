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
import java.util.Map;

import com.orientechnologies.orient.core.id.ORID;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.UasComponent;

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
//    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS);
    
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();
    OrientDBRequest orientDBRequest = (OrientDBRequest) request;
    
    for(var statement : UPDATE_STATEMENTS) {
      HashMap<String,Object> params = new HashMap<String,Object>();
      service.command(orientDBRequest, statement, params);
    }
  }
  
  public static final String[] UPDATE_STATEMENTS = new String[] {
      
    "UPDATE odmr_un\n"
    + "SET resolution =\n"
    + "  config\n"
    + "    .subString(config.indexOf('\"resolution\":') + 13)\n"
    + "    .subString(0,\n"
    + "      if(\n"
    + "        eval(\"config.subString(config.indexOf('\\\"resolution\\\":') + 13).indexOf(',') > -1\"),\n"
    + "        config.subString(config.indexOf('\"resolution\":') + 13).indexOf(','),\n"
    + "        config.subString(config.indexOf('\"resolution\":') + 13).indexOf('}')\n"
    + "      )\n"
    + "    )\n"
    + "    .replace('\"','')\n"
    + "    .trim()\n"
    + "    .asFloat()\n"
    + "WHERE config LIKE '%\"resolution\"%';",
    
    "UPDATE odmr_un\n"
    + "SET pcQuality =\n"
    + "  if(\n"
    + "    eval(\"config.subString(config.indexOf('\\\"pcQuality\\\":') + 12).indexOf(',') > -1\"),\n"
    + "    config.subString(config.indexOf('\"pcQuality\":') + 12)\n"
    + "          .subString(0, config.subString(config.indexOf('\"pcQuality\":') + 12).indexOf(','))\n"
    + "          .replace('\"','')\n"
    + "          .trim(),\n"
    + "    config.subString(config.indexOf('\"pcQuality\":') + 12)\n"
    + "          .subString(0, config.subString(config.indexOf('\"pcQuality\":') + 12).indexOf('}'))\n"
    + "          .replace('\"','')\n"
    + "          .trim()\n"
    + "  )\n"
    + "WHERE config LIKE '%\"pcQuality\"%';",
    
    "UPDATE odmr_un\n"
    + "SET featureQuality =\n"
    + "  if(\n"
    + "    eval(\"config.subString(config.indexOf('\\\"featureQuality\\\":') + 17).indexOf(',') > -1\"),\n"
    + "    config.subString(config.indexOf('\"featureQuality\":') + 17)\n"
    + "          .subString(0, config.subString(config.indexOf('\"featureQuality\":') + 17).indexOf(','))\n"
    + "          .replace('\"','')\n"
    + "          .trim(),\n"
    + "    config.subString(config.indexOf('\"featureQuality\":') + 17)\n"
    + "          .subString(0, config.subString(config.indexOf('\"featureQuality\":') + 17).indexOf('}'))\n"
    + "          .replace('\"','')\n"
    + "          .trim()\n"
    + "  )\n"
    + "WHERE config LIKE '%\"featureQuality\"%';",
    
    "UPDATE odmr_un\n"
    + "SET matcherNeighbors =\n"
    + "  config\n"
    + "    .subString(config.indexOf('\"matcherNeighbors\":') + 19)\n"
    + "    .subString(0,\n"
    + "      if(\n"
    + "        eval(\"config.subString(config.indexOf('\\\"matcherNeighbors\\\":') + 19).indexOf(',') > -1\"),\n"
    + "        config.subString(config.indexOf('\"matcherNeighbors\":') + 19).indexOf(','),\n"
    + "        config.subString(config.indexOf('\"matcherNeighbors\":') + 19).indexOf('}')\n"
    + "      )\n"
    + "    )\n"
    + "    .replace('\"','')\n"
    + "    .trim()\n"
    + "    .asLong()\n"
    + "WHERE config LIKE '%\"matcherNeighbors\"%';",
    
    "UPDATE odmr_un\n"
    + "SET minNumFeatures =\n"
    + "  config\n"
    + "    .subString(config.indexOf('\"minNumFeatures\":') + 17)\n"
    + "    .subString(0,\n"
    + "      if(\n"
    + "        eval(\"config.subString(config.indexOf('\\\"minNumFeatures\\\":') + 17).indexOf(',') > -1\"),\n"
    + "        config.subString(config.indexOf('\"minNumFeatures\":') + 17).indexOf(','),\n"
    + "        config.subString(config.indexOf('\"minNumFeatures\":') + 17).indexOf('}')\n"
    + "      )\n"
    + "    )\n"
    + "    .replace('\"','')\n"
    + "    .trim()\n"
    + "    .asLong()\n"
    + "WHERE config LIKE '%\"minNumFeatures\"%';",
    
    "UPDATE odmr_un\n"
    + "SET videoResolution =\n"
    + "  config\n"
    + "    .subString(config.indexOf('\"videoResolution\":') + 18)\n"
    + "    .subString(0,\n"
    + "      if(\n"
    + "        eval(\"config.subString(config.indexOf('\\\"videoResolution\\\":') + 18).indexOf(',') > -1\"),\n"
    + "        config.subString(config.indexOf('\"videoResolution\":') + 18).indexOf(','),\n"
    + "        config.subString(config.indexOf('\"videoResolution\":') + 18).indexOf('}')\n"
    + "      )\n"
    + "    )\n"
    + "    .replace('\"','')\n"
    + "    .trim()\n"
    + "    .asLong()\n"
    + "WHERE config LIKE '%\"videoResolution\"%';",
    
    "UPDATE odmr_un\n"
    + "SET videoLimit =\n"
    + "  config\n"
    + "    .subString(config.indexOf('\"videoLimit\":') + 13)\n"
    + "    .subString(0,\n"
    + "      if(\n"
    + "        eval(\"config.subString(config.indexOf('\\\"videoLimit\\\":') + 13).indexOf(',') > -1\"),\n"
    + "        config.subString(config.indexOf('\"videoLimit\":') + 13).indexOf(','),\n"
    + "        config.subString(config.indexOf('\"videoLimit\":') + 13).indexOf('}')\n"
    + "      )\n"
    + "    )\n"
    + "    .replace('\"','')\n"
    + "    .trim()\n"
    + "    .asLong()\n"
    + "WHERE config LIKE '%\"videoLimit\"%';",
    
    "UPDATE odmr_un\n"
    + "SET radiometricCalibration =\n"
    + "  if(\n"
    + "    eval(\"config.subString(config.indexOf('\\\"radiometricCalibration\\\":') + 25).indexOf(',') > -1\"),\n"
    + "    config.subString(config.indexOf('\"radiometricCalibration\":') + 25)\n"
    + "          .subString(0, config.subString(config.indexOf('\"radiometricCalibration\":') + 25).indexOf(','))\n"
    + "          .replace('\"','')\n"
    + "          .trim(),\n"
    + "    config.subString(config.indexOf('\"radiometricCalibration\":') + 25)\n"
    + "          .subString(0, config.subString(config.indexOf('\"radiometricCalibration\":') + 25).indexOf('}'))\n"
    + "          .replace('\"','')\n"
    + "          .trim()\n"
    + "  )\n"
    + "WHERE config LIKE '%\"radiometricCalibration\"%';"
    
  };
}
