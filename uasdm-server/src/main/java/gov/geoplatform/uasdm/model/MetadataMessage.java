package gov.geoplatform.uasdm.model;

import org.json.JSONObject;

public class MetadataMessage implements JSONSerializable
{
  private CollectionIF collection;

  public MetadataMessage(CollectionIF collection)
  {
    this.collection = collection;
  }

  @Override
  public JSONObject toJSON()
  {
    return collection.toMetadataMessage();
  }
}
