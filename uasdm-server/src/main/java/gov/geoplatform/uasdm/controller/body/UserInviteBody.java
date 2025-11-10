package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.spring.core.JsonArrayDeserializer;
import net.geoprism.spring.core.JsonObjectDeserializer;

public class UserInviteBody
{
  @NotNull
  @JsonDeserialize(using = JsonObjectDeserializer.class)
  JsonObject invite;

  @JsonDeserialize(using = JsonArrayDeserializer.class)
  JsonArray  roleIds;

  public UserInviteBody()
  {

  }

  public JsonObject getInvite()
  {
    return invite;
  }

  public void setInvite(JsonObject invite)
  {
    this.invite = invite;
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