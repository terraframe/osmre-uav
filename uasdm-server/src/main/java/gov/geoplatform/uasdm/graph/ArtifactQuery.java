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
package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;

public class ArtifactQuery implements SiteObjectDocumentQueryIF
{
  private UasComponentIF component;

  private ProductIF      product;

  private Long           skip;

  private Long           limit;

  public ArtifactQuery(UasComponentIF component, ProductIF product)
  {
    super();
    this.component = component;
    this.product = product;
  }

  public ArtifactQuery(UasComponentIF component, Optional<ProductIF> product)
  {
    super();
    this.component = component;
    this.product = product.orElse(null);
  }

  public Long getLimit()
  {
    return limit;
  }

  public void setLimit(Long limit)
  {
    this.limit = limit;
  }

  public Long getSkip()
  {
    return skip;
  }

  public void setSkip(Long skip)
  {
    this.skip = skip;
  }

  public GraphQuery<Document> getQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT FROM " + mdGraph.getDBClassName());
    ql.append(" WHERE " + mdAttribute.getColumnName() + " LIKE :dem");
    ql.append(" OR " + mdAttribute.getColumnName() + " LIKE :ortho");
    ql.append(" OR " + mdAttribute.getColumnName() + " LIKE :ptcloud");

    if (this.skip != null)
    {
      ql.append(" SKIP " + this.skip);
    }

    if (this.limit != null)
    {
      ql.append(" LIMIT " + this.limit);
    }

    final GraphQuery<Document> query = new GraphQuery<Document>(ql.toString());
    query.setParameter("dem", component.getS3location(product, ImageryComponent.DEM) + "/" + "%");
    query.setParameter("ortho", component.getS3location(product, ImageryComponent.ORTHO)  + "/"+ "%");
    query.setParameter("ptcloud", component.getS3location(product, ImageryComponent.PTCLOUD) + "/" + "%");

    return query;
  }

  @Override
  public Long getCount()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT COUNT(*) FROM " + mdGraph.getDBClassName());
    ql.append(" WHERE " + mdAttribute.getColumnName() + " LIKE :dem");
    ql.append(" OR " + mdAttribute.getColumnName() + " LIKE :ortho");
    ql.append(" OR " + mdAttribute.getColumnName() + " LIKE :ptcloud");

    final GraphQuery<Long> query = new GraphQuery<Long>(ql.toString());
    query.setParameter("dem", component.getS3location(product, ImageryComponent.DEM) + "/" + "%");
    query.setParameter("ortho", component.getS3location(product, ImageryComponent.ORTHO) + "/" + "%");
    query.setParameter("ptcloud", component.getS3location(product, ImageryComponent.PTCLOUD) + "/" + "%");

    return query.getSingleResult();
  }

  public List<SiteObject> getSiteObjects()
  {
    List<Document> documents = this.getDocuments();

    LinkedList<SiteObject> objects = new LinkedList<SiteObject>();

    for (Document document : documents)
    {
      objects.add(SiteObject.create(component, document));
    }

    return objects;
  }

  public List<Document> getDocuments()
  {
    if (this.product != null)
    {
      return this.getQuery().getResults();
    }

    return new LinkedList<>();
  }
}
