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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.ProductDetailView;
import gov.geoplatform.uasdm.view.ProductView;
import gov.geoplatform.uasdm.view.SiteObject;

public class ProductService
{
  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    ComponentFacade.getProduct(oid).delete();
  }

  @Request(RequestType.SESSION)
  public List<ProductView> getProducts(String sessionId, String oid)
  {
    List<ProductView> list = new LinkedList<ProductView>();

    final UasComponentIF parent = ComponentFacade.getComponent(oid);
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
  public RemoteFileObject getAllZip(String sessionId, String id)
  {
    final ProductIF product = ComponentFacade.getProduct(id);
    final UasComponentIF component = product.getComponent();

    // This commented out code was for fetching the all zips from the ProductHasDocument relationship. Unfortunately
    // we can't do this because Documents don't have a lastUpdateDate field so we wouldn't know how to order them.
    // I'm leaving this code here because it's technically a "better" solution if we ever have lastUpdate on the graph.
//    List<DocumentIF> docs = product.getDocuments();
//    
//    Iterator<DocumentIF> it = docs.iterator();
//    while (it.hasNext())
//    {
//      DocumentIF doc = it.next();
//      
//      if (! (doc.getS3location().contains("/" + Product.ODM_ALL_DIR + "/") && doc.getS3location().endsWith(".zip")))
//      {
//        it.remove();
//      }
//    }
//    
//    DocumentIF lastDoc = null;
//    for (DocumentIF doc : docs)
//    {
//      if (lastDoc == null || doc.getLastModified().after(lastDoc.getLastModified()))
//      {
//        lastDoc = doc;
//      }
//    }
//    
//    if (lastDoc != null)
//    {
//      return component.download(lastDoc.getS3location());
//    }
//    else
//    {
      List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, Product.ODM_ALL_DIR, new LinkedList<SiteObject>(), null, null).getObjects();
  
      SiteObject last = null;
  
      for (SiteObject item : items)
      {
        if (last == null || item.getLastModified().after(last.getLastModified()))
        {
          last = item;
        }
      }
  
      if (last != null)
      {
        return component.download(last.getKey());
      }
      else
      {
        throw new ProgrammingErrorException("No files exist");
      }
    }
//  }

}
