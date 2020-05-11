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
package com.runwaysdk.patcher.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBProperties;

public class GraphDatabaseCreator
{
  private static final Logger logger = LoggerFactory.getLogger(GraphDatabaseCreator.class);
  
  public static void main(String[] args)
  {
    initDatabase();
  }
  
  private static void initDatabase()
  {
    OrientDB orient = new OrientDB(OrientDBProperties.getUrl(), OrientDBProperties.getRootUserName(), OrientDBProperties.getRootUserPassword(), OrientDBConfig.defaultConfig());
    
    boolean exists = false;
    try
    {
      exists = orient.exists(OrientDBProperties.getDatabaseName());
    }
    finally
    {
      orient.close();
    }
    
    if (!exists)
    {
      logger.info("Initializing OrientDB database.");
      GraphDBService.getInstance().initializeDB();
    }
    else
    {
      logger.info("Skipping OrientDB init because it already exists.");
    }
  }
}
