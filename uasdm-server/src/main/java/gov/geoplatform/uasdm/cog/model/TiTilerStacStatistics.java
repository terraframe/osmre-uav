package gov.geoplatform.uasdm.cog.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class TiTilerStacStatistics
{
  private Map<String, TiTilerStacBandStatistic> assetBands;

  public TiTilerStacStatistics()
  {
    this.assetBands = new HashMap<>();
  }

  @JsonAnySetter
  public void addDynamicProperty(String key, TiTilerStacBandStatistic value)
  {
    this.assetBands.put(key, value);
  }

  public Map<String, TiTilerStacBandStatistic> getAssets()
  {
    return assetBands;
  }

  public void setAssets(Map<String, TiTilerStacBandStatistic> assets)
  {
    this.assetBands = assets;
  }

  public TiTilerStacBandStatistic getAssetBand(String asset)
  {
    return this.assetBands.get(asset);
  }

}
