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
package gov.geoplatform.uasdm.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.web.WebClientSession;

@ServerEndpoint(value = "/websocket-notifier/notify", configurator = GetHttpSessionConfigurator.class)
public class NotificationEndpoint
{
  private static Set<NotificationEndpoint> endpoints = new CopyOnWriteArraySet<>();

  private static Logger                    logger    = LoggerFactory.getLogger(NotificationEndpoint.class);

  private Session                          session;

  private String                           userId;

  @OnOpen
  public void onOpen(Session session, EndpointConfig config) throws IOException
  {
    HttpSession s = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
    WebClientSession clientSession = (WebClientSession) s.getAttribute(ClientConstants.CLIENTSESSION);

    this.session = session;

    if (!clientSession.getRequest().isPublicUser())
    {
      this.userId = clientSession.getRequest().getSessionUser().getOid();
    }

//    if (this.userId != null)
    {
      endpoints.add(this);

//      logger.debug("Connecting websocket for user [" + this.userId + "]");
    }
  }

  // @OnMessage
  // public void onMessage(Session session) throws IOException
  // {
  // // Do nothing
  // }

  @OnClose
  public void onClose(Session session) throws IOException
  {
//    if (this.userId != null)
    {
      endpoints.remove(this);

//      logger.debug("Closing websocket for user [" + this.userId + "]");
    }
  }

  @OnError
  public void onError(Session session, Throwable throwable)
  {
    // Do error handling here
  }

  public static void broadcast(String userId, JSONObject message)
  {
    logger.debug("Broadcasting to [" + userId + "]: " + message.toString());

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        try
        {
          if (endpoint.userId.equals(userId))
          {
            endpoint.session.getBasicRemote().sendText(message.toString());
          }
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  public static void broadcast(JSONObject message)
  {
    logger.debug("Broadcasting to [all]: " + message.toString());

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        try
        {
          endpoint.session.getBasicRemote().sendText(message.toString());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

}