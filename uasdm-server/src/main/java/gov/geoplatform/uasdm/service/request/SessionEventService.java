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
package gov.geoplatform.uasdm.service.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.SessionEventLog;
import gov.geoplatform.uasdm.service.SessionEventCSVExportService;

@Service
public class SessionEventService
{
  private Logger                       logger = LoggerFactory.getLogger(SessionEventService.class);

  @Autowired
  private SessionEventCSVExportService csvExportService;

  public static enum EventType {
    LOGIN_SUCCESS, LOGIN_FAILURE
  }

  @Request(RequestType.SESSION)
  public void logSuccessfulLogin(String sessionId, String username)
  {
    final SessionIF session = Session.getCurrentSession();
    final SingleActor user = (SingleActor) BusinessFacade.get(session.getUser());

    SessionEventLog.log(EventType.LOGIN_SUCCESS.name(), username, user.getOid());
  }

  @Request
  public void logFailureLogin(String username)
  {
    SessionEventLog.log(EventType.LOGIN_FAILURE.name(), username, null);
  }

  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, Integer pageNumber, Integer pageSize)
  {
    return SessionEventLog.page(pageNumber, pageSize).toJSON();
  }

  @Request(RequestType.SESSION)
  public InputStream export(String sessionId)
  {
    try
    {
      File file = File.createTempFile("session-event-export", ".csv");

      try (FileOutputStream fos = new FileOutputStream(file))
      {
        csvExportService.export(fos);
        fos.flush();

        // Zip up the entire contents of the file
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);

        Thread t = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              try (ZipOutputStream zipFile = new ZipOutputStream(pos))
              {
                ZipEntry entry = new ZipEntry(file.getName());
                zipFile.putNextEntry(entry);

                try (FileInputStream in = new FileInputStream(file))
                {
                  IOUtils.copy(in, zipFile);
                }
              }
              finally
              {
                pos.close();
              }

              FileUtils.deleteQuietly(file);
            }
            catch (IOException e)
            {
              logger.error("Error while writing the workbook", e);
            }
          }
        });
        t.setDaemon(true);
        t.start();

        return pis;
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

}
