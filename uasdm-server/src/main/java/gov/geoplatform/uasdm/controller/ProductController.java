package gov.geoplatform.uasdm.controller;

import java.io.IOException;
import java.util.List;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import gov.geoplatform.uasdm.service.ProductService;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;

@Controller(url = "product")
public class ProductController
{
  private ProductService service;

  public ProductController()
  {
    this.service = new ProductService();
  }

  @Endpoint(url = "get-all", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getAll(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    List<ProductView> products = service.getProducts(request.getSessionId(), id);

    return new RestBodyResponse(ProductView.serialize(products));
  }

  @Endpoint(url = "detail", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF detail(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize) throws IOException
  {
    pageNumber = pageNumber != null ? pageNumber : 1;
    pageSize = pageSize != null ? pageSize : 20;

    ProductDetailView detail = service.getProductDetail(request.getSessionId(), id, pageNumber, pageSize);

    return new RestBodyResponse(detail.toJSON());
  }

  @Endpoint(url = "toggle-publish", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF togglePublish(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    ProductView view = service.togglePublish(request.getSessionId(), id);

    return new RestBodyResponse(view.toJSON());
  }

  @Endpoint(url = "remove", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "id") String id) throws IOException
  {
    this.service.remove(request.getSessionId(), id);

    return new RestResponse();
  }
}
