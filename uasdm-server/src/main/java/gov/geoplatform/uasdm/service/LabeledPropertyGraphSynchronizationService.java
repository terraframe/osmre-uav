/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.service;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.adapter.HTTPConnector;
import net.geoprism.graph.adapter.RegistryConnectorBuilderIF;
import net.geoprism.graph.adapter.RegistryConnectorFactory;
import net.geoprism.graph.adapter.RegistryConnectorIF;

public class LabeledPropertyGraphSynchronizationService
{
  @Request(RequestType.SESSION)
  public JsonArray getAll(String sessionId)
  {
    return LabeledPropertyGraphSynchronization.getAll();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, JsonObject json)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.fromJSON(json);
    synchronization.apply();

    return synchronization.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);

    if (synchronization != null)
    {
      synchronization.delete();
    }
  }

  @Request(RequestType.SESSION)
  public void execute(String sessionId, String oid)
  {
    // TODO Decide how to handle self-signed HTTPS certs
    RegistryConnectorFactory.setBuilder(new RegistryConnectorBuilderIF()
    {

      @Override
      public RegistryConnectorIF build(String url)
      {

        return new HTTPConnector(url)
        {
          @Override
          public synchronized void initialize()
          {
            try
            {

              CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
              this.setClient(httpClient);
            }
            catch (Exception e)
            {
              throw new RuntimeException(e);
            }
          }
        };
      }
    });

    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);

    if (synchronization != null)
    {
      synchronization.execute();
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return LabeledPropertyGraphSynchronization.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject newInstance(String sessionId)
  {
    return new LabeledPropertyGraphSynchronization().toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, JsonObject criteria)
  {
    return LabeledPropertyGraphSynchronization.page(criteria).toJSON();
  }

}
