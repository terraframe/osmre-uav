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