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

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.bus.AbstractMessage;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;
import gov.geoplatform.uasdm.model.MetadataMessage;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import me.desair.tus.server.upload.UploadInfo;

@Service
public class WorkflowService
{
  final Logger log = LoggerFactory.getLogger(WorkflowService.class);

  @Transaction
  public JSONObject updateUploadTaskInTransaction(String uploadId)
  {
    AbstractWorkflowTask task = ImageryWorkflowTaskIF.getWorkflowTaskForUpload(uploadId);

    if (task != null)
    {
      task.lock();
      task.setStatus(WorkflowTaskStatus.UPLOADING.toString());
      task.setMessage("Uploading to the staging environment..."); // parser.getPercent()
      // + "%
      // complete"
      task.apply();
    }

    JSONObject message = new JSONObject();
    message.put("currentTask", task.toJSON());

    return message;
  }

  @Transaction
  public void updateOrCreateUploadTask(String userOid, UploadInfo uploadInfo)
  {
    String uploadId = uploadInfo.getId().toString();

    AbstractWorkflowTask task = ImageryWorkflowTaskIF.getWorkflowTaskForUpload(uploadId);

    if (task == null)
    {
      Map<String, String> metadata = uploadInfo.getMetadata();
      ProcessConfiguration configuration = ProcessConfiguration.parse(uploadInfo);

      String componentId = metadata.get("componentId");
      String uploadTarget = metadata.get("uploadTarget");

      UasComponentIF uasComponent = ComponentFacade.getComponent(componentId);

      task = uasComponent.createWorkflowTask(userOid, uploadId, uploadTarget);
      task.setDescription(metadata.getOrDefault("description", null));
      task.setTool(metadata.getOrDefault("tool", null));
      task.setProjectionName(metadata.getOrDefault("projectionName", null));
      task.setOrthoCorrectionModel(metadata.getOrDefault("orthoCorrectionModel", null));
      task.setProductName(configuration.getProductName());

      if (metadata.containsKey("ptEpsg"))
      {
        task.setPtEpsg(Integer.valueOf(metadata.get("ptEpsg")));
      }

      if (task instanceof AbstractUploadTask)
      {
        AbstractUploadTask uploadTask = (AbstractUploadTask) task;
        uploadTask.setConfiguration(configuration);
      }

      if (task instanceof WorkflowTask && configuration.isODM())
      {
        WorkflowTask workflowTask = (WorkflowTask) task;

        workflowTask.setProcessDem(configuration.toODM().getProcessDem());
        workflowTask.setProcessOrtho(configuration.toODM().getProcessOrtho());
        workflowTask.setProcessPtcloud(configuration.toODM().getProcessPtcloud());
      }
    }
    else
    {
      task.lock();
    }

    if (task.getStatus().length() == 0)
    {
      task.setStatus(WorkflowTaskStatus.STARTED.toString());
    }

    task.setMessage("Uploading to the staging environment...");
    task.apply();
  }

  @Request(RequestType.SESSION)
  public JSONObject getTasks(String sessionId, String statuses, Integer pageNumber, Integer pageSize, Integer token)
  {
    Page<CollectionStatus> page = CollectionStatus.getUserWorkflowTasks(statuses, pageNumber, pageSize);
    page.addParam("token", token);

    return page.toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject getTasksCount(String sessionId, String status)
  {
    long metadataCount = ComponentFacade.getMissingMetadataCount();
    long count = WorkflowTask.getUserWorkflowTasksCount(status);

    JSONObject response = new JSONObject();
    response.put("tasksCount", count + metadataCount);

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONArray getComponentTasks(String sessionId, String componentId, String productId)
  {
    return WorkflowTask.getComponentTasks(componentId, productId);
  }

  @Request(RequestType.SESSION)
  public JSONObject getMessages(String sessionId, Integer pageNumber, Integer pageSize)
  {
    Page<AbstractMessage> page = AbstractMessage.getPage(pageNumber, pageSize);

    return page.toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject getMissingMetadata(String sessionId, Integer pageNumber, Integer pageSize)
  {
    Page<MetadataMessage> page = ComponentFacade.getMissingMetadata(pageNumber, pageSize);

    return page.toJSON();
  }

  @Request(RequestType.SESSION)
  public JSONObject getTask(String sessionId, String id)
  {
    JSONObject response = new JSONObject();
    response.put("task", AbstractWorkflowTask.get(id).toJSON());

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONObject getUploadTask(String sessionId, String uploadId)
  {
    AbstractUploadTask task = AbstractUploadTask.getTaskByUploadId(uploadId);

    return task.toJSON();
  }

  @Request(RequestType.SESSION)
  public void cancel(String sessionId, String uploadId)
  {
    AbstractUploadTask task = AbstractUploadTask.getTaskByUploadId(uploadId);

    if (task != null)
    {
      task.appLock();
      task.setStatus("Failed");
      task.apply();
    }
  }

}
