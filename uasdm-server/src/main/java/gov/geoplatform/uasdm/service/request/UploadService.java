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
package gov.geoplatform.uasdm.service.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.service.WorkflowService;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;

@Service
public class UploadService
{
  private final Logger                   log = LoggerFactory.getLogger(UploadService.class);

  private final ProjectManagementService pService;

  private final WorkflowService          wService;

  private final TusFileUploadService     tusFileUploadService;

  public UploadService(TusFileUploadService tusFileUploadService, WorkflowService wService, ProjectManagementService pService)
  {
    this.tusFileUploadService = tusFileUploadService;
    this.wService = wService;
    this.pService = pService;
  }

  @Request(RequestType.SESSION)
  public void upload(String sessionId, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException
  {

    String userOid = Session.getCurrentSession().getUser().getOid();

    this.tusFileUploadService.process(servletRequest, servletResponse, userOid);

    String uploadURI = servletRequest.getRequestURI();

    final UploadInfo uploadInfo = getUploadInfo(userOid, uploadURI);

    if (uploadInfo != null)
    {
      // Upload is no longer in progress - its complete
      if (!uploadInfo.isUploadInProgress())
      {
        Thread t = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            handleTusComplete(userOid, uploadURI, uploadInfo);
          }
        });
        t.start();
      }
      else
      {
        // Ensure the task exists workflow task
        this.wService.updateOrCreateUploadTask(userOid, uploadInfo);
      }
    }
  }

  private UploadInfo getUploadInfo(String userOid, String uploadURI)
  {
    UploadInfo uploadInfo = null;
    try
    {
      uploadInfo = this.tusFileUploadService.getUploadInfo(uploadURI, userOid);
    }
    catch (IOException | TusException e)
    {
      log.error("get upload info", e);
    }
    return uploadInfo;
  }

  @Request
  private void handleTusComplete(String userOid, String uploadURI, UploadInfo uploadInfo)
  {
    // Ensure the task exists workflow task
    this.wService.updateOrCreateUploadTask(userOid, uploadInfo);

    try (InputStream is = this.tusFileUploadService.getUploadedBytes(uploadURI, userOid))
    {
      // Handle the file
      this.pService.handleUploadFinish(userOid, uploadInfo, is);
    }
    catch (IOException | TusException e)
    {
      log.error("get uploaded bytes", e);
    }
    finally
    {
      try
      {
        this.tusFileUploadService.deleteUpload(uploadURI, userOid);
      }
      catch (IOException | TusException e)
      {
        log.error("delete upload", e);
      }
    }
  }

  @Request(RequestType.SESSION)
  public Optional<JSONObject> getTask(String sessionId, String uploadUrl)
  {
    String userOid = Session.getCurrentSession().getUser().getOid();

    UploadInfo uploadInfo = this.getUploadInfo(userOid, uploadUrl);

    if (uploadInfo != null)
    {
      String uploadId = uploadInfo.getId().toString();

      AbstractUploadTask task = AbstractUploadTask.getTaskByUploadId(uploadId);

      if (task != null)
      {
        return Optional.of(task.toJSON());
      }
    }

    return Optional.empty();
  }

  @Request(RequestType.SESSION)
  public void removeUpload(String sessionId, String uploadUrl)
  {
    String userOid = Session.getCurrentSession().getUser().getOid();

    UploadInfo uploadInfo = this.getUploadInfo(userOid, uploadUrl);

    if (uploadInfo != null)
    {
      String uploadId = uploadInfo.getId().toString();

      AbstractUploadTask task = AbstractUploadTask.getTaskByUploadId(uploadId);

      if (task != null)
      {
        task.delete();
      }

      try
      {
        this.tusFileUploadService.deleteUpload(uploadUrl, userOid);
      }
      catch (IOException | TusException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  @Scheduled(fixedDelayString = "PT24H")
  // @Scheduled(fixedDelay = 1, initialDelay = 0, timeUnit = TimeUnit.MINUTES)
  private void cleanup()
  {
    // Path locksDir = this.tusUploadDirectory.resolve("locks");
    // if (Files.exists(locksDir))
    // {
    try
    {
      this.tusFileUploadService.cleanup();
    }
    catch (IOException e)
    {
      log.error("error during cleanup", e);
    }
    // }
  }

}
