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
package gov.geoplatform.uasdm.service.business;

import java.util.LinkedList;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.AttributeLocal;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableChar;

import gov.geoplatform.uasdm.OrganizationSynchronization;
import gov.geoplatform.uasdm.OrganizationSynchronizationQuery;
import net.geoprism.registry.view.Page;

@Service
public class OrganizationSynchronizationBusinessService
{

  @Transaction
  public void delete(OrganizationSynchronization synchronization)
  {
    synchronization.delete();

  }

  public void execute(OrganizationSynchronization synchronization)
  {
    synchronization.execute();
  }

  public OrganizationSynchronization fromJSON(JsonObject object)
  {
    OrganizationSynchronization list = null;

    if (object.has("oid") && !object.get("oid").isJsonNull())
    {
      String oid = object.get("oid").getAsString();

      list = OrganizationSynchronization.lock(oid);
    }
    else
    {
      list = new OrganizationSynchronization();
    }

    list.parse(object);

    return list;
  }

  @Transaction
  public OrganizationSynchronization apply(JsonObject object)
  {
    OrganizationSynchronization list = this.fromJSON(object);
    list.apply();

    return list;
  }

  public JsonArray getAll()
  {
    OrganizationSynchronizationQuery query = new OrganizationSynchronizationQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getUrl());

    JsonArray array = new JsonArray();

    try (OIterator<? extends OrganizationSynchronization> iterator = query.getIterator())
    {
      iterator.forEach(i -> array.add(i.toJSON()));
    }

    return array;
  }

  public Page<OrganizationSynchronization> page(JsonObject criteria)
  {
    OrganizationSynchronizationQuery query = new OrganizationSynchronizationQuery(new QueryFactory());
    int pageSize = 10;
    int pageNumber = 1;

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.get("first").getAsInt();
      pageSize = criteria.get("rows").getAsInt();
      pageNumber = ( first / pageSize ) + 1;

      query.restrictRows(pageSize, pageNumber);
    }

    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.get("sortField").getAsString();
      SortOrder order = criteria.get("sortOrder").getAsInt() == 1 ? SortOrder.ASC : SortOrder.DESC;

      query.ORDER_BY(query.getS(field), order);
    }
    else if (criteria.has("multiSortMeta"))
    {
      JsonArray sorts = criteria.get("multiSortMeta").getAsJsonArray();

      for (int i = 0; i < sorts.size(); i++)
      {
        JsonObject sort = sorts.get(i).getAsJsonObject();

        String field = sort.get("field").getAsString();
        SortOrder order = sort.get("order").getAsInt() == 1 ? SortOrder.ASC : SortOrder.DESC;

        query.ORDER_BY(query.getS(field), order);
      }
    }

    if (criteria.has("filters"))
    {
      JsonObject filters = criteria.get("filters").getAsJsonObject();
      Set<String> keys = filters.keySet();

      for (String attributeName : keys)
      {
        Selectable attribute = query.get(attributeName);

        if (attribute != null)
        {
          JsonObject filter = filters.get(attributeName).getAsJsonObject();

          String value = filter.get("value").getAsString();
          String mode = filter.get("matchMode").getAsString();

          if (mode.equals("contains"))
          {
            if (attribute instanceof AttributeLocal)
            {
              query.WHERE( ( (AttributeLocal) attribute ).localize().LIKEi("%" + value + "%"));
            }
            else
            {
              SelectableChar selectable = (SelectableChar) attribute;
              query.WHERE(selectable.LIKEi("%" + value + "%"));
            }
          }
          else if (mode.equals("equals"))
          {
            if (attribute instanceof AttributeLocal)
            {
              query.WHERE( ( (AttributeLocal) attribute ).localize().EQ(value));
            }
            else
            {
              query.WHERE(attribute.EQ(value));
            }
          }
        }
      }
    }

    long count = query.getCount();

    try (OIterator<? extends OrganizationSynchronization> iterator = query.getIterator())
    {
      return new Page<OrganizationSynchronization>(count, pageNumber, pageSize, new LinkedList<OrganizationSynchronization>(iterator.getAll()));
    }
  }

  public OrganizationSynchronization get(String oid)
  {
    return OrganizationSynchronization.get(oid);
  }

}
