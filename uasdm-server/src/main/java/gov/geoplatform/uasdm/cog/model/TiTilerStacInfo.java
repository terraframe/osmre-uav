package gov.geoplatform.uasdm.cog.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class TiTilerStacInfo
{
  private Map<String, TiTilerStacAssetInfo> assets;

  public TiTilerStacInfo()
  {
    this.assets = new HashMap<>();
  }

  @JsonAnySetter
  public void addDynamicProperty(String key, TiTilerStacAssetInfo value)
  {
    this.assets.put(key, value);
  }

  public Map<String, TiTilerStacAssetInfo> getAssets()
  {
    return assets;
  }

  public void setAssets(Map<String, TiTilerStacAssetInfo> assets)
  {
    this.assets = assets;
  }

  public TiTilerStacAssetInfo getAsset(String asset)
  {
    return this.assets.get(asset);
  }

}
