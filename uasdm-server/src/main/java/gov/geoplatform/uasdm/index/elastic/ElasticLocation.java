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
package gov.geoplatform.uasdm.index.elastic;

public class ElasticLocation
{
  private String oid;

  private String synchronizationId;

  private String versionId;

  private String label;

  private String typeLabel;

  private String hierarchyLabel;

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public String getSynchronizationId()
  {
    return synchronizationId;
  }

  public void setSynchronizationId(String synchronizationId)
  {
    this.synchronizationId = synchronizationId;
  }

  public String getVersionId()
  {
    return versionId;
  }

  public void setVersionId(String versionId)
  {
    this.versionId = versionId;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }
  
  public String getHierarchyLabel()
  {
    return hierarchyLabel;
  }
  
  public void setHierarchyLabel(String hierarchyLabel)
  {
    this.hierarchyLabel = hierarchyLabel;
  }
  
  public String getTypeLabel()
  {
    return typeLabel;
  }
  
  public void setTypeLabel(String typeLabel)
  {
    this.typeLabel = typeLabel;
  }
}
