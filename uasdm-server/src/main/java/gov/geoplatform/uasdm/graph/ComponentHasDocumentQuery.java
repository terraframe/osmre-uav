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
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.view.SiteObject;

/**
 * Queries raw collection input documents by traversing from the collection
 * vertex to its document vertices first, then applying raw-document filtering.
 *
 * This avoids scanning the entire document table by s3location.
 * 
 * This query is to be preferred over it's predecessor, SiteObjectDocumentQuery, because this query is way faster.
 * 
 * @author rrowlands
 */
public class ComponentHasDocumentQuery implements SiteObjectDocumentQueryIF
{
  private final UasComponent component;
  
  private ProductIF product;
  
  private String folder;

  private final MdEdgeDAOIF   mdEdge;

  private Long                skip;

  private Long                limit;

  public ComponentHasDocumentQuery(UasComponent component, ProductIF product, String folder)
  {
    super();

    this.component = component;
    this.product = product;
    this.folder = folder;
    this.mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
  }
  
  public ComponentHasDocumentQuery(UasComponent component, String folder)
  {
    super();

    this.component = component;
    this.folder = folder;
    this.mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
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

  private void addWhereStatements(MdAttributeDAOIF s3location, StringBuilder ql)
  {
    ql.append(" WHERE " + s3location.getColumnName() + " LIKE :s3location");
    
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
          ql.append(" " + s3location.getColumnName() + ".toUpperCase() LIKE :jpeg");
          ql.append(" OR " + s3location.getColumnName() + ".toUpperCase() LIKE :jpg");
          ql.append(" OR " + s3location.getColumnName() + ".toUpperCase() LIKE :png");
          ql.append(" OR " + s3location.getColumnName() + ".toUpperCase() LIKE :tif");
          ql.append(" OR " + s3location.getColumnName() + ".toUpperCase() LIKE :tiff");
        }
        else
        {
          ql.append(" " + s3location.getColumnName() + ".toUpperCase() LIKE :laz");
          ql.append(" OR " + s3location.getColumnName() + ".toUpperCase() LIKE :las");
        }

        ql.append(" )");
      }
      else if (this.folder.equals("raw"))
      {
        ql.append(" AND NOT (" + s3location.getColumnName() + " LIKE :xml)");
        ql.append(" AND NOT (" + s3location.getColumnName() + " LIKE :txt)");
        ql.append(" AND NOT (" + s3location.getColumnName() + " LIKE :csv)");
      }

    }
  }

  private void addParameters(final GraphQuery<?> query)
  {
    query.setParameter("rid", this.component.getRID());
    
    String actualFolder = folder;

    if (folder.equals("image") || folder.equals("data"))
    {
      actualFolder = "raw";
    }

    String s3Loc = component.getS3location(this.product, actualFolder);
    if (actualFolder.startsWith("dem/") && s3Loc.endsWith("/"))
      s3Loc = s3Loc.substring(0, s3Loc.length()-1);
    
    query.setParameter("s3location", s3Loc + "%");

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
      else if (this.folder.equals("raw"))
      {
        query.setParameter("xml", "%.xml");
        query.setParameter("txt", "%.txt");
        query.setParameter("csv", "%.csv");
      }
    }
  }

  private void addCollectionDocumentTraversal(StringBuilder ql)
  {
    ql.append(" FROM (");
    ql.append("   SELECT EXPAND(OUT('" + this.mdEdge.getDBClassName() + "'))");
    ql.append("   FROM :rid");
    ql.append(" )");
  }

  public GraphQuery<Document> getQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);

    MdAttributeDAOIF s3location = mdGraph.definesAttribute(Document.S3LOCATION);
    MdAttributeDAOIF name = mdGraph.definesAttribute(Document.NAME);

    StringBuilder ql = new StringBuilder();

    ql.append("SELECT");
    addCollectionDocumentTraversal(ql);

    addWhereStatements(s3location, ql);

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

    MdAttributeDAOIF s3location = mdGraph.definesAttribute(Document.S3LOCATION);

    StringBuilder ql = new StringBuilder();

    ql.append("SELECT COUNT(*)");
    addCollectionDocumentTraversal(ql);

    addWhereStatements(s3location, ql);

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
    List<Document> documents = this.getDocuments();

    LinkedList<SiteObject> objects = new LinkedList<SiteObject>();

    for (Document document : documents)
    {
      objects.add(SiteObject.create(this.component, document));
    }

    return objects;
  }

  public List<Document> getDocuments()
  {
    return this.getQuery().getResults();
  }
}