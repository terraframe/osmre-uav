package gov.geoplatform.uasdm.doc;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import net.geoprism.registry.graph.GraphOrganization;

public class HelpPageContent extends HelpPageContentBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1231670035;
  
  public HelpPageContent()
  {
    super();
  }
  
  public static HelpPageContent getByOrg(GraphOrganization org)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(HelpPageContent.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(HelpPageContent.ORGANIZATION);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :organization");

    GraphQuery<HelpPageContent> query = new GraphQuery<HelpPageContent>(statement.toString());
    query.setParameter("organization", org.getRID());

    return query.getSingleResult();
  }
  
}
