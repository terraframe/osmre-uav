package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class Product extends ProductBase
{
  private static final long serialVersionUID = 1797567850;

  public Product()
  {
    super();
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    // Delete all of the documents
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

  public void addDocuments(List<Document> documents)
  {
    for (Document document : documents)
    {
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

  public static Product createIfNotExist(UasComponent uasComponent)
  {
    Product product = find(uasComponent);

    if (product == null)
    {
      product = new Product();
      product.setComponent(uasComponent);
      product.setName(uasComponent.getName());
      product.apply();
    }

    return product;
  }

  public static Product find(UasComponent uasComponent)
  {
    ProductQuery query = new ProductQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(uasComponent));

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
}