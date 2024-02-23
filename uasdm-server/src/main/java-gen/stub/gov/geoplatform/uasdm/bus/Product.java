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
package gov.geoplatform.uasdm.bus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Product extends ProductBase implements ProductIF
{
  private static final long   serialVersionUID = 1797567850;

  private static final Logger logger           = LoggerFactory.getLogger(Product.class);

  private String              imageKey         = null;

  private String              mapKey           = null;

  public Product()
  {
    super();
  }

  public String getImageKey()
  {
    return this.imageKey;
  }

  public String getMapKey()
  {
    return this.mapKey;
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    List<Document> documents = new LinkedList<Document>();

    try (OIterator<? extends Document> it = this.getAllDocuments())
    {
      documents.addAll(it.getAll());
    }

    for (Document document : documents)
    {
      document.delete(removeFromS3);
    }

    super.delete();
  }

  public void addDocuments(List<DocumentIF> documents)
  {
    for (DocumentIF doc : documents)
    {
      Document document = (Document) doc;

      ProductHasDocument pd = getProductHasDocument(document);

      if (pd == null)
      {
        this.addDocuments(document).apply();
      }
    }
  }

  public ProductHasDocument getProductHasDocument(Document document)
  {
    ProductHasDocumentQuery query = new ProductHasDocumentQuery(new QueryFactory());
    query.WHERE(query.getParent().EQ(this));
    query.AND(query.getChild().EQ(document));

    try (OIterator<? extends ProductHasDocument> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  @Override
  public List<DocumentIF> getGeneratedFromDocuments()
  {
    List<DocumentIF> generated = new LinkedList<DocumentIF>();

    try (OIterator<? extends Document> it = this.getAllGeneratedDocuments())
    {
      generated.addAll(it.getAll());
    }

    return generated;
  }

  @Override
  public Page<DocumentIF> getGeneratedFromDocuments(Integer pageNumber, Integer pageSize)
  {
    final List<DocumentIF> documents = this.getGeneratedFromDocuments();

    return new Page<DocumentIF>(1, 1, 1, documents);
  }

  public static Product createIfNotExist(UasComponentIF uasComponent)
  {
    Product product = find(uasComponent);

    if (product == null)
    {
      product = new Product();
      product.setComponent((UasComponent) uasComponent);
      product.setName(uasComponent.getName());
      product.setPublished(false);
      product.apply();
    }

    return product;
  }

  public static Product find(UasComponentIF uasComponent)
  {
    ProductQuery query = new ProductQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ((UasComponent) uasComponent));

    try (OIterator<? extends Product> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  @Transaction
  public void clear()
  {
    try (OIterator<? extends DocumentGeneratedProduct> relationships = this.getAllGeneratedDocumentsRel())
    {
      List<? extends DocumentGeneratedProduct> list = relationships.getAll();

      for (DocumentGeneratedProduct relationship : list)
      {
        relationship.delete();
      }
    }
  }

  public void updateBoundingBox(boolean newProduct)
  {
    UasComponent component = this.getComponent();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);

    components.add(component);

    if (this.getImageKey() == null || this.getMapKey() == null)
    {
//      this.calculateKeys(components);
    }

    if (this.getMapKey() != null && this.getMapKey().length() > 0)
    {
//      String bbox = this.calculateBoundingBox(this.getMapKey());
//
//      if (bbox != null)
//      {
//        this.lock();
//        this.setBoundingBox(bbox);
//        this.apply();
//      }
    }
  }

  @Override
  public boolean isPublished()
  {
    return this.getPublished() != null && this.getPublished();
  }

  @Override
  @Transaction
  public void togglePublished()
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public StacItem toStacItem()
  {
    return null;
  }

  public static List<ProductIF> getProduct()
  {
    ProductQuery query = new ProductQuery(new QueryFactory());

    try (OIterator<? extends Product> it = query.getIterator())
    {
      return new LinkedList<ProductIF>(it.getAll());
    }
  }

  @Override
  public void calculateKeys(List<UasComponentIF> components)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DocumentIF> getDocuments()
  {
    throw new UnsupportedOperationException();
  }
}
