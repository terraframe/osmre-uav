package com.runwaysdk.build.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.model.EdgeType;

public class DuplicateProductDocumentDeleter
{
  private static final Logger logger = LoggerFactory.getLogger(DuplicateProductDocumentDeleter.class);
  
  public static void main(String[] args)
  {
    doIt();
  }
  
  @Request()
  private static void doIt()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PRODUCT_HAS_DOCUMENT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdEdge.getDBClassName() + " \n");
    
    final GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString());
    
    List<EdgeObject> edges = query.getResults();
    
    List<EdgeObject> duplicates = new LinkedList<EdgeObject>();
    
    for (EdgeObject edge : edges)
    {
      int count = edges.stream().filter(searche -> searche.getParent().getOid() == edge.getParent().getOid() && searche.getChild().getOid() == edge.getChild().getOid()).collect(Collectors.toList()).size();
      
      if (count > 1 && !duplicates.stream().map(duplicate -> duplicate.getOid()).collect(Collectors.toList()).contains(edge.getOid()))
      {
        duplicates.add(edge);
      }
    }
    
    if (duplicates.size() > 0)
    {
      logger.error("Found " + duplicates.size() + " duplicate edges. They will be deleted.");
      
      for (EdgeObject duplicate: duplicates)
      {
        logger.error("Deleting duplicate edge between parent [" + duplicate.getParent().getObjectValue("name") + "] and child [" + duplicate.getChild().getObjectValue("name") + "].");
        
        duplicate.delete();
      }
    }
  }
}
