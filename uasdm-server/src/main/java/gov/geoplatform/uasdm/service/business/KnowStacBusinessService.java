package gov.geoplatform.uasdm.service.business;

import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.odm.HTTPConnector;
import gov.geoplatform.uasdm.remote.KnowStacResponse;

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

      GenericException ex = new GenericException();
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
      catch (GenericException e)
      {
        throw e;
      }
      catch (JsonProcessingException e)
      {
        throw new ProgrammingErrorException(e);
      }
      catch (RuntimeException e)
      {
        GenericException ex = new GenericException(e);
        ex.setUserMessage("A problem occurred while communicating with the KnowSTAC server. Please try your request again.");
        throw ex;
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
      catch (GenericException e)
      {
        throw e;
      }
      catch (RuntimeException e)
      {
        GenericException ex = new GenericException(e);
        ex.setUserMessage("A problem occurred while communicating with the KnowSTAC server. Please try your request again.");
        throw ex;
      }
    }
  }

}
