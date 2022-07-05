package gov.geoplatform.uasdm.serialization;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.StacLink;
import gov.geoplatform.uasdm.model.StacItem.Asset;

public class StacItemSerializer extends JsonSerializer<StacItem>
{

  @Override
  public void serialize(StacItem item, JsonGenerator gen, SerializerProvider provider) throws IOException
  {
    gen.writeStartObject();

    try
    {
      if (item != null)
      {

        gen.writeStringField("type", item.getType());
        gen.writeStringField("stac_version", item.getStacVersion());

        gen.writeArrayFieldStart("stac_extensions");

        if (item.getStacExtensions() != null)
        {
          for (String ext : item.getStacExtensions())
          {
            gen.writeString(ext);
          }
        }

        gen.writeEndArray();

        gen.writeStringField("id", item.getId());

        if (item.getGeometry() != null)
        {
//          gen.writeFieldOb("geometry");          
          
          gen.writeObjectField("geometry",  item.getGeometry());
//          gen.writeObjectField("bbox", item.getBbox());
        }
        else
        {
          gen.writeNullField("geometry");
        }

        Map<String, Object> properties = item.getProperties();

        if (properties != null)
        {
          gen.writeObjectFieldStart("properties");

          Set<Entry<String, Object>> entries = properties.entrySet();

          for (Entry<String, Object> entry : entries)
          {
            gen.writeObjectField(entry.getKey(), entry.getValue());
          }

          gen.writeEndObject();
        }

        List<StacLink> links = item.getLinks();

        if (links != null)
        {
          gen.writeArrayFieldStart("links");

          for (StacLink link : links)
          {
            gen.writeObject(link);
          }

          gen.writeEndArray();
        }

        Map<String, Asset> assets = item.getAssets();

        if (assets != null)
        {
          gen.writeObjectFieldStart("assets");

          Set<Entry<String, Asset>> entries = assets.entrySet();

          for (Entry<String, Asset> entry : entries)
          {
            gen.writeObjectField(entry.getKey(), entry.getValue());
          }

          gen.writeEndObject();
        }

        if (item.getCollection() != null)
        {
          gen.writeStringField("collection", item.getCollection());
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      // ExceptionController.getInstance().error("Error while Serializing the
      // Loan Application Object", v_exException);
    }
    finally
    {
      gen.writeEndObject();
    }
  }
}
