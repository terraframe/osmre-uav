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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class QueuedCollectionReportProcessor implements Runnable, CollectionReportProcessor
{
  private static Thread   workerThread;
  
  private static BlockingQueue<CollectionReportTask> queue = new ArrayBlockingQueue<CollectionReportTask>(1000);

  private static Boolean  runThread = true;
  
  public QueuedCollectionReportProcessor()
  {
    startWorkerThread(this);
  }

  public synchronized static void startWorkerThread(QueuedCollectionReportProcessor processor)
  {
    if (workerThread != null)
    {
      return;
    }

    workerThread = new Thread(processor, "QueuedCollectionReportProcessor");
    workerThread.setDaemon(true);
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
        
        CollectionReportTask crt = queue.poll(4000, TimeUnit.MILLISECONDS);
        
        if (crt != null)
        {
          crt.run();
        }
      }
      catch (Exception e)
      {
        String errMsg = e.getMessage();
        throw new ProgrammingErrorException(errMsg);
      }
    }
  }
}