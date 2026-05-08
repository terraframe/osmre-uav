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
package gov.geoplatform.uasdm.maintenance;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.CollectionStatusQuery;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery;
import gov.geoplatform.uasdm.bus.OrthoProcessingTask;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.odm.ODMUploadTask;
import gov.geoplatform.uasdm.service.WorkflowService;

@Component
public class LingeringWorkflowTaskCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(LingeringWorkflowTaskCleanupJob.class);
    
    public static void main(String[] args)
    {
      new LingeringWorkflowTaskCleanupJob().cleanUp();
    }

    /**
     * Runs once a day at 04:00.
     * Cron format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanUp() {
      try {
        inRequest();
      } catch (Throwable t) {
        logger.error("Unhandled exception thrown during cleanup", t);
      }
    }
    
    @Request
    private void inRequest() {
      cleanupCollectionStatus();
      cleanupWorkflowTasks();
    }
    
    // More than x days old? And still processing? Yeah, no.
    private void cleanupWorkflowTasks() {
      AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
      query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel())).OR(query.getStatus().EQ("Processing")).OR(query.getStatus().EQ(WorkflowTaskStatus.STARTED.toString()))));
      
      try (OIterator<? extends AbstractWorkflowTask> it = query.getIterator())
      {
        while (it.hasNext())
        {
          AbstractWorkflowTask staleEntity = it.next();
          
          String taskOid = staleEntity.getOid();
          Date taskLastUpdate = staleEntity.getLastUpdateDate();
          
          try {
            int expireDays = 7;
            if (staleEntity instanceof ODMProcessingTask || (staleEntity.getStatus().equals(WorkflowTaskStatus.STARTED.toString()) && staleEntity.getMessage().toLowerCase().contains(WorkflowService.FINALIZING_UPLOAD_MESSAGE.toLowerCase())))
              expireDays = 1;
            else if (staleEntity instanceof ODMUploadTask || staleEntity instanceof OrthoProcessingTask)
              expireDays = 3;
            
            if (taskLastUpdate != null && taskLastUpdate.toInstant().isBefore(Instant.now().minus(expireDays, ChronoUnit.DAYS))) {
              logger.error("Workflow task [" + taskOid + "] has been processing too long without updates [" + expireDays + " days]. Failing task.");
              AbstractWorkflowTask task = AbstractWorkflowTask.lock(taskOid);
              task.setStatus(ODMStatus.FAILED.getLabel());
              task.setMessage("The task timed out (was processing for too long with no updates). Contact your technical support.");
              task.apply();
            }
          } catch(Throwable t) {
            logger.error("Unexpected exception encountered while cleaning up workflow tasks on record [" + taskOid + "]", t);
          }
        }
      }
    }
    
    private void cleanupCollectionStatus() {
      CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());
      query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel())).OR(query.getStatus().EQ("Processing"))));
      
      try (OIterator<? extends CollectionStatus> it = query.getIterator())
      {
        while (it.hasNext())
        {
          CollectionStatus staleEntity = it.next();
          
          String oid = staleEntity.getOid();
          String componentId = staleEntity.getComponent();
          String productId = staleEntity.getProductId();
          
          try {
            int updated = CollectionStatus.updateStatus(componentId, productId);
            
            if (updated == 0) {
              // No associated tasks... Lingering status object?
              
              var col = UasComponent.get(componentId);
              CollectionStatus colStat = CollectionStatus.lock(oid);
              
              if (col == null) {
                logger.error("No associated tasks for CollectionStatus object, and referencing collection does not exist! Deleting lingering CollectionStatus [" + colStat.getOid() + "] object.");
                colStat.delete();
              } else {
                logger.error("No associated tasks for CollectionStatus object [" + col.getS3location() + "]! Updating status of CollectionStatus object [" + colStat.getOid() + "] to Failed.");
                colStat.setStatus(ODMStatus.FAILED.getLabel());
                colStat.apply();
              }
            }
          } catch (Throwable t) {
            logger.error("Unexpected exception encountered while cleaning up collection status on record [" + oid + "]", t);
          }
        }
      }
    }
}
