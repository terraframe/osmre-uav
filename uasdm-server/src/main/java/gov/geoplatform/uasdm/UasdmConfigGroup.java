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

import com.runwaysdk.configuration.ConfigurationManager.ConfigGroupIF;

/**
 * Defines the bundle locations for configuration groups. Intended for use with
 * Runway's ConfigurationManager API.
 * 
 * @author Richard Rowlands
 */
public enum UasdmConfigGroup implements ConfigGroupIF {
  CLIENT("uasdm/", "client"), COMMON("uasdm/", "common"), SERVER("uasdm/", "server"), ROOT("", "root");

  private String path;

  private String identifier;

  UasdmConfigGroup(String path, String identifier)
  {
    this.path = path;
    this.identifier = identifier;
  }

  public String getPath()
  {
    return this.path;
  }

  @Override
  public String getOidentifier()
  {
    return identifier;
  }
}