package gov.geoplatform.uasdm.serialization;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

public class DateDeserializer extends JsonDeserializer<Date>
{
  private SimpleDateFormat format;

  public DateDeserializer()
  {
    this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    this.format.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException
  {
    String dateStr = jsonParser.readValueAs(String.class);

    try
    {
      return this.format.parse(dateStr);
    }
    catch (ParseException e)
    {
      throw ValueInstantiationException.from(jsonParser, "Unable to parse [" + dateStr + "]", null, e);
    }
  }

}