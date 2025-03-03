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
package gov.geoplatform.uasdm.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.runwaysdk.business.graph.VertexObject;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class ComponentProductDTO
{
  private UasComponentIF component;

  private ProductIF             primary;

  private Collection<ProductIF> products;

  public ComponentProductDTO(UasComponentIF component)
  {
    this.component = component;
    this.products = new TreeSet<ProductIF>((ProductIF p1, ProductIF p2) -> {
      if (p1.isPrimary() == p2.isPrimary())
      {
         return (p1.getProductName() == null ? "" : p1.getProductName()).compareTo(p2.getProductName() == null ? "" : p2.getProductName());
      }

      return Boolean.compare(Boolean.TRUE.equals(p2.isPrimary()), Boolean.TRUE.equals(p1.isPrimary()));
    });
  }

  public UasComponentIF getComponent()
  {
    return component;
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
  
  public ProductIF getPrimaryOrAny()
  {
    return primary != null ? primary : products.stream().findAny().get();
  }

  public Collection<ProductIF> getProducts()
  {
    return products;
  }

  public boolean isEmpty()
  {
    return this.products.isEmpty();
  }

  public static List<ComponentProductDTO> process(List<VertexObject> results)
  {
    List<ComponentProductDTO> dtos = new LinkedList<ComponentProductDTO>();

    ComponentProductDTO current = null;

    for (VertexObject result : results)
    {
      if (result instanceof UasComponentIF)
      {
        current = new ComponentProductDTO((UasComponentIF) result);

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
