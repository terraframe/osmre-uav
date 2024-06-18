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
package gov.geoplatform.uasdm.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.CollectionProductDTO;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.ODMRunView;
import gov.geoplatform.uasdm.view.ProductCriteria;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;

public class ProductService
{
  @Request(RequestType.SESSION)
  public void refreshAllDocuments(String sessionId) throws InterruptedException
  {
    Product.refreshAllDocuments();
  }

  /**
   * Downloads the product's ODM all.zip and refreshes S3 and database documents
   * with the data contained.
   * 
   * @param sessionId
   * @param productId
   * @throws InterruptedException
   */
  @Request(RequestType.SESSION)
  public void refreshDocuments(String sessionId, String productId) throws InterruptedException
  {
    final Product product = Product.get(productId);

    product.refreshDocuments();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    ComponentFacade.getProduct(oid).delete();
  }

  @Request(RequestType.SESSION)
  public JSONArray getProducts(String sessionId, ProductCriteria criteria)
  {
    JSONArray array = new JSONArray();

    List<CollectionProductDTO> dtos = null;

    if (criteria.getType().equals(ProductCriteria.SITE))
    {
      final UasComponentIF parent = ComponentFacade.getComponent(criteria.getId());
      dtos = parent.getDerivedProducts(criteria.getSortField(), criteria.getSortOrder());
    }
    else
    {
      dtos = ComponentFacade.getProducts(criteria);
    }

    for (CollectionProductDTO dto : dtos)
    {
      CollectionIF component = dto.getCollection();

      List<UasComponentIF> components = component.getAncestors();
      Collections.reverse(components);
      components.add(component);

      JSONArray products = dto.getProducts().stream().map(product -> {
        return Converter.toView(product, components).toJSON();
      }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

      JSONObject object = new JSONObject();
      object.put("componentId", component.getOid());
      object.put("products", products);

      array.put(object);
    }

    return array;
  }

  @Request(RequestType.SESSION)
  public ProductDetailView getProductDetail(String sessionId, String id, Integer pageNumber, Integer pageSize)
  {
    ProductIF product = ComponentFacade.getProduct(id);

    UasComponentIF component = product.getComponent();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);
    components.add(component);

    final List<DocumentIF> generated = product.getGeneratedFromDocuments();

    return Converter.toDetailView(product, components, generated, pageNumber, pageSize);
  }

  @Request(RequestType.SESSION)
  public ProductView togglePublish(String sessionId, String id)
  {
    ProductIF product = ComponentFacade.getProduct(id);
    product.togglePublished();

    UasComponentIF component = product.getComponent();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);
    components.add(component);

    return Converter.toView(product, components);
  }

  @Request(RequestType.SESSION)
  public void toggleLock(String sessionId, String id)
  {
    ProductIF product = ComponentFacade.getProduct(id);
    product.toggleLock();
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject downloadAllZip(String sessionId, String id)
  {
    final Product product = Product.get(id);

    return product.downloadAllZip();
  }

  @Request(RequestType.SESSION)
  public SiteObject getAllZip(String sessionId, String id)
  {
    final Product product = Product.get(id);

    return product.getAllZip();
  }

  public ODMRunView getODMRunByArtifact(String sessionId, String artifactId)
  {
    Document doc = Document.get(artifactId);

    ODMRun run = ODMRun.getGeneratingRun(doc);

    return ODMRunView.fromODMRun(run);
  }

  @Request(RequestType.SESSION)
  public ProductView create(String sessionId, String collectionId, String productName)
  {
    CollectionIF collection = ComponentFacade.getCollection(collectionId);

    ProductIF product = collection.createProductIfNotExist(productName);

    collection.setPrimaryProduct(product);

    List<UasComponentIF> components = collection.getAncestors();
    Collections.reverse(components);
    components.add(collection);

    JSONObject data = new JSONObject();
    data.put("collection", collection.getOid());

    NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.PRODUCT_GROUP_CHANGE, data));

    return Converter.toView(product, components);
  }

}
