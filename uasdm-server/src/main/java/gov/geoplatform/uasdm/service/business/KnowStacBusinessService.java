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
package gov.geoplatform.uasdm.service.business;

import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.odm.HTTPConnector;
import gov.geoplatform.uasdm.remote.KnowStacResponse;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.NotificationMessage;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;

@Service
public class KnowStacBusinessService
{
  private static final Logger logger = LoggerFactory.getLogger(KnowStacBusinessService.class);

  private HTTPConnector       connector;

  public KnowStacBusinessService()
  {
    this.connector = new HTTPConnector();
    this.connector.setServerUrl(AppProperties.getKnowStacUrl());
  }

  private void processRespose(KnowStacResponse resp)
  {
    if (resp.hasError())
    {
      String message = resp.getError();

      GenericException ex = new GenericException(message);
      ex.setUserMessage(message);
      throw ex;
    }
  }

  public void put(StacItem item)
  {
    if (AppProperties.isKnowStacEnabled())
    {

      try
      {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(item);

        this.processRespose(new KnowStacResponse(connector.httpPost("api/item/put", json)));
      }
      // catch (GenericException e)
      // {
      // throw e;
      // }
      // catch (JsonProcessingException e)
      // {
      // throw new ProgrammingErrorException(e);
      // }
      // catch (RuntimeException e)
      // {
      // GenericException ex = new GenericException(e);
      // ex.setUserMessage("A problem occurred while communicating with the
      // KnowSTAC server. Please try your request again.");
      // throw ex;
      // }
      catch (Exception e)
      {
        logger.error("An error occurred communicating with know-stac", e);

        SessionIF session = Session.getCurrentSession();

        if (session != null)
        {
          JSONObject content = NotificationMessage.content("text", "There was a problem publishing the product to GeoPlatform. If you want this product to be available on GeoPlatform please republish the product later.");

          NotificationFacade.queue(new UserNotificationMessage(session, MessageType.NOTIFICATION, content));
        }
      }
    }
  }

  public void remove(String id)
  {
    if (AppProperties.isKnowStacEnabled())
    {
      try
      {
        processRespose(new KnowStacResponse(connector.httpPost("api/item/remove", Arrays.asList(new BasicNameValuePair("id", id)))));
      }
//      catch (GenericException e)
//      {
//        throw e;
//      }
//      catch (RuntimeException e)
//      {
//        GenericException ex = new GenericException(e);
//        ex.setUserMessage("A problem occurred while communicating with the KnowSTAC server. Please try your request again.");
//        throw ex;
//      }
      catch (Exception e)
      {
        logger.error("An error occurred communicating with know-stac", e);

        SessionIF session = Session.getCurrentSession();

        if (session != null)
        {
          JSONObject content = NotificationMessage.content("text", "There was a problem removing the product from GeoPlatform.");

          NotificationFacade.queue(new UserNotificationMessage(session, MessageType.NOTIFICATION, content));
        }
      }
      
    }
  }

}
