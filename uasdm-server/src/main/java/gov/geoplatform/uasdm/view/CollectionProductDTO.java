package gov.geoplatform.uasdm.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.runwaysdk.business.graph.VertexObject;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ProductIF;

public class CollectionProductDTO
{
  private CollectionIF          collection;

  private ProductIF             primary;

  private Collection<ProductIF> products;

  public CollectionProductDTO(CollectionIF collection)
  {
    this.collection = collection;
    this.products = new TreeSet<ProductIF>((ProductIF p1, ProductIF p2) -> {
      if (p1.isPrimary() == p2.isPrimary())
      {
        return p1.getProductName().compareTo(p2.getProductName());
      }

      return Boolean.compare(p2.isPrimary(), p1.isPrimary());
    });
  }

  public CollectionIF getCollection()
  {
    return collection;
  }

  public void addProduct(ProductIF product)
  {
    if (product.isPrimary())
    {
      this.primary = product;
    }

    this.products.add(product);
  }

  public ProductIF getPrimary()
  {
    return primary;
  }

  public Collection<ProductIF> getProducts()
  {
    return products;
  }

  public boolean isEmpty()
  {
    return this.products.isEmpty();
  }

  public static List<CollectionProductDTO> process(List<VertexObject> results)
  {
    List<CollectionProductDTO> dtos = new LinkedList<CollectionProductDTO>();

    CollectionProductDTO current = null;

    for (VertexObject result : results)
    {
      if (result instanceof CollectionIF)
      {
        current = new CollectionProductDTO((CollectionIF) result);

        dtos.add(current);
      }
      else if (current != null)
      {
        current.addProduct((ProductIF) result);
      }
    }

    return dtos.stream().filter(v -> !v.isEmpty()).collect(Collectors.toList());
  }
}
