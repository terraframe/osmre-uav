package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;

import net.geoprism.spring.core.JsonObjectDeserializer;

public class SynchronizeOrganizationBody
{
  @NotNull
  @JsonDeserialize(using = JsonObjectDeserializer.class)
  private JsonObject sync;

  public JsonObject getSync()
  {
    return sync;
  }

  public void setSync(JsonObject sync)
  {
    this.sync = sync;
  }
}