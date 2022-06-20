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
package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ComponentStrategy
{
  public UasComponentIF getComponent(String oid);

  public SiteIF getSite(String oid);

  public ProjectIF getProject(String oid);

  public MissionIF getMission(String oid);

  public CollectionIF getCollection(String oid);

  public ImageryIF getImagery(String oid);

  public ProductIF getProduct(String oid);

  public DocumentIF getDocument(String oid);

  public List<ProductIF> getProducts();

  public List<SiteIF> getSites(String conditions, String sort);

  public Page<MetadataMessage> getMissingMetadata(Integer pageNumber, Integer pageSize);

  public long getMissingMetadataCount();

  public UasComponentIF newRoot();

  public JSONObject features() throws IOException;

  public JSONArray bbox();
}
