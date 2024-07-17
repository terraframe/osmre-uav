package gov.geoplatform.uasdm.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TileAccessControl
{

  private Map<String, Boolean> cache = Collections.synchronizedMap(new HashMap<String, Boolean>());

  public void setAccess(String path, Boolean access)
  {
    this.cache.put(path, access);
  }

  public boolean hasAccess(String path)
  {
    return cache.containsKey(path) && cache.get(path).booleanValue();
  }

  public boolean contains(String path)
  {
    return cache.containsKey(path);
  }

}
