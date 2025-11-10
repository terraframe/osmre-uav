package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.geoplatform.uasdm.serialization.JSONObjectDeserializer;

public class StandaloneProductBody
{
  @NotNull
  @JsonDeserialize(using = JSONObjectDeserializer.class)
  private JSONObject productGroup;

  public JSONObject getProductGroup()
  {
    return productGroup;
  }

  public void setProductGroup(JSONObject productGroup)
  {
    this.productGroup = productGroup;
  }
}
