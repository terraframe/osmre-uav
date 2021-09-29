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

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.view.Option;

public class Bureau extends BureauBase
{
  private static final long serialVersionUID = -122368662;

  public Bureau()
  {
    super();
  }

  @Transaction
  public static void create(gov.geoplatform.uasdm.bus.Bureau source)
  {
    Bureau bureau = new Bureau();
    bureau.setName(source.getName());
    bureau.setDisplayLabel(source.getDisplayLabel());
    bureau.setBureau(source);
    bureau.apply();
  }

  @Transaction
  public static void update(gov.geoplatform.uasdm.bus.Bureau source)
  {
    Bureau bureau = getBySource(source);

    if (bureau != null)
    {
      bureau.setName(source.getName());
      bureau.setDisplayLabel(source.getDisplayLabel());
      bureau.apply();
    }
  }

  public static Bureau getBySource(gov.geoplatform.uasdm.bus.Bureau source)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Bureau.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(BUREAU);
    final String className = mdVertex.getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + className);
    builder.append(" WHERE " + mdAttribute.getColumnName() + " = :bureau");

    final GraphQuery<Bureau> query = new GraphQuery<Bureau>(builder.toString());
    query.setParameter("bureau", source);

    return query.getSingleResult();
  }

  public static List<Option> getOptions()
  {
    List<Option> options = new LinkedList<Option>();

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Bureau.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(DISPLAYLABEL);

    final String className = mdVertex.getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + className);
    builder.append(" ORDER BY " + mdAttribute.getColumnName() + " DESC");

    final GraphQuery<Bureau> query = new GraphQuery<Bureau>(builder.toString());

    List<Bureau> bureaus = query.getResults();

    for (Bureau bureau : bureaus)
    {
      options.add(new Option(bureau.getOid(), bureau.getDisplayLabel()));
    }

    return options;
  }
}
