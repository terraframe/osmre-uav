package gov.geoplatform.uasdm.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeDeserializer extends JsonDeserializer<Envelope>
{
  @Override
  public Envelope deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException
  {
    Double[] values = jsonParser.readValueAs(Double[].class);

    return new Envelope(values[0], values[2], values[1], values[3]);
  }

}