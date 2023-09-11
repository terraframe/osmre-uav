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
package gov.geoplatform.uasdm.index;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.graph.VertexObject;

import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.QueryResult;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public interface Index
{
  public boolean startup();

  public void shutdown();

  public void deleteDocuments(String fieldId, String oid);

  public void deleteDocument(UasComponentIF component, String key);

  public void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name);

  public void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata);

  public void updateComponent(UasComponentIF component, boolean isNameModified);

  public void createDocument(List<UasComponentIF> ancestors, UasComponentIF component);

  public List<QueryResult> query(String text);

  public void createStacItems(ProductIF product);

  public void removeStacItems(ProductIF product);

  public JSONArray getTotals(String text, JSONArray filters);

  public Page<StacItem> getItems(JSONObject criteria, Integer pageSize, Integer pageNumber);

  public void clear();

  public void createDocument(LabeledPropertyGraphSynchronization synchronization, VertexObject object);

  public void deleteDocuments(LabeledPropertyGraphSynchronization synchronization);
  
  public void deleteDocuments(LabeledPropertyGraphTypeVersion version);
}
