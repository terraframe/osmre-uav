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

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;

public class SiteObjectDocumentQuery implements SiteObjectDocumentQueryIF
{
  private UasComponentIF component;

  private String         folder;

  private Long           skip;

  private Long           limit;

  public SiteObjectDocumentQuery(UasComponentIF component, String folder)
  {
    super();
    this.component = component;
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

  public GraphQuery<Document> getQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT FROM " + mdGraph.getDBClassName());
    ql.append(" WHERE " + mdAttribute.getColumnName() + " LIKE :s3location");

    if (this.folder.equals("image"))
    {
      ql.append(" AND ( ");
      ql.append(" " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :jpeg");
      ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :png");
      ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :tif");
      ql.append(" )");
    }
    else if (this.folder.equals("data"))
    {
      ql.append(" AND NOT ( ");
      ql.append(" " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :jpeg");
      ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :png");
      ql.append(" OR " + mdAttribute.getColumnName() + ".toUpperCase() LIKE :tif");
      ql.append(" )");
    }

    if (this.skip != null)
    {
      ql.append(" SKIP " + this.skip);
    }

    if (this.limit != null)
    {
      ql.append(" LIMIT " + this.limit);
    }

    String actualFolder = folder;

    if (folder.equals("image") || folder.equals("data"))
    {
      actualFolder = "raw";
    }

    final GraphQuery<Document> query = new GraphQuery<Document>(ql.toString());
    query.setParameter("s3location", component.getS3location() + actualFolder + "%");
    
    if (this.folder.equals("image") || this.folder.equals("data"))
    {
      query.setParameter("jpeg", "%.JPEG");
      query.setParameter("png", "%.PNG");
      query.setParameter("tif", "%.TIF");
    }

    return query;
  }

  public GraphQuery<Long> getCountQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);
    String key = component.getS3location() + folder + "%";

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT COUNT(*) FROM " + mdGraph.getDBClassName());
    ql.append(" WHERE " + mdAttribute.getColumnName() + " LIKE :s3location");

    final GraphQuery<Long> query = new GraphQuery<Long>(ql.toString());
    query.setParameter("s3location", key);

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
