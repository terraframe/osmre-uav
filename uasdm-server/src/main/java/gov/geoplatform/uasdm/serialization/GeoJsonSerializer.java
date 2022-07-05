package gov.geoplatform.uasdm.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

public class GeoJsonSerializer extends JsonSerializer<Geometry>
{
  @Override
  public void serialize(Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    gen.writeRawValue(new GeoJsonWriter().write(value));
  }
}