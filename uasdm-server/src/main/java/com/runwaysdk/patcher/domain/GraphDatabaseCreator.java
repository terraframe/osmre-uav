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
