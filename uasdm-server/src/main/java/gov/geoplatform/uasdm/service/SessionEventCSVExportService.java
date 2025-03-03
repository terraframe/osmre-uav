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
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.SessionEventLog;
import gov.geoplatform.uasdm.SessionEventLogQuery;
import gov.geoplatform.uasdm.Util;


@Service
public class SessionEventCSVExportService
{
  public void export(OutputStream os)
  {
    try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(os)))
    {
      writer.writeNext(new String[] {
          SessionEventLog.USERNAME, SessionEventLog.EVENTTYPE, SessionEventLog.EVENTDATE, SessionEventLog.ORGANIZATION
      });
      
      try (OIterator<? extends SessionEventLog> it = query())
      {
        while (it.hasNext())
        {
          SessionEventLog log = it.next();
          
          String[] line = toCSV(log);
          
          writer.writeNext(line);
        }
      }
    }
    catch (IOException ex)
    {
      throw new ProgrammingErrorException(ex);
    }
  }
  
  public String[] toCSV(SessionEventLog log)
  {
    final String[] object = new String[4];
    
    object[0] = log.getUsername();
    object[1] = log.getEventType();
    object[2] = Util.formatIso8601(log.getEventDate(), true);

    if (!StringUtils.isBlank(log.getOrganizationOid()))
    {
      object[3] = log.getOrganization().getDisplayLabel().getValue();
    }

    return object;
  }
  
  private OIterator<? extends SessionEventLog> query()
  {
    SessionEventLogQuery query = new SessionEventLogQuery(new QueryFactory());
    
    return query.getIterator();
  }
}
