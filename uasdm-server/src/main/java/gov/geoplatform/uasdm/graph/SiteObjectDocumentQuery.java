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
package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;

public class SiteObjectDocumentQuery implements SiteObjectDocumentQueryIF
{
  private UasComponentIF component;

  private ProductIF      product;

  private String         folder;

  private Long           skip;

  private Long           limit;

  public SiteObjectDocumentQuery(UasComponentIF component, ProductIF product, String folder)
  {
    super();
    this.component = component;
    this.product = product;
    this.folder = folder;
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

  private void addWhereStatements(MdAttributeDAOIF mdAttribute, StringBuilder ql)
  {
    ql.append(" WHERE " + mdAttribute.getColumnName() + " LIKE :s3location");

    if (this.component instanceof CollectionIF)
    {
      CollectionIF collection = (CollectionIF) this.component;

      if (this.folder.equals("image") || this.folder.equals("data"))
      {
        if (this.folder.equals("image"))
        {
          ql.append(" AND ( ");
        }
        else if (this.folder.equals("data"))
        {
          ql.append(" AND NOT ( ");
        }

        if (!collection.isLidar())
        {
          ql.append(" " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :jpeg");
          ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :jpg");
          ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :png");
          ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :tif");
          ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :tiff");
        }
        else
        {
          ql.append(" " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :laz");
          ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :las");
        }

        ql.append(" )");
      }

    }
  }

  private void addParameters(final GraphQuery<?> query)
  {
    String actualFolder = folder;

    if (folder.equals("image") || folder.equals("data"))
    {
      actualFolder = "raw";
    }

    query.setParameter("s3location", component.getS3location(this.product, actualFolder) + "%");

    if (this.component instanceof CollectionIF)
    {
      CollectionIF collection = (CollectionIF) this.component;

      if (this.folder.equals("image") || this.folder.equals("data"))
      {
        if (!collection.isLidar())
        {
          query.setParameter("jpeg", "%.JPEG");
          query.setParameter("jpg", "%.JPG");
          query.setParameter("png", "%.PNG");
          query.setParameter("tif", "%.TIF");
          query.setParameter("tiff", "%.TIFF");
        }
        else
        {
          query.setParameter("las", "%.LAS");
          query.setParameter("laz", "%.LAZ");
        }
      }
    }
  }

  public GraphQuery<Document> getQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);
    MdAttributeDAOIF name = mdGraph.definesAttribute(Document.NAME);

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT FROM " + mdGraph.getDBClassName());

    addWhereStatements(mdAttribute, ql);

    ql.append(" ORDER BY " + name.getColumnName() + " ASC");

    if (this.skip != null)
    {
      ql.append(" SKIP " + this.skip);
    }

    if (this.limit != null)
    {
      ql.append(" LIMIT " + this.limit);
    }

    final GraphQuery<Document> query = new GraphQuery<Document>(ql.toString());

    this.addParameters(query);

    return query;
  }

  public GraphQuery<Long> getCountQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT COUNT(*) FROM " + mdGraph.getDBClassName());

    this.addWhereStatements(mdAttribute, ql);

    final GraphQuery<Long> query = new GraphQuery<Long>(ql.toString());

    this.addParameters(query);

    return query;
  }

  public Long getCount()
  {
    return this.getCountQuery().getSingleResult();
  }

  public List<SiteObject> getSiteObjects()
  {
    List<Document> documents = getDocuments();

    LinkedList<SiteObject> objects = new LinkedList<SiteObject>();

    for (Document document : documents)
    {
      objects.add(SiteObject.create(component, document));
    }

    return objects;
  }

  public List<Document> getDocuments()
  {
    return this.getQuery().getResults();
  }
}
