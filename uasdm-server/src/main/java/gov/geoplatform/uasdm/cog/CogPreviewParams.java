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
package gov.geoplatform.uasdm.cog;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CogPreviewParams
{
  private Integer max_size;
  
  private Integer width;
  
  private Integer height;
  
  public CogPreviewParams()
  {
    
  }
  
  public CogPreviewParams(int width, int height)
  {
    this.width = width;
    this.height = height;
  }

  public int getMax_size()
  {
    return max_size;
  }

  public void setMax_size(int max_size)
  {
    this.max_size = max_size;
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }
  
  public void addParameters(Map<String, List<String>> parameters)
  {
    if (this.height != null)
    {
      parameters.put("height", Arrays.asList(String.valueOf(height)));
    }
    
    if (this.width != null)
    {
      parameters.put("width", Arrays.asList(String.valueOf(width)));
    }
    
    if (this.max_size!= null)
    {
      parameters.put("max_size", Arrays.asList(String.valueOf(max_size)));
    }
  }
}
