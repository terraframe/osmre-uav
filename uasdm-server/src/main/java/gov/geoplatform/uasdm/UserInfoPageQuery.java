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
package gov.geoplatform.uasdm;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.MdStructDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.MdRelationshipDAO;
import com.runwaysdk.dataaccess.metadata.MdStructDAO;
import com.runwaysdk.system.Users;

import net.geoprism.GeoprismUser;
import net.geoprism.account.ExternalProfile;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationDisplayLabel;

public class UserInfoPageQuery
{
  private JSONObject criteria;

  public UserInfoPageQuery(JSONObject criteria)
  {
    this.criteria = criteria;
  }

  private void addFromClause(StringBuilder statement)
  {
    MdBusinessDAOIF mdUserInfo = MdBusinessDAO.getMdBusinessDAO(UserInfo.CLASS);
    MdBusinessDAOIF mdOrganization = MdBusinessDAO.getMdBusinessDAO(Organization.CLASS);
    MdRelationshipDAOIF mdOrganizationHasUser = MdRelationshipDAO.getMdRelationshipDAO(OrganizationHasUser.CLASS);
    MdStructDAOIF mdDisplayLabel = MdStructDAO.getMdStructDAO(OrganizationDisplayLabel.CLASS);

    if (AppProperties.requireKeycloakLogin())
    {
      MdBusinessDAOIF mdUser = MdBusinessDAO.getMdBusinessDAO(ExternalProfile.CLASS);
      statement.append(" FROM " + mdUser.getTableName() + " AS geo_user" + "\n");
    }
    else
    {
      MdBusinessDAOIF mdGeoUser = MdBusinessDAO.getMdBusinessDAO(GeoprismUser.CLASS);
      MdBusinessDAOIF mdUser = MdBusinessDAO.getMdBusinessDAO(Users.CLASS);
      statement.append(" FROM " + mdGeoUser.getTableName() + " AS geo_user" + "\n");
      statement.append(" LEFT JOIN " + mdUser.getTableName() + " AS _user ON _user.oid = geo_user.oid" + "\n");
    }
    
    statement.append(" LEFT JOIN " + mdUserInfo.getTableName() + " AS user_info ON user_info." + mdUserInfo.definesAttribute(UserInfo.GEOPRISMUSER).getColumnName() + " = geo_user.oid" + "\n");
    statement.append(" LEFT JOIN " + mdOrganizationHasUser.getTableName() + " AS org_user ON org_user.child_oid = user_info.oid" + "\n");
    statement.append(" LEFT JOIN " + mdOrganization.getTableName() + " AS org ON org_user.parent_oid = org.oid" + "\n");
    statement.append(" LEFT JOIN " + mdDisplayLabel.getTableName() + " AS olabel ON org.display_label = olabel.oid" + "\n");
  }

  @SuppressWarnings("unchecked")
  private void addCriteria(StringBuilder statement)
  {
    Map<String, Object> parameters = new HashMap<>();

    MdBusinessDAOIF mdGeoUser = MdBusinessDAO.getMdBusinessDAO(GeoprismUser.CLASS);

    if (criteria.has("filters"))
    {
      JSONObject filters = criteria.getJSONObject("filters");
      Iterator<String> keys = filters.keys();

      while (keys.hasNext())
      {
        String attributeName = keys.next();
        JSONObject filter = filters.getJSONObject(attributeName);

        String value = filter.get("value").toString();
        // String mode = filter.get("matchMode").toString();

        int count = parameters.size();
        String parameterName = "p" + parameters.size();
        String clause = count == 0 ? "WHERE" : "AND";

        if (attributeName.equals(UserInfo.ORGANIZATION))
        {
          statement.append(" " + clause + " " + "UPPER(olabel.default_locale) LIKE '%" + value.toUpperCase() + "%'" + "\n");

          parameters.put(parameterName, value);
        }
        else if (attributeName.equals(GeoprismUser.USERNAME))
        {
          statement.append(" " + clause + " " + "UPPER(username) LIKE '%" + value.toUpperCase() + "%'" + "\n");

          parameters.put(parameterName, value.toUpperCase());
        }
        else
        {
          MdAttributeConcreteDAOIF attribute = mdGeoUser.definesAttribute(attributeName);

          if (attribute != null)
          {
            statement.append(" " + clause + " UPPER(" + attribute.getColumnName() + ") LIKE '%" + value.toUpperCase() + "%'" + "\n");

            parameters.put(parameterName, value.toUpperCase());
          }
        }

      }
    }
  }

