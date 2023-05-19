package gov.geoplatform.uasdm.cog.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class TitilerCogStatistics
{
  private ObjectNode json;
  
  public TitilerCogStatistics(String json)
  {
    ObjectMapper objectMapper = new ObjectMapper();
    
    try
    {
      this.json = (ObjectNode) objectMapper.readTree(json);
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public TitilerCogBandStatistic getBandStatistic(int bandNum)
  {
    ObjectMapper objectMapper = new ObjectMapper();
    
    JsonNode bandJson = json.get(String.valueOf(bandNum));
    
    return objectMapper.convertValue(bandJson, TitilerCogBandStatistic.class);
  }
}
