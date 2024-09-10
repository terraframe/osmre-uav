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
import gov.geoplatform.uasdm.odm.Response;
import gov.geoplatform.uasdm.remote.KnowStacNoopResponse;
import gov.geoplatform.uasdm.remote.KnowStacResponse;
import gov.geoplatform.uasdm.remote.KnowStacResponseIF;

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

  public KnowStacResponseIF put(StacItem item)
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(item);

      Response resp = connector.httpPost("api/item/put", json);

      return new KnowStacResponse(resp);
    }
    catch (RuntimeException | IOException e)
    {
      logger.error("Unable to upload stac item to know stac server", e);
    }
    return new KnowStacNoopResponse();
  }

  public KnowStacResponseIF remove(String id)
  {
    try
    {
      Response resp = connector.httpPost("api/item/remove", Arrays.asList(new BasicNameValuePair("id", id)));

      return new KnowStacResponse(resp);
    }
    catch (RuntimeException e)
    {
      logger.error("Unable to remove stac item to know stac server", e);
    }

    return new KnowStacNoopResponse();
  }

}
