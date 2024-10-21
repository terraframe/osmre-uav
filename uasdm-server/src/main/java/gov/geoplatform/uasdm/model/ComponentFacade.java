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
package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.view.ComponentProductDTO;
import gov.geoplatform.uasdm.view.ProductCriteria;

public class ComponentFacade
{
  private static ComponentStrategy STRATEGY = new GraphStrategy();

  public static UasComponentIF getComponent(String oid)
  {
    return STRATEGY.getComponent(oid);
  }

  public static SiteIF getSite(String oid)
  {
    return STRATEGY.getSite(oid);
  }

  public static ProjectIF getProject(String oid)
  {
    return STRATEGY.getProject(oid);
  }

  public static MissionIF getMission(String oid)
  {
    return STRATEGY.getMission(oid);
  }

  public static CollectionIF getCollection(String oid)
  {
    return STRATEGY.getCollection(oid);
  }

  public static ImageryIF getImagery(String oid)
  {
    return STRATEGY.getImagery(oid);
  }

  public static ProductIF getProduct(String oid)
  {
    return STRATEGY.getProduct(oid);
  }

  public static List<ProductIF> getProducts()
  {
    return STRATEGY.getProducts();
  }

  public static DocumentIF getDocument(String oid)
  {
    return STRATEGY.getDocument(oid);
  }

  public static List<SiteIF> getSites(String conditions, String sort)
  {
    return STRATEGY.getSites(conditions, sort);
  }

  public static long getMissingMetadataCount()
  {
    return STRATEGY.getMissingMetadataCount();
  }

  public static Page<MetadataMessage> getMissingMetadata(Integer pageNumber, Integer pageSize)
  {
    return STRATEGY.getMissingMetadata(pageNumber, pageSize);
  }

  public static UasComponentIF newRoot()
  {
    return STRATEGY.newRoot();
  }

  public static JSONObject features(String conditions) throws IOException
  {
    return STRATEGY.features(conditions);
  }

  public static JSONArray bbox()
  {
    return STRATEGY.bbox();
  }

  public static List<ComponentProductDTO> getProducts(ProductCriteria criteria)
  {
    return STRATEGY.getProducts(criteria);
  }

}
