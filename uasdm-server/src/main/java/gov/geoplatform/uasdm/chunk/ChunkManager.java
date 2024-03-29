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
package gov.geoplatform.uasdm.chunk;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;

public class ChunkManager implements Runnable
{
  private static final Logger                      logger           = LoggerFactory.getLogger(ChunkManager.class);

  /**
   * Executer responsible for running the cleanup thread
   */
  private static final ScheduledExecutorService executor      = Executors.newSingleThreadScheduledExecutor();

  /**
   * Interval time in hours
   */
  public static final long                      INTERVAL_TIME = 1;

  @Override
  public void run()
  {
    try
    {
      Calendar instance = Calendar.getInstance();
      instance.setTime(new Date());
      instance.add(Calendar.DATE, ( -1 * AppProperties.getChunkExpireTime() ));

      File directory = AppProperties.getUploadDirectory();

      File[] files = directory.listFiles();

      if (files != null)
      {
        for (File file : files)
        {
          if (file.exists() && file.isDirectory())
          {
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTime(new Date(file.lastModified()));

            if (instance.after(lastModified))
            {
              FileUtils.deleteQuietly(file);
            }

          }
        }
      }
      
      runInReq();
    }
    catch (Exception e)
    {
      logger.error("Chunk manager error", e);
    }
  }
  
  @Request
  public void runInReq()
  {
    // Since we're removing chunks that are a certain age, we need to also remove the workflow task
    
    Calendar instance = Calendar.getInstance();
    instance.setTime(new Date());
    instance.add(Calendar.DATE, ( -1 * AppProperties.getChunkExpireTime() ));
    
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getStatus().EQ(WorkflowTaskStatus.UPLOADING.toString()));
    query.WHERE(query.getLastUpdateDate().LE(instance.getTime()));
    OIterator<? extends WorkflowTask> it = query.getIterator();
    
    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public static void start()
  {
    executor.scheduleWithFixedDelay(new ChunkManager(), 0, INTERVAL_TIME, TimeUnit.HOURS);
  }

  public static void stop()
  {
    executor.shutdown();
  }
}
