package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONObject;

import com.runwaysdk.business.graph.VertexQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.Bureau;
import gov.geoplatform.uasdm.bus.DuplicateSiteException;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.AttributeListType;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.EqCondition;
import gov.geoplatform.uasdm.view.Option;

public class Site extends SiteBase implements SiteIF
{
  public static final long    serialVersionUID = 2038909434;

  private static final String CHILD_EDGE       = "gov.geoplatform.uasdm.graph.SiteHasProject";

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
  public List<UasComponent> getParents()
  {
    return new LinkedList<UasComponent>();
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

    final VertexQuery<Site> query = new VertexQuery<Site>(statement.toString());
    query.setParameter("name", name);

    if (oid != null)
    {
      query.setParameter("oid", oid);
    }

    return ( query.getResults().size() > 0 );
  }

  public static List<Site> getSites(String bounds)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final TreeMap<String, Object> parameters = new TreeMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

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
      WKTWriter writer = new WKTWriter();
      GeometryFactory factory = new GeometryFactory();
      Geometry geometry = factory.toGeometry(envelope);

//      statement.append(" WHERE ST_WITHIN(geoPoint, ST_GeomFromText('" + writer.write(geometry) + "')) = true");
      statement.append(" WHERE ST_WITHIN(geoPoint, ST_GeomFromText(:wkt)) = true");
//      statement.append(" WHERE ST_WITHIN(geoPoint, :location) = true");

      parameters.put("wkt", writer.write(geometry));

//      q.WHERE(new ST_WITHIN(q.getGeoPoint(), geometry));
    }

    final VertexQuery<Site> query = new VertexQuery<Site>(statement.toString(), parameters);

    return query.getResults();
  }

}
