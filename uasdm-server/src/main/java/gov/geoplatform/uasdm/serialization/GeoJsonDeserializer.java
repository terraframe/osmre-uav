package gov.geoplatform.uasdm.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;

public class GeoJsonDeserializer extends JsonDeserializer<Geometry>
{
  @Override
  public Geometry deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException
  {
    try
    {
      return new GeoJsonReader().read(jp.getCodec().readTree(jp).toString());
    }
    catch (ParseException | IOException e)
    {
      throw new JsonParseException(jp, "Unable to parse GeoJSON", e);
    }
  }

}