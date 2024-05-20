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
package gov.geoplatform.uasdm.processing.gcp;

public class GroundControlPointFileInvalidFormatException extends RuntimeException
{

  private static final long serialVersionUID = -2508082579554640833L;

  private String reason;

  public GroundControlPointFileInvalidFormatException(String reason)
  {
    this.reason = reason;
  }

  public GroundControlPointFileInvalidFormatException(String reason, Throwable cause)
  {
    super(cause);
    this.reason = reason;
  }
  
  @Override
  public String getMessage()
  {
    return "Ground control point file did not match the expected file format. " + this.reason;
  }
  
}