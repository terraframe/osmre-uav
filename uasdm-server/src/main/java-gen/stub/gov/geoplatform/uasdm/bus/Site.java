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
package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.postgis.ST_WITHIN;
import gov.geoplatform.uasdm.view.AttributeListType;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.EqCondition;
import gov.geoplatform.uasdm.view.Option;

public class Site extends SiteBase implements SiteIF
{
  private static final long  serialVersionUID  = -986618112;

  public static final String DEFAULT_SITE_NAME = "Cottonwood";

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

  public ComponentHasComponent addComponent(gov.geoplatform.uasdm.bus.UasComponent uasComponent)
  {
    return this.addProjects((Project) uasComponent);
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

  public static boolean isDuplicateSiteName(String oid, String name)
  {
    QueryFactory qf = new QueryFactory();
    SiteQuery query = new SiteQuery(qf);

    query.WHERE(query.getName().EQ(name));

    if (oid != null)
    {
      query.AND(query.getOid().NE(oid));
    }

    try (OIterator<? extends UasComponent> i = query.getIterator())
    {
      if (i.hasNext())
      {
        return true;
      }
    }

    return false;
  }

  @Override
  public List<AbstractWorkflowTask> getTasks()
  {
    return new LinkedList<AbstractWorkflowTask>();
  }

  public static List<SiteIF> getSites(String bounds)
  {
    QueryFactory qf = new QueryFactory();
    SiteQuery q = new SiteQuery(qf);

    if (bounds != null && bounds.length() > 0)
    {
      // {"_sw":{"lng":-90.55128715174949,"lat":20.209904454730363},"_ne":{"lng":-32.30032930862288,"lat":42.133128793454745}}
      JSONObject object = new JSONObject(bounds);

      JSONObject sw = object.getJSONObject("_sw");
      JSONObject ne = object.getJSONObject("_ne");

      double x1 = sw.getDouble("lng");
      double x2 = ne.getDouble("lng");
      double y1 = sw.getDouble("lat");
      double y2 = ne.getDouble("lat");

      Envelope envelope = new Envelope(x1, x2, y1, y2);
      GeometryFactory factory = new GeometryFactory();
      Geometry geometry = factory.toGeometry(envelope);

      q.WHERE(new ST_WITHIN(q.getGeoPoint(), geometry));
    }

    q.ORDER_BY_ASC(q.getName());

    try (OIterator<? extends Site> i = q.getIterator())
    {
      return new LinkedList<SiteIF>(i.getAll());
    }
  }

}
