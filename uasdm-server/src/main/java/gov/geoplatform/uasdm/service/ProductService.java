package gov.geoplatform.uasdm.service;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.Document;
import gov.geoplatform.uasdm.bus.Product;
import gov.geoplatform.uasdm.bus.ProductQuery;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;

public class ProductService
{
  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    Product.get(oid).delete();
  }

  @Request(RequestType.SESSION)
  public List<ProductView> getProducts(String sessionId, String oid)
  {
    List<ProductView> list = new LinkedList<ProductView>();

    UasComponent parent = UasComponent.get(oid);

    ProductQuery query = new ProductQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getName());

    try (OIterator<? extends Product> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        Product product = iterator.next();
        UasComponent component = product.getComponent();

        List<UasComponent> components = component.getAncestors();
        components.add(component);

        boolean valid = false;

        for (UasComponent com : components)
        {
          if (com.getOid().equals(parent.getOid()))
          {
            valid = true;
          }
        }

        if (valid)
        {
          list.add(Converter.toView(product, components));
        }
      }
    }

    return list;
  }

  @Request(RequestType.SESSION)
  public ProductDetailView getProductDetail(String sessionId, String id)
  {
    Product product = Product.get(id);

    UasComponent component = product.getComponent();

    List<UasComponent> components = component.getAncestors();
    components.add(component);

    List<Document> generated = new LinkedList<Document>();

    try (OIterator<? extends Document> it = product.getAllGeneratedDocuments())
    {
      generated.addAll(it.getAll());
    }

    return Converter.toDetailView(product, components, generated);
  }
}
