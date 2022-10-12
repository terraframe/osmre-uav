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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;

public class IndexServiceMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(IndexServiceMigration.class);

  public static void main(String[] args)
  {
    if (args != null && args.length > 0)
    {
      new IndexServiceMigration(Boolean.parseBoolean(args[0])).run();
    }
    else
    {

      new IndexServiceMigration().run();
    }
  }

  public static void start()
  {
    Thread t = new Thread(new IndexServiceMigration(), "IndexServiceMigration");
    t.setDaemon(true);
    t.start();
  }

  private boolean standalone;

  public IndexServiceMigration()
  {
    this.standalone = true;
  }

  public IndexServiceMigration(boolean standalone)
  {
    this.standalone = standalone;
  }

  @Request
  public void run()
  {
    transaction();
  }

  @Transaction
  protected void transaction()
  {
    IndexService.clear();

    this.migrateComponents();
    this.migrateProducts();

    if (standalone)
    {
      IndexService.shutdown();
    }
  }

  private void migrateComponents()
  {
    List<UasComponent> components = this.getComponents();

    logger.info("Patcher will update [" + components.size() + "] collections");

    for (UasComponent component : components)
    {
      List<UasComponentIF> ancestors = component.getAncestors();
      IndexService.createDocument(ancestors, component);

      List<DocumentIF> documents = component.getDocuments();

      for (DocumentIF document : documents)
      {
        String key = document.getS3location();

        logger.info("Indexing file [" + key + "]");

        String documentName = key.substring(key.lastIndexOf("/") + 1);

        IndexService.updateOrCreateDocument(ancestors, component, key, documentName);
      }
    }
  }

  private void migrateProducts()
  {
    List<Product> products = this.getProducts();

    logger.info("Patcher will index [" + products.size() + "] products");

    for (Product product : products)
    {
      IndexService.createStacItems(product);
    }
  }

  private List<UasComponent> getComponents()
  {
    MdVertexDAOIF collection = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);
    final String component0 = collection.getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + component0);
    // builder.append(" WHERE " + metadataUploaded0 + " = :metadataUploaded");

    final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(builder.toString());
    // query.setParameter("metadataUploaded", false);

    return query.getResults();
  }

  private List<Product> getProducts()
  {
    MdVertexDAOIF collection = MdVertexDAO.getMdVertexDAO(Product.CLASS);
    final String component0 = collection.getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + component0);
    // builder.append(" WHERE " + metadataUploaded0 + " = :metadataUploaded");

    final GraphQuery<Product> query = new GraphQuery<Product>(builder.toString());
    // query.setParameter("metadataUploaded", false);

    return query.getResults();
  }
}
