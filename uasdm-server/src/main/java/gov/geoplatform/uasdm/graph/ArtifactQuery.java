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

import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;

public class ArtifactQuery
{
  private UasComponentIF component;

  public ArtifactQuery(UasComponentIF component)
  {
    super();
    this.component = component;
  }

  public GraphQuery<Document> getQuery()
  {
    final MdVertexDAOIF mdGraph = MdVertexDAO.getMdVertexDAO(Document.CLASS);
    MdAttributeDAOIF mdAttribute = mdGraph.definesAttribute(Document.S3LOCATION);

    StringBuilder ql = new StringBuilder();
    ql.append("SELECT FROM " + mdGraph.getDBClassName());
    ql.append(" WHERE " + mdAttribute.getColumnName() + " LIKE :s3location");
    ql.append(" AND NOT (" + mdAttribute.getColumnName() + " LIKE :raw )");

    final GraphQuery<Document> query = new GraphQuery<Document>(ql.toString());
    query.setParameter("s3location", component.getS3location() + "%");
    query.setParameter("raw", component.getS3location() + ImageryComponent.RAW + "%");

    return query;
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
    return this.getQuery().getResults();
  }
}
