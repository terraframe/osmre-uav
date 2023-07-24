package gov.geoplatform.uasdm.mock;

import java.io.InputStreamReader;

import org.apache.http.NameValuePair;

import com.google.gson.JsonElement;
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
