package gov.geoplatform.uasdm.controller.body;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;

import net.geoprism.spring.core.JsonObjectDeserializer;

public class CompleteUserInviteBody
{
  @NotNull
  @JsonDeserialize(using = JsonObjectDeserializer.class)
  JsonObject user;

  @NotEmpty
  String     token;

  public CompleteUserInviteBody()
  {

  }

  public JsonObject getUser()
  {
    return user;
  }

  public void setUser(JsonObject user)
  {
    this.user = user;
  }

  public String getToken()
  {
    return token;
  }

  public void setToken(String token)
  {
    this.token = token;
  }
}