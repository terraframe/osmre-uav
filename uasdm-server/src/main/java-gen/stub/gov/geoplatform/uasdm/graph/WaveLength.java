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
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.Page;

public class WaveLength extends WaveLengthBase implements Classification
{
  private static final long  serialVersionUID  = 920922499;

  public static final String LiDAR             = "LiDAR";

  public static final String NATURAL_COLOR_RGB = "Natural Color RGB";

  public static final String NEAR_INFRARED     = "Near InfraRed";

  public static final String RED_EDGE          = "Red Edge";

  public static final String THERMAL           = "Thermal";

  public WaveLength()
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
        Optional<Collection> col = metadata.getCollection();
        
        if (col.isPresent()) {
          new GenerateMetadataCommand(col.get(), null, metadata).doIt();
        } else {
          List<Product> prods = metadata.getProducts();
          
          if (prods.size() > 0) {
            new GenerateMetadataCommand(prods.get(0).getComponent(), prods.get(0), metadata).doIt();
          }
        }
      });
    }
  }
  

  public List<CollectionMetadata> getReferencingMetadata()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(CollectionMetadata.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(CollectionMetadata.SENSOR);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO("gov.geoplatform.uasdm.graph.SensorHasWaveLength");

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " IN (");
    statement.append("   SELECT IN('" + mdEdge.getDBClassName() + "') FROM :rid");
    statement.append(" )");

    final GraphQuery<CollectionMetadata> query = new GraphQuery<CollectionMetadata>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }

  public static WaveLength getByName(String name)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM wave_length WHERE name=:name");

    final GraphQuery<WaveLength> query = new GraphQuery<WaveLength>(statement.toString());
    query.setParameter("name", name);

    return query.getSingleResult();
  }

  public static Long getCount()
  {
    return Classification.getCount(WaveLength.CLASS);
  }

  public static Page<Classification> getPage(JSONObject criteria)
  {
    return Classification.getPage(WaveLength.CLASS, criteria);
  }

  public static JSONArray getAll()
  {
    return Classification.getAll(WaveLength.CLASS);
  }

  public static WaveLength fromJSON(JSONObject json)
  {
    WaveLength classification = null;

    if (json.has(WaveLength.OID))
    {
      String oid = json.getString(WaveLength.OID);

      if (oid != null)
      {
        classification = WaveLength.get(oid);
      }
    }

    if (classification == null)
    {
      classification = new WaveLength();
    }

    classification.setName(json.getString(WaveLength.NAME));

    if (json.has(WaveLength.SEQ))
    {
      classification.setSeq(json.getLong(WaveLength.SEQ));
    }

    return classification;
  }

}
