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
