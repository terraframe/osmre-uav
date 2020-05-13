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
package gov.geoplatform.uasdm.model;

import java.util.LinkedList;
import java.util.List;

import gov.geoplatform.uasdm.bus.UasComponentDeleteException;

public class CompositeDeleteException extends RuntimeException
{
  /**
   * 
   */
  private static final long                 serialVersionUID = -5117042171793804977L;

  private List<UasComponentDeleteException> exceptions;

  public CompositeDeleteException()
  {
    this.exceptions = new LinkedList<UasComponentDeleteException>();
  }

  public List<UasComponentDeleteException> getExceptions()
  {
    return exceptions;
  }

  public void add(UasComponentDeleteException exceptions)
  {
    this.exceptions.add(exceptions);
  }

  public void addAll(List<UasComponentDeleteException> exceptions)
  {
    this.exceptions.addAll(exceptions);
  }

  public boolean hasExceptions()
  {
    return this.exceptions.size() > 0;
  }

  public String toLabel()
  {
    StringBuilder label = new StringBuilder();

    for (UasComponentDeleteException e : this.exceptions)
    {
      label.append(e.getComponentName() + " [" + e.getTypeLabel() + "]");
    }

    return label.toString();
  }
}
