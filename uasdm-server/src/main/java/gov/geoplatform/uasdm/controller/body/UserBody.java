package gov.geoplatform.uasdm.controller.body;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.spring.core.JsonArrayDeserializer;
import net.geoprism.spring.core.JsonObjectDeserializer;

public class UserBody
{
  @NotEmpty
  @JsonDeserialize(using = JsonObjectDeserializer.class)
  private JsonObject account;

  @Nullable
  @JsonDeserialize(using = JsonArrayDeserializer.class)
  private JsonArray  roleIds;

  public UserBody()
  {

  }

  public JsonObject getAccount()
  {
    return account;
  }

  public void setAccount(JsonObject account)
  {
    this.account = account;
  }

  public JsonArray getRoleIds()
  {
    return roleIds;
  }

  public void setRoleIds(JsonArray roleIds)
  {
    this.roleIds = roleIds;
  }
}