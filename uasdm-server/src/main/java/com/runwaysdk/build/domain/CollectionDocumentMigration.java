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

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.CollectionQuery;
import gov.geoplatform.uasdm.bus.Document;
import gov.geoplatform.uasdm.bus.DocumentQuery;
import gov.geoplatform.uasdm.bus.Product;
import gov.geoplatform.uasdm.bus.ProductQuery;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.view.SiteObject;

public class CollectionDocumentMigration implements Runnable
{
  public static void start()
  {
    Thread t = new Thread(new CollectionDocumentMigration(), "CollectionMigrator");
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
    CollectionQuery cq = new CollectionQuery(new QueryFactory());

    try (OIterator<? extends Collection> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Collection col = it.next();

        if (col.getDocuments().size() == 0)
        {
          String[] folders = new String[] { Collection.RAW, Collection.PTCLOUD, Collection.DEM, Collection.ORTHO };

          List<DocumentIF> rDocuments = new LinkedList<DocumentIF>();
          List<DocumentIF> pDocuments = new LinkedList<DocumentIF>();

          for (String folder : folders)
          {
            List<SiteObject> objects = col.getSiteObjects(folder, null, null).getObjects();

            for (SiteObject object : objects)
            {
              Document document = Document.createIfNotExist(col, object.getKey(), object.getName());

              if (!folder.equals(Collection.RAW))
              {
                pDocuments.add(document);
              }
              else
              {
                rDocuments.add(document);
              }
            }
          }

          if (pDocuments.size() > 0)
          {
            Product product = Product.createIfNotExist(col);
            product.addDocuments(pDocuments);

            for (DocumentIF document : rDocuments)
            {
              document.addGeneratedProduct(product);
            }
          }
        }
      }
    }
  }

  @Transaction
  protected static void cleanupData()
  {
    ProductQuery pQuery = new ProductQuery(new QueryFactory());

    try (OIterator<? extends Product> it = pQuery.getIterator())
    {
      List<? extends Product> products = it.getAll();

      for (Product product : products)
      {
        product.delete(false);
      }
    }

    DocumentQuery dQuery = new DocumentQuery(new QueryFactory());

    try (OIterator<? extends Document> it = dQuery.getIterator())
    {
      List<? extends Document> products = it.getAll();

      for (Document product : products)
      {
        product.delete(false, false);
      }
    }

  }

  @Request
  public static void runCleanup()
  {
    // Remove existing data
    cleanupData();
  }

  public static void main(String[] args)
  {
    runCleanup();
  }
}
