package gov.geoplatform.uasdm.service;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.Platform;
import gov.geoplatform.uasdm.bus.PlatformQuery;

public class PlatformService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, Integer pageNumber)
  {
    PlatformQuery query = Platform.page(20, pageNumber);

    return query.toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONArray getAll(String sessionId, Integer pageNumber)
  {
    JSONArray array = new JSONArray();

    PlatformQuery query = Platform.getAll();

    OIterator<? extends Platform> iterator = null;

    try
    {
      iterator = query.getIterator();

      while (iterator.hasNext())
      {
        array.put(iterator.next().toJSON());
      }
    }
    finally
    {
      if (iterator != null)
      {
        iterator.close();
      }
    }

    return array;
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, JSONObject json)
  {
    Platform sensor = Platform.fromJSON(json);
    sensor.apply();

    return sensor.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    Platform sensor = Platform.get(oid);

    if (sensor != null)
    {
      sensor.delete();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId, String oid)
  {
    return Platform.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject lock(String sessionId, String oid)
  {
    return Platform.lock(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject unlock(String sessionId, String oid)
  {
    return Platform.unlock(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject newInstance(String sessionId)
  {
    return new Platform().toJSON();
  }
}
