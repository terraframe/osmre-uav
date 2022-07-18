package gov.geoplatform.uasdm.serialization;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DateSerializer extends JsonSerializer<Date>
{
  private SimpleDateFormat format;

  public DateSerializer()
  {
    this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    this.format.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException
  {
    String dateStr = this.format.format(value);

    gen.writeString(dateStr);
  }
}