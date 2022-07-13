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

public class UnexpectedODMResponseException extends RuntimeException
{

  public static final String MESSAGE = "Unexpected response from ODM [{odm_resp}]";
  
  private String resp;
  
  public UnexpectedODMResponseException(String resp)
  {
    super();
    this.resp = resp;
  }
  
  public UnexpectedODMResponseException(String resp, Throwable cause)
  {
    super(cause);
    this.resp = resp;
  }
  
  @Override
  public String getMessage()
  {
    return MESSAGE.replaceFirst("\\{odm_resp\\}", this.resp == null ? "" : this.resp);
  }
  
}
