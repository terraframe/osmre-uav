package gov.geoplatform.uasdm.graph;

import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;

public class UAV extends UAVBase implements JSONSerializable
{
  private static final long serialVersionUID = 1730854538;

  public UAV()
  {
    super();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.getOid());
    object.put(UAV.SERIALNUMBER, this.getSerialNumber());
    object.put(UAV.FAANUMBER, this.getFaaNumber());
    object.put(UAV.DESCRIPTION, this.getDescription());
    object.put(UAV.BUREAU, this.getBureauOid());

    String platform = this.getObjectValue(UAV.PLATFORM);

    if (platform != null)
    {
      object.put(UAV.PLATFORM, platform);
    }

    if (this.getSeq() != null)
    {
      object.put(UAV.SEQ, this.getSeq());
    }

    return object;
  }

  @Transaction
  public static UAV apply(JSONObject json)
  {
    UAV uav = null;

    if (json.has(UAV.OID))
    {
      String oid = json.getString(UAV.OID);

      if (oid != null)
      {
        uav = UAV.get(oid);
      }
    }

    if (uav == null)
    {
      uav = new UAV();
    }

    uav.setSerialNumber(json.getString(UAV.SERIALNUMBER));
    uav.setFaaNumber(json.getString(UAV.FAANUMBER));
    uav.setDescription(json.getString(UAV.DESCRIPTION));
    uav.setBureauId(json.getString(UAV.BUREAU));

    if (json.has(UAV.PLATFORM))
    {
      String oid = json.getString(UAV.PLATFORM);

      uav.setPlatform(Platform.get(oid));
    }
    else
    {
      uav.setPlatform(null);
    }

    if (json.has(UAV.SEQ))
    {
      uav.setSeq(json.getLong(UAV.SEQ));
    }

    uav.apply();

    return uav;
  }

  public static Long getCount()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }

  public static Page<UAV> getPage(Integer pageNumber, Integer pageSize)
  {
    final Long count = UAV.getCount();

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(UAV.SERIALNUMBER);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" ORDER BY " + mdAttribute.getColumnName());
    statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    final GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString());

    return new Page<UAV>(count, pageNumber, pageSize, query.getResults());
  }

  public static boolean isPlatformReferenced(Platform platform)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(UAV.PLATFORM);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :platform");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("platform", platform.getRID());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

  public static List<UAV> search(String text)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UAV.CLASS);
    MdAttributeDAOIF serialAttribute = mdVertex.definesAttribute(UAV.SERIALNUMBER);
    MdAttributeDAOIF faaAttribute = mdVertex.definesAttribute(UAV.FAANUMBER);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + serialAttribute.getColumnName() + ".toUpper() LIKE :text");
    statement.append(" OR " + faaAttribute.getColumnName() + ".toUpper() LIKE :text");
    statement.append(" ORDER BY " + serialAttribute.getColumnName());

    final GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString());
    query.setParameter("text", text);

    return query.getResults();
  }

}
