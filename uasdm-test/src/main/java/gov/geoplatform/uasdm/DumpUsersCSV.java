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

import java.util.ArrayList;
import java.util.List;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;

public class DumpUsersCSV
{
  public static void main(String[] args)
  {
    inReq();
  }
  
  @Request
  public static void inReq()
  {
    GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
    
    List<String> lines = new ArrayList<String>();
    
    for (GeoprismUser user : query.getIterator())
    {
      lines.add(user.getUsername() + "," + user.getUsername() + "," + user.getEmail());
    }
    
    for (String line : lines)
    {
      System.out.println(line);
    }
  }
}
