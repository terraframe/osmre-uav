/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.ws;

import org.json.JSONObject;

public abstract class NotificationMessage implements Runnable
{

  private MessageType type;

  private JSONObject  content;

  public NotificationMessage(MessageType type, JSONObject content)
  {
    super();
    this.type = type;
    this.content = content;
  }

  public JSONObject getMessage()
  {
    JSONObject message = new JSONObject();
    message.put("type", this.type.name());

    if (this.content != null)
    {
      message.put("content", this.content);
    }

    return message;
  }

  public JSONObject getContent()
  {
    return content;
  }

  public void setContent(JSONObject content)
  {
    this.content = content;
  }

  public MessageType getType()
  {
    return type;
  }

  public void setType(MessageType type)
  {
    this.type = type;
  }

  public static JSONObject content(String property, String value)
  {
    JSONObject object = new JSONObject();
    object.put(property, value);

    return object;
  }
}
