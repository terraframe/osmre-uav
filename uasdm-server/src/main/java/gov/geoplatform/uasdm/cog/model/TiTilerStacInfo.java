/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
