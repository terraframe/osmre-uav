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
package gov.geoplatform.uasdm.processing.report;

import java.util.ArrayDeque;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuedCollectionReportProcessor implements Runnable, CollectionReportProcessor
{
  public static final Logger logger = LoggerFactory.getLogger(QueuedCollectionReportProcessor.class);
  
  private static Thread   workerThread;
  
  private static Queue<CollectionReportTask> queue = new ArrayDeque<CollectionReportTask>(1000);

  private static Boolean  runThread = true;
  
  private static QueuedCollectionReportProcessor INSTANCE;
  
  public synchronized static QueuedCollectionReportProcessor getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new QueuedCollectionReportProcessor();
      
      startup();
    }
    
    return INSTANCE;
  }

  public synchronized static void startup()
  {
    if (workerThread != null)
    {
      return;
    }

    workerThread = new Thread(INSTANCE, "QueuedCollectionReportProcessor");
    workerThread.setDaemon(false);
    workerThread.start();

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        runThread = false;
      }
    }, "QueuedCollectionReportProcessor"));
  }
  
  public static void shutdown()
  {
    runThread = false;
  }

  @Override
  public void process(CollectionReportTask task)
  {
    queue.offer(task);
  }

  public void run()
  {
    while (runThread)
    {
      try
      {
        Thread.sleep(1000);
        
        CollectionReportTask crt = queue.poll();
        
        if (crt != null)
        {
          crt.run();
        }
      }
      catch (InterruptedException ex)
      {
        Thread.currentThread().interrupt();
        return;
      }
      catch (Exception e)
      {
        String errMsg = e.getMessage();
        logger.error(errMsg, e);
      }
    }
  }
}