  public long getCount()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*)");

    this.addFromClause(statement);
    this.addCriteria(statement);

    try (ResultSet results = Database.query(statement.toString()))
    {
      if (results.next())
      {
        return results.getLong(1);
      }
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return 0L;
  }

  public JSONArray getResults()
  {
    MdBusinessDAOIF mdGeoUser = MdBusinessDAO.getMdBusinessDAO(GeoprismUser.CLASS);
    MdBusinessDAOIF mdUser = MdBusinessDAO.getMdBusinessDAO(Users.CLASS);
    MdStructDAOIF mdDisplayLabel = MdStructDAO.getMdStructDAO(OrganizationDisplayLabel.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT geo_user.oid AS oid");
    statement.append(", " + mdUser.definesAttribute(Users.USERNAME).getColumnName() + " AS username");
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.FIRSTNAME).getColumnName() + " AS firstName");
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.LASTNAME).getColumnName() + " AS lastName");
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.PHONENUMBER).getColumnName() + " AS phoneNumber");
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.EMAIL).getColumnName() + " AS email");
    statement.append(", ARRAY_AGG ( olabel." + mdDisplayLabel.definesAttribute(OrganizationDisplayLabel.DEFAULTLOCALE).getColumnName() + ") AS organization" + "\n");

    this.addFromClause(statement);
    this.addCriteria(statement);

    statement.append("GROUP BY geo_user.oid");
    statement.append(", " + mdUser.definesAttribute(Users.USERNAME).getColumnName());
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.FIRSTNAME).getColumnName());
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.LASTNAME).getColumnName());
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.PHONENUMBER).getColumnName());
    statement.append(", " + mdGeoUser.definesAttribute(GeoprismUser.EMAIL).getColumnName() + "\n");

    int limit = 10;
    int first = 0;

    if (criteria.has("first") && criteria.has("rows"))
    {
      first = criteria.getInt("first");
      limit = criteria.getInt("rows");
    }

    if (limit != -1)
    {
      statement.append(" LIMIT " + limit + " OFFSET " + first + "\n");
    }

    JSONArray jsonResults = new JSONArray();

    try (ResultSet results = Database.query(statement.toString()))
    {
      while (results.next())
      {
        Array array = results.getArray(UserInfo.ORGANIZATION);
        List<String> orgs = Arrays.stream((String[]) array.getArray()).filter(s -> s != null).collect(Collectors.toList());

        JSONObject result = new JSONObject();
        result.put(GeoprismUser.OID, results.getString(GeoprismUser.OID));
        result.put(GeoprismUser.USERNAME, results.getString(GeoprismUser.USERNAME));
        result.put(GeoprismUser.FIRSTNAME, results.getString(GeoprismUser.FIRSTNAME));
        result.put(GeoprismUser.LASTNAME, results.getString(GeoprismUser.LASTNAME));
        result.put(GeoprismUser.PHONENUMBER, results.getString(GeoprismUser.PHONENUMBER));
        result.put(GeoprismUser.EMAIL, results.getString(GeoprismUser.EMAIL));
        result.put(UserInfo.ORGANIZATION, String.join(", ", orgs));

        jsonResults.put(result);
      }
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return jsonResults;
  }

  public JSONObject getPage()
  {
    int limit = 10;
    int first = 0;

    if (criteria.has("first") && criteria.has("rows"))
    {
      first = criteria.getInt("first");
      limit = criteria.getInt("rows");
    }

    int pageNumber = ( first / limit ) + 1;

    JSONObject page = new JSONObject();
    page.put("resultSet", this.getResults());
    page.put("count", this.getCount());
    page.put("pageNumber", pageNumber);
    page.put("pageSize", limit);

    return page;

  }

}
