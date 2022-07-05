package gov.geoplatform.uasdm.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeSerializer extends JsonSerializer<Envelope>
{
  @Override
  public void serialize(Envelope value, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    double[] array = new double[] {
        value.getMinX(), value.getMinY(), value.getMaxX(), value.getMaxY()
    };

    gen.writeArray(array, 0, array.length);
  }
}