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
package gov.geoplatform.uasdm;

public class CannotDeleteProcessingCollection extends CannotDeleteProcessingCollectionBase
{
  private static final long serialVersionUID = -1448335337;
  
  public CannotDeleteProcessingCollection()
  {
    super();
  }
  
  public CannotDeleteProcessingCollection(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public CannotDeleteProcessingCollection(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public CannotDeleteProcessingCollection(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
