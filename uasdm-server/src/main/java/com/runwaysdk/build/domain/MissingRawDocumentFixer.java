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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.view.SiteObject;

public class MissingRawDocumentFixer implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(MissingRawDocumentFixer.class);
  
  public static void main(String[] args)
  {
    new MissingRawDocumentFixer().run();
  }
  
  public static void start()
  {
    Thread t = new Thread(new MissingRawDocumentFixer(), "CollectionMigrator");
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
    List<Collection> collections = this.getCorruptCollections();
    
    logger.error("Patcher will fix [" + collections.size() + "] corrupted collections, which have a product but no raw images.");
    
    for (Collection col : collections)
    {
      List<String> createdDocuments = new ArrayList<String>();
      
      String[] folders = new String[] { Collection.RAW };

      List<DocumentIF> rDocuments = new LinkedList<DocumentIF>();

      for (String folder : folders)
      {
        List<SiteObject> objects = RemoteFileFacade.getSiteObjects(col, folder, new LinkedList<SiteObject>(), null, null).getObjects();
        
        for (SiteObject object : objects)
        {
          // This should also create a component_has_document relationship between the collection and the document.
          DocumentIF document = col.createDocumentIfNotExist(object.getKey(), object.getName(), null, null, null, null, null);

          rDocuments.add(document);
          
          createdDocuments.add(object.getName());
        }
      }
      
      logger.error("Created for Collection [" + col.getName() + "] documents [" + StringUtils.join(createdDocuments, ", ") + "].");

      // Assume that all RAW documents were used to create the product
      Product product = Product.find(col);
      if (product != null)
      {
        List<String> addedDocuments = new ArrayList<String>();
        
        for (DocumentIF document : rDocuments)
        {
          document.addGeneratedProduct(product);
          addedDocuments.add(product.getName());
        }
        
        logger.error("Added to product [" + product.getName() + "] documents [" + StringUtils.join(addedDocuments, ", ") + "]");
      }
    }
  }
  
  private List<Collection> getCorruptCollections()
  {
    final String component_has_document = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT).getDBClassName();
    final String component_has_product = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT).getDBClassName();
    final String collection0 = MdVertexDAO.getMdVertexDAO(Collection.CLASS).getDBClassName();
    final String document0 = MdVertexDAO.getMdVertexDAO(Document.CLASS).getDBClassName();
    final String product0 = MdVertexDAO.getMdVertexDAO(Product.CLASS).getDBClassName();
    final String oid = MdVertexInfo.OID;
    final String s3location = Collection.getS3locationMd().getColumnName();
    final String raw = Collection.RAW;

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT * FROM " + collection0 + " WHERE " + oid + " NOT IN (");
    builder.append("SELECT " + oid + " FROM (SELECT expand(in('" + component_has_document + "')) FROM (SELECT FROM " + document0 + " WHERE " + s3location + " CONTAINSTEXT '/" + raw + "/'))");
    builder.append(") AND " + oid + " IN (");
    builder.append("SELECT " + oid + " FROM (SELECT expand(in('" + component_has_product + "')) FROM " + product0 + "))");
    
    final GraphQuery<Collection> query = new GraphQuery<Collection>(builder.toString());

    return query.getResults();
  }
}
