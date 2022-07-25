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

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.JSONSerializable;
import gov.geoplatform.uasdm.model.Page;
import net.geoprism.GeoprismUser;

public abstract class AbstractMessage extends AbstractMessageBase implements JSONSerializable
{
  private static final long serialVersionUID = 605525039;

  public static final String MESSAGE = "message";

  public static final String DATA = "data";

  public AbstractMessage()
  {
    super();
  }

  public abstract String getMessage();

  public abstract JSONObject getData();

  public final JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(OID, this.getOid());
    object.put(MESSAGE, this.getMessage());
    object.put(TYPE, this.getClass().getSimpleName());
    object.put(DATA, this.getData());

    return object;
  }

  public static Page<AbstractMessage> getPage(Integer pageNumber, Integer pageSize)
  {
    AbstractMessageQuery query = new AbstractMessageQuery(new QueryFactory());
    query.WHERE(query.getGeoprismUser().EQ(GeoprismUser.getCurrentUser()));
    query.restrictRows(pageSize, pageNumber);

    try (OIterator<? extends AbstractMessage> iterator = query.getIterator())
    {
      return new Page<AbstractMessage>(query.getCount(), pageNumber, pageSize, new LinkedList<AbstractMessage>(iterator.getAll()));
    }
  }

}
