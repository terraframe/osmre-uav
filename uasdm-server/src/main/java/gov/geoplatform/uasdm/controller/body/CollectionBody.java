package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.geoplatform.uasdm.serialization.JSONArrayDeserializer;

public class CollectionBody
{
  @NotNull
  @JsonDeserialize(using = JSONArrayDeserializer.class)
  private JSONArray selections;

  public JSONArray getSelections()
  {
    return selections;
  }

  public void setSelections(JSONArray selections)
  {
    this.selections = selections;
  }
}
