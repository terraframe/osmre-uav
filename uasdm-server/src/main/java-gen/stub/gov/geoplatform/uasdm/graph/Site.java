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
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;

import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.DuplicateSiteException;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.AttributeListType;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.EqCondition;
import gov.geoplatform.uasdm.view.Option;

public class Site extends SiteBase implements SiteIF
{
  public static final long serialVersionUID = 2038909434;

  private static final String CHILD_EDGE = "gov.geoplatform.uasdm.graph.SiteHasProject";

  public Site()
  {
    super();
  }

  @Override
  public void applyWithParent(UasComponentIF parent)
  {
    if (this.isNew() && isDuplicateSiteName(this.getOid(), this.getName()))
    {
      DuplicateSiteException e = new DuplicateSiteException();
      e.setFolderName(this.getName());

      throw e;
    }

    String bureauOid = this.getBureauOid();

    if (bureauOid != null && bureauOid.length() > 0)
    {
      Bureau bureau = Bureau.get(bureauOid);

      if (bureau == null)
      {
        throw new ProgrammingErrorException("Bad oid for bureau value [" + bureauOid + "]");
      }
    }

    super.applyWithParent(parent);
  }

  @Override
  public List<AttributeType> attributes()
  {
    AttributeListType attributeType = (AttributeListType) AttributeType.create(this.getMdAttributeDAO(Site.BUREAU));
    attributeType.setOptions(Site.getBureauOptions());

    AttributeType otherAttributeType = AttributeType.create(this.getMdAttributeDAO(Site.OTHERBUREAUTXT));
    otherAttributeType.setCondition(Site.getBureauCondition());
    otherAttributeType.setRequired(true);

    List<AttributeType> list = super.attributes();
    list.add(attributeType);
    list.add(otherAttributeType);
    list.add(AttributeType.create(this.getMdAttributeDAO(Site.GEOPOINT)));

    return list;
  }

  @Override
  public String getSolrIdField()
  {
    return "siteId";
  }

  @Override
  public String getSolrNameField()
  {
    return "siteName";
  }

  @Override
  public Project createDefaultChild()
  {
    return new Project();
  }

  protected boolean needsUpdate()
  {
    return this.isModified(Site.BUREAU);
  }

  @Request
  public static EqCondition getBureauCondition()
  {
    return new EqCondition(Site.BUREAU, Bureau.getByKey(Bureau.OTHER).getOid());
  }

  @Request
  public static List<Option> getBureauOptions()
  {
    return Bureau.getOptions();
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }

  @Override
  protected MdEdgeDAOIF getParentMdEdge()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected MdEdgeDAOIF getChildMdEdge()
  {
    return MdEdgeDAO.getMdEdgeDAO(CHILD_EDGE);
  }

  @Override
  public List<UasComponentIF> getParents()
  {
    return new LinkedList<UasComponentIF>();
  }

  @Override
  protected String buildProductExpandClause()
  {
    return Site.expandClause();
  }

  public static boolean isDuplicateSiteName(String oid, String name)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + NAME + " = :name");

    if (oid != null)
    {
      statement.append(" AND " + OID + " != :oid");
    }

    final GraphQuery<Site> query = new GraphQuery<Site>(statement.toString());
    query.setParameter("name", name);

    if (oid != null)
    {
      query.setParameter("oid", oid);
    }

    return ( query.getResults().size() > 0 );
  }

  public static List<SiteIF> getSites(String conditions, String sort)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final TreeMap<String, Object> parameters = new TreeMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    if (conditions != null && conditions.length() > 0)
    {
      JSONArray array = new JSONArray(conditions);

      for (int i = 0; i < array.length(); i++)
      {
        JSONObject condition = array.getJSONObject(i);

        String field = condition.getString("field");

        if (field.equalsIgnoreCase("bounds"))
        {
          // {"_sw":{"lng":-90.55128715174949,"lat":20.209904454730363},"_ne":{"lng":-32.30032930862288,"lat":42.133128793454745}}
          JSONObject object = condition.getJSONObject("value");

          JSONObject sw = object.getJSONObject("_sw");
          JSONObject ne = object.getJSONObject("_ne");

          double x1 = sw.getDouble("lng");
          double x2 = ne.getDouble("lng");
          double y1 = sw.getDouble("lat");
          double y2 = ne.getDouble("lat");

          Envelope envelope = new Envelope(x1, x2, y1, y2);
          WKTWriter writer = new WKTWriter();
          GeometryFactory factory = new GeometryFactory();
          Geometry geometry = factory.toGeometry(envelope);

          statement.append(i == 0 ? " WHERE" : " AND");
          statement.append(" ST_WITHIN(geoPoint, ST_GeomFromText(:wkt)) = true");

          parameters.put("wkt", writer.write(geometry));
        }
        else if (field.equalsIgnoreCase(Site.BUREAU))
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(field.equalsIgnoreCase(Site.BUREAU) ? Site.CLASS : UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            String value = condition.getString("value");

            statement.append(i == 0 ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName());

            parameters.put(mdAttribute.getColumnName(), value);
          }

        }
        else
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            String value = condition.getString("value");

            statement.append(i == 0 ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName());

            parameters.put(mdAttribute.getColumnName(), value);
          }
        }
      }
    }

    if (sort != null && sort.length() > 0)
    {
      MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(sort.equalsIgnoreCase(Site.BUREAU) ? Site.CLASS : UasComponent.CLASS);

      MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(sort);

      statement.append(" ORDER BY " + mdAttribute.getColumnName());
    }
    else
    {
      MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

      MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(UasComponent.NAME);

      statement.append(" ORDER BY " + mdAttribute.getColumnName());
    }

    final GraphQuery<SiteIF> query = new GraphQuery<SiteIF>(statement.toString(), parameters);

    return query.getResults();
  }

  public static String expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.SITE_HAS_PROJECT);

    return "OUT('" + mdEdge.getDBClassName() + "')." + Project.expandClause();
  }
}
