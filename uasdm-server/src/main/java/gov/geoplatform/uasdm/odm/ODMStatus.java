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
package gov.geoplatform.uasdm.odm;

public enum ODMStatus
{
  NEW("New", -1),
  QUEUED("Queued", 10),
  RUNNING("Running", 20),
  FAILED("Failed", 30),
  COMPLETED("Complete", 40),
  CANCELED("Canceled", 50);
  
  private String label;
  
  private Integer code;
  
  private ODMStatus(String label, Integer code)
  {
    this.label = label;
    this.code = code;
  }
  
  public String getLabel()
  {
    return this.label;
  }
  
  public Integer getCode()
  {
    return this.code;
  }
  
  public static ODMStatus getByCode(Integer code)
  {
    if (code == NEW.getCode() || code == null)
    {
      return NEW;
    }
    else if (code == QUEUED.getCode())
    {
      return QUEUED;
    }
    else if (code == RUNNING.getCode())
    {
      return RUNNING;
    }
    else if (code == FAILED.getCode())
    {
      return FAILED;
    }
    else if (code == COMPLETED.getCode())
    {
      return COMPLETED;
    }
    else if (code == CANCELED.getCode())
    {
      return CANCELED;
    }
    
    return null;
  }
}
