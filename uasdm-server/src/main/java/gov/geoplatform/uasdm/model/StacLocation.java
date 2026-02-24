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

public class StacLocation
{
  private String label;

  private String uuid;

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getUuid()
  {
    return uuid;
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  public static StacLocation build(String uuid, String label)
  {
    StacLocation location = new StacLocation();
    location.setUuid(uuid);
    location.setLabel(label);

    return location;
  }

}
