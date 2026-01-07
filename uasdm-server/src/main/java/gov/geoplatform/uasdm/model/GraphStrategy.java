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
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Imagery;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.RawSet;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.view.ComponentProductDTO;
import gov.geoplatform.uasdm.view.CollectionCriteria;

public class GraphStrategy implements ComponentStrategy
{

  public UasComponentIF getComponent(String oid)
  {
    return UasComponent.getWithAccessControl(oid).orElseThrow(() -> {
      GenericException ex = new GenericException();
      ex.setUserMessage("Unable to find a component an id of [" + oid + "]");
      throw ex;
    });
  }

  public SiteIF getSite(String oid)
  {
    return (SiteIF) this.getComponent(oid);
  }

  public ProjectIF getProject(String oid)
  {
    return (ProjectIF) this.getComponent(oid);
  }

  public MissionIF getMission(String oid)
  {
    return (MissionIF) this.getComponent(oid);
  }

  public CollectionIF getCollection(String oid)
  {
    return (CollectionIF) this.getComponent(oid);
  }

  public ImageryIF getImagery(String oid)
  {
    return Imagery.get(oid);
  }

  public ProductIF getProduct(String oid)
  {
    return Product.get(oid);
  }

  public DocumentIF getDocument(String oid)
  {
    return Document.get(oid);
  }

  public List<SiteIF> getSites(String conditions, String sort)
  {
    return Site.getSites(conditions, sort);
  }

  @Override
  public List<ProductIF> getProducts()
  {
    return Product.getProducts();
  }

  @Override
  public List<ComponentProductDTO> getProducts(CollectionCriteria criteria)
  {
    return Product.getProducts(criteria);
  }

  @Override
  public Page<MetadataMessage> getMissingMetadata(Integer pageNumber, Integer pageSize)
  {
    java.util.Collection<CollectionIF> collections = Collection.getMissingMetadata(pageNumber, pageSize);

    long count = this.getMissingMetadataCount();

    List<MetadataMessage> results = new LinkedList<MetadataMessage>();

    for (CollectionIF collection : collections)
    {
      results.add(new MetadataMessage(collection));
    }

    return new Page<MetadataMessage>(count, pageNumber, pageSize, results);
  }

  @Override
  public long getMissingMetadataCount()
  {
    return Collection.getMissingMetadataCount();
  }

  @Override
  public UasComponentIF newRoot()
  {
    return new Site();
  }

  @Override
  public JSONObject features(String conditions) throws IOException
  {
    return Site.features(conditions);
  }

  @Override
  public JSONArray bbox()
  {
    return UasComponent.bbox();
  }
  
  @Override
  public RawSet getRawSet(String oid)
  {
    return RawSet.get(oid);
  }

  @Override
  public List<ComponentRawSet> getRawSets(CollectionCriteria criteria)
  {
    return RawSet.getAll(criteria);
  }
}
