package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.geoplatform.uasdm.serialization.JSONObjectDeserializer;

public class MetadataBody
{
  @NotNull
  @JsonDeserialize(using = JSONObjectDeserializer.class)
  private JSONObject selection;

  public JSONObject getSelection()
  {
    return selection;
  }

  public void setSelection(JSONObject selection)
  {
    this.selection = selection;
  }
}
