package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.geoplatform.uasdm.serialization.JSONObjectDeserializer;

public class EntityBody
{
  @NotNull
  @JsonDeserialize(using = JSONObjectDeserializer.class)
  private JSONObject entity;

  public JSONObject getEntity()
  {
    return entity;
  }

  public void setEntity(JSONObject entity)
  {
    this.entity = entity;
  }

}
