package gov.geoplatform.uasdm.service.business;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.odm.HTTPConnector;
import gov.geoplatform.uasdm.odm.KnowStacResponse;
import gov.geoplatform.uasdm.odm.Response;

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

  public KnowStacResponse put(StacItem item)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(item);

      Response resp = connector.httpPost("api/item/put", json);

      return new KnowStacResponse(resp);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public KnowStacResponse remove(String id)
  {

    Response resp = connector.httpPost("api/item/remove", Arrays.asList(new BasicNameValuePair("id", id)));

    return new KnowStacResponse(resp);
  }

}
