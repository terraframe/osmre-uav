/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.runwaysdk.business.graph.VertexObject;

import gov.geoplatform.uasdm.graph.RawSet;

public class ComponentRawSet
{
  private UasComponentIF     component;

  private Collection<RawSet> sets;

  public ComponentRawSet(UasComponentIF component)
  {
    this.component = component;
    this.sets = new TreeSet<RawSet>((RawSet p1, RawSet p2) -> p1.getName().compareTo(p2.getName()));
  }

  public UasComponentIF getComponent()
  {
    return component;
  }

  public void addRawSet(RawSet set)
  {
    this.sets.add(set);
  }

  public Collection<RawSet> getRawSets()
  {
    return sets;
  }

  public boolean isEmpty()
  {
    return this.sets.isEmpty();
  }

  public static List<ComponentRawSet> process(List<VertexObject> results)
  {
    List<ComponentRawSet> dtos = new LinkedList<ComponentRawSet>();

    ComponentRawSet current = null;

    for (VertexObject result : results)
    {
      if (result instanceof UasComponentIF)
      {
        current = new ComponentRawSet((UasComponentIF) result);

        dtos.add(current);
      }
      else if (current != null)
      {
        current.addRawSet((RawSet) result);
      }
    }

    return dtos.stream().filter(v -> !v.isEmpty()).collect(Collectors.toList());
  }
}
