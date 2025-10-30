package gov.geoplatform.uasdm.service.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
            handleTusUpdate(userOid, uploadURI, uploadInfo);
          }
        });
        t.start();
      }
      else
      {
        // Update of create the workflow task
        Map<String, String> metadata = uploadInfo.getMetadata();
        String entityId = metadata.get("entityId");
        String uploadTarget = metadata.get("uploadTarget");

        this.wService.updateOrCreateUploadTask(userOid, uploadInfo.getId().toString(), entityId, uploadTarget);
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
  private void handleTusUpdate(String userOid, String uploadURI, UploadInfo uploadInfo)
  {
    try (InputStream is = this.tusFileUploadService.getUploadedBytes(uploadURI, userOid))
    {
      Map<String, String> metadata = uploadInfo.getMetadata();
      String filename = metadata.get("filename");

      // Handle the file
      this.pService.handleUploadFinish(userOid, uploadInfo.getId().toString(), filename, is);
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

  @Scheduled(fixedDelayString = "PT24H")
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

}
