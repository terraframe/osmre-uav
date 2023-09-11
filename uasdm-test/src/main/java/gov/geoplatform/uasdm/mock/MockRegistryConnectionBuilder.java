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
package gov.geoplatform.uasdm.mock;

import java.io.InputStreamReader;

import org.apache.http.NameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;

import net.geoprism.graph.adapter.RegistryConnectorBuilderIF;
import net.geoprism.graph.adapter.RegistryConnectorIF;
import net.geoprism.graph.adapter.exception.BadServerUriException;
import net.geoprism.graph.adapter.exception.HTTPException;
import net.geoprism.graph.adapter.response.RegistryResponse;

public class MockRegistryConnectionBuilder implements RegistryConnectorBuilderIF
{

  public static class LocalRegistryConnector implements RegistryConnectorIF
  {
    private int objectCount = 0;

    private int edgeCount   = 0;

    @Override
    public String getServerUrl()
    {
      return "localhost";
    }

    @Override
    @Request
    public RegistryResponse httpGet(String url, NameValuePair... params) throws HTTPException, BadServerUriException
    {
      if (url.endsWith("get"))
      {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(this.getClass().getResourceAsStream("/labeled_type.json")));

        return new RegistryResponse(element.toString(), 200);
      }
      else if (url.endsWith("entry"))
      {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(this.getClass().getResourceAsStream("/entry.json")));

        return new RegistryResponse(element.toString(), 200);
      }
      else if (url.endsWith("version"))
      {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(this.getClass().getResourceAsStream("/version.json")));

        return new RegistryResponse(element.toString(), 200);
      }
      else if (url.endsWith("data"))
      {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(this.getClass().getResourceAsStream("/data.json")));

        return new RegistryResponse(element.toString(), 200);
      }
      else if (url.endsWith("geo-objects"))
      {
        if (objectCount == 0)
        {
          objectCount++;

          JsonObject element = JsonParser.parseReader(new InputStreamReader(this.getClass().getResourceAsStream("/data.json"))).getAsJsonObject();
          JsonArray objects = element.get("geoObjects").getAsJsonArray();

          return new RegistryResponse(objects.toString(), 200);
        }

        return new RegistryResponse(new JsonArray().toString(), 200);
      }
      else if (url.endsWith("edges"))
      {
        if (edgeCount == 0)
        {
          edgeCount++;

          JsonObject element = JsonParser.parseReader(new InputStreamReader(this.getClass().getResourceAsStream("/data.json"))).getAsJsonObject();
          JsonArray edges = element.get("edges").getAsJsonArray();

          return new RegistryResponse(edges.toString(), 200);
        }

        return new RegistryResponse(new JsonArray().toString(), 200);
      }

      throw new BadServerUriException();
    }

    @Override
    public void close()
    {
      // Do nothing
    }

  }

  @Override
  public RegistryConnectorIF build(String url)
  {
    return new LocalRegistryConnector();
  }

}
