package gov.geoplatform.uasdm.graph;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.SiteObject;

public class SiteObjectDocumentQuery
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

    if (this.skip != null)
    {
      ql.append(" SKIP " + this.skip);
    }

    if (this.limit != null)
    {
      ql.append(" LIMIT " + this.limit);
    }

    final GraphQuery<Document> query = new GraphQuery<Document>(ql.toString());
    query.setParameter("s3location", component.getS3location() + folder + "%");

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
    List<Document> documents = this.getQuery().getResults();

    LinkedList<SiteObject> objects = new LinkedList<SiteObject>();

    for (Document document : documents)
    {
      objects.add(SiteObject.create(component, document));
    }

    return objects;
  }
}
