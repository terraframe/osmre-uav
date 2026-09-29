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
import java.io.OutputStream;
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
  public void export(String sessionId, OutputStream stream)
  {
    try (ZipOutputStream zipFile = new ZipOutputStream(stream))
    {
      ZipEntry entry = new ZipEntry("session-event-export.csv");
      zipFile.putNextEntry(entry);
      csvExportService.export(zipFile);
      zipFile.closeEntry();

      zipFile.finish();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

}
