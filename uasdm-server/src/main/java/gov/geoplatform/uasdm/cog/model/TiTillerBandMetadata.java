package gov.geoplatform.uasdm.cog.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({ "name", "metadata" })
public class TiTillerBandMetadata
{
  public String              name;

  public Map<String, String> metadata;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public Map<String, String> getMetadata()
  {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata)
  {
    this.metadata = metadata;
  }

}
