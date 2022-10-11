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