package gov.geoplatform.uasdm.postgis;

import java.util.Map;
import java.util.Set;

import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.Join;
import com.runwaysdk.query.SelectableGeometry;
import com.runwaysdk.query.StatementGeometry;
import com.runwaysdk.query.Visitor;
import com.vividsolutions.jts.geom.Geometry;

public class ST_WITHIN extends Condition
{
  private SelectableGeometry selectable;

  private StatementGeometry  statement;

  public ST_WITHIN(SelectableGeometry selectable, Geometry geometry)
  {
    this.selectable = selectable;
    this.statement = new StatementGeometry(geometry, ( (MdAttributeGeometryDAOIF) selectable.getMdAttributeIF() ).getSRID());
  }

  @Override
  public Set<Join> getJoinStatements()
  {
    return null;
  }

  @Override
  public Map<String, String> getFromTableMap()
  {
    return null;
  }

  /**
   * Visitor to traverse the query object structure.
   * 
   * @param visitor
   */
  public void accept(Visitor visitor)
  {
    visitor.visit(this);

    this.statement.accept(visitor);
  }

  /**
   * Returns the SQL representation of this condition.
   *
   * @return SQL representation of this condition.
   */
  public String getSQL()
  {
    String statementSQL1 = this.selectable.getSQL();
    String statementSQL2 = this.statement.getSQL();

    return "ST_Within(" + statementSQL1 + ", " + statementSQL2 + ")";
  }
}
