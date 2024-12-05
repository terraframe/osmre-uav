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

import com.runwaysdk.dataaccess.transaction.Transaction;

public class CompositeComponent<T extends UasComponentIF>
{

  private T                             component;

  private List<ComponentWithAttributes> metadatas;

  public CompositeComponent(T component)
  {
    this.component = component;
    this.metadatas = new LinkedList<>();
  }

  public T getComponent()
  {
    return component;
  }

  public void setComponent(T component)
  {
    this.component = component;
  }

  public void addMetadata(ComponentWithAttributes metadata)
  {
    this.metadatas.add(metadata);
  }

  @Transaction
  public void apply()
  {
    this.component.apply();

    this.metadatas.forEach(metadata -> metadata.apply());
  }

  @Transaction
  public void applyWithParent(UasComponentIF parent)
  {
    this.component.applyWithParent(parent);

    this.metadatas.forEach(metadata -> metadata.apply());
  }
}
