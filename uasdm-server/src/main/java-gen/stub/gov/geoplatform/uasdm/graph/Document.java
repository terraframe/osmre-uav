package gov.geoplatform.uasdm.graph;

import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Document extends DocumentBase implements DocumentIF
{
  private static final long serialVersionUID = -1445705168;

  public Document()
  {
    super();
  }

  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_DOCUMENT).apply();
    }
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    super.delete();

    if (removeFromS3 && !this.getS3location().trim().equals(""))
    {
      this.deleteS3File(this.getS3location());
    }
  }

  public UasComponent getComponent()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
    final List<UasComponent> parents = this.getParents(mdEdge, UasComponent.class);

    return parents.get(0);
  }

  protected void deleteS3File(String key)
  {
    final RemoteFileDeleteCommand command = new RemoteFileDeleteCommand(key);
    command.doIt();
  }

  @Override
  protected String buildKey()
  {
    return this.getS3location();
  }

  public static Document createIfNotExist(UasComponentIF uasComponent, String key, String name)
  {
    Document document = Document.find(key);

    if (document == null)
    {
      document = new Document();
      document.setS3location(key);
    }
    else
    {
    }

    document.setName(name);
    document.apply(uasComponent);

    return document;
  }

  public static Document find(String key)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Document.CLASS);

    String statement = "SELECT FROM " + mdVertex.getDBClassName() + " WHERE s3location = :s3location";

    final GraphQuery<Document> query = new GraphQuery<Document>(statement);
    query.setParameter("s3location", key);

    return query.getSingleResult();
  }

  public void addGeneratedProduct(ProductIF product)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.DOCUMENT_GENERATED_PRODUCT);

    this.addChild((Product) product, mdEdge).apply();
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.getOid());
    object.put("key", this.getS3location());
    object.put("name", this.getName());
    object.put("component", this.getComponent().getOid());

    return object;
  }

}
