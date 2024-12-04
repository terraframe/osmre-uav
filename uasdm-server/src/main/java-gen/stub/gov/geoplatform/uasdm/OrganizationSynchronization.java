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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.registry.lpg.adapter.RegistryBridge;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.spring.core.ApplicationContextHolder;

public class OrganizationSynchronization extends OrganizationSynchronizationBase implements JsonSerializable
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 952465141;

  public OrganizationSynchronization()
  {
    super();
  }

  @Override
  public void execute()
  {
    OrganizationBusinessServiceIF service = ApplicationContextHolder.getBean(OrganizationBusinessServiceIF.class);

    try (RegistryConnectorIF connector = RegistryConnectorFactory.getConnector(this.getUrl()))
    {
      RegistryBridge bridge = new RegistryBridge(connector);

      JsonArray results = bridge.getOrganizations().getJsonArray();

      service.importJsonTree(results);
    }
  }

  public void parse(JsonObject object)
  {
    this.setUrl(object.get(LabeledPropertyGraphSynchronization.URL).getAsString());
  }

  public final JsonObject toJSON()
  {
    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(LabeledPropertyGraphSynchronization.OID, this.getOid());
    }

    object.addProperty(LabeledPropertyGraphSynchronization.URL, this.getUrl());

    return object;
  }
}
