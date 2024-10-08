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
package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.view.Option;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;

public class Bureau extends BureauBase
{
  private static final long  serialVersionUID = 806603996;

  public static final String OTHER            = "OTHER";

  public Bureau()
  {
    super();
  }

  @Override
  public void apply()
  {
    boolean isNew = this.isNew();

    super.apply();

    if (isNew)
    {
      gov.geoplatform.uasdm.graph.Bureau.create(this);
    }
    else
    {
      gov.geoplatform.uasdm.graph.Bureau.update(this);
    }
  }

  public static List<Option> getOptions()
  {
    List<Option> options = new LinkedList<Option>();

    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel().localize());

    try (OIterator<? extends Organization> it = query.getIterator())
    {
      List<? extends Organization> bureaus = it.getAll();

      for (Organization bureau : bureaus)
      {
        options.add(new Option(bureau.getOid(), bureau.getDisplayLabel().getValue()));
      }

      return options;
    }
  }

}
