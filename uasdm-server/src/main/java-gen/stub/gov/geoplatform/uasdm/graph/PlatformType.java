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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.Page;

public class PlatformType extends PlatformTypeBase implements Classification
{
  private static final long serialVersionUID = -1918394479;
  
  public static final String FIXED_WING = "Fixed Wing";

  public PlatformType()
  {
    super();
  }
  
  @Override
  public void apply()
  {
    boolean isNew = this.isNew();

    super.apply();

    if (!isNew)
    {
      this.getReferencingMetadata().forEach(metadata -> {
        new GenerateMetadataCommand(metadata.getProduct().getComponent(), metadata).doIt();
      });
    }
  }

  public List<CollectionMetadata> getReferencingMetadata()
  {
    final MdVertexDAOIF platformVertex = MdVertexDAO.getMdVertexDAO(Platform.CLASS);
    MdAttributeDAOIF platformAttribute = platformVertex.definesAttribute(Platform.PLATFORMTYPE);    
    
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(CollectionMetadata.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(CollectionMetadata.SENSOR);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO("gov.geoplatform.uasdm.graph.PlatformHasSensor");
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " IN (");
    statement.append("   SELECT OUT ('" + mdEdge.getDBClassName() + "') FROM " + platformVertex.getDBClassName());    
    statement.append("   WHERE " + platformAttribute.getColumnName() + " = :rid");    
    statement.append(" )");

    final GraphQuery<CollectionMetadata> query = new GraphQuery<CollectionMetadata>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }
  
  public static PlatformType getByName(String name)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM platform_type WHERE name=:name");

    final GraphQuery<PlatformType> query = new GraphQuery<PlatformType>(statement.toString());
    query.setParameter("name", name);
    
    return query.getSingleResult();
  }

  @Override
  public void delete()
  {
    if (Platform.isPlatformTypeReferenced(this))
    {
      GenericException message = new GenericException();
      message.setUserMessage("The platform type cannot be deleted because it is being used in a platform");
      throw message;
    }

    super.delete();
  }

  public static Long getCount()
  {
    return Classification.getCount(PlatformType.CLASS);
  }

  public static Page<Classification> getPage(JSONObject criteria)
  {
    return Classification.getPage(PlatformType.CLASS, criteria);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(PlatformType.CLASS);
  }

  public static PlatformType fromJSON(JSONObject json)
  {
    PlatformType classification = null;

    if (json.has(PlatformType.OID))
    {
      String oid = json.getString(PlatformType.OID);

      if (oid != null)
      {
        classification = PlatformType.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new PlatformType();
    }

    classification.setName(json.getString(PlatformType.NAME));

    if (json.has(PlatformType.SEQ))
    {
      classification.setSeq(json.getLong(PlatformType.SEQ));
    }

    return classification;
  }

}
