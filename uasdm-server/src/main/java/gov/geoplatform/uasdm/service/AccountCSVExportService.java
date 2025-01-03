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
package gov.geoplatform.uasdm.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;

import gov.geoplatform.uasdm.SessionEventLog;
import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInfoPageQuery;
import gov.geoplatform.uasdm.Util;
import net.geoprism.GeoprismUser;


@Service
public class AccountCSVExportService
{
  public void export(OutputStream os)
  {
    try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(os)))
    {
      JSONArray results = query();
      
      if (results.length() < 1) return;
      
      String[] keys = new String[] { GeoprismUser.USERNAME, GeoprismUser.FIRSTNAME, GeoprismUser.LASTNAME, UserInfo.ORGANIZATION, GeoprismUser.EMAIL, GeoprismUser.PHONENUMBER, GeoprismUser.OID };
      
      writer.writeNext(keys);
      
      for (int i = 0; i < results.length(); ++i)
      {
        JSONObject result = results.getJSONObject(i);
        
        String[] line = new String[keys.length];
        
        for (int j = 0; j < keys.length; ++j)
        {
          if (result.has(keys[j]))
          {
            line[j] = result.getString(keys[j]);
          }
        }
        
        writer.writeNext(line);
      }
    }
    catch (IOException ex)
    {
      throw new ProgrammingErrorException(ex);
    }
  }
  
  private JSONArray query()
  {
    JSONObject criteria = new JSONObject();
    criteria.put("rows", -1);
    criteria.put("first", 0);
    
    JSONObject page = new UserInfoPageQuery(criteria).getPage();
    
    return page.getJSONArray("resultSet");
  }
}
