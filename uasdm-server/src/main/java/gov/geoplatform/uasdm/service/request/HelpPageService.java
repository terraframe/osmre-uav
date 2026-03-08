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
package gov.geoplatform.uasdm.service.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.service.business.HelpPageBusinessService;

@Service
@Primary
public class HelpPageService
{
  @Autowired
  private HelpPageBusinessService service;

  @Request(RequestType.SESSION)
  public JsonObject content(String sessionId, String orgCode)
  {
    JsonObject json = this.service.content(orgCode);

    return json;
  }
  
  @Request(RequestType.SESSION)
  public void edit(String sessionId, String orgCode, String content)
  {
    this.service.edit(orgCode, content);
  }

}
