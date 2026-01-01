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