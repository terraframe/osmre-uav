package gov.geoplatform.uasdm.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.ComponentFactory;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;

public class ProductService
{
  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    ComponentFactory.getProduct(oid).delete();
  }

  @Request(RequestType.SESSION)
  public List<ProductView> getProducts(String sessionId, String oid)
  {
    List<ProductView> list = new LinkedList<ProductView>();

    final UasComponentIF parent = ComponentFactory.getComponent(oid);
    final List<ProductIF> products = parent.getDerivedProducts();

    for (ProductIF product : products)
    {
      UasComponentIF component = product.getComponent();

      List<UasComponentIF> components = component.getAncestors();
      Collections.reverse(components);
      components.add(component);

      list.add(Converter.toView(product, components));
    }

    return list;
  }

  @Request(RequestType.SESSION)
  public ProductDetailView getProductDetail(String sessionId, String id)
  {
    ProductIF product = ComponentFactory.getProduct(id);

    UasComponentIF component = product.getComponent();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);

    components.add(component);

    final List<DocumentIF> generated = product.getGeneratedFromDocuments();

    return Converter.toDetailView(product, components, generated);
  }
}
