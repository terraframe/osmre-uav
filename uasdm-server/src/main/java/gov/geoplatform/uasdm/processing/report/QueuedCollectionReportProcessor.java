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
package gov.geoplatform.uasdm.processing.report;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuedCollectionReportProcessor implements CollectionReportProcessor
{
  private static final Logger                 logger = LoggerFactory.getLogger(QueuedCollectionReportProcessor.class);

  private ExecutorService                     executor;

  private ExecutorService                     scheduler;

  private BlockingQueue<CollectionReportTask> queue;

  public QueuedCollectionReportProcessor()
  {
    this.scheduler = Executors.newFixedThreadPool(1, new ThreadFactory()
    {
      @Override
      public Thread newThread(Runnable r)
      {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("report-queue");
        thread.setDaemon(true);

        return thread;
      }
    });
    this.executor = Executors.newFixedThreadPool(1);
    this.queue = new ArrayBlockingQueue<CollectionReportTask>(1000);

    this.scheduler.execute(() -> {
      while (true)
      {
        try
        {
          CollectionReportTask task = queue.take();

          executor.execute(task);
        }
        catch (Throwable e)
        {
          logger.error("Error processing the report task", e);
        }
      }
    });

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        QueuedCollectionReportProcessor.this.shutdown();
      }
    }, "report-processor-shutdown"));
  }

  @Override
  public void process(CollectionReportTask task)
  {
    this.queue.offer(task);
  }

  @Override
  public void shutdown()
  {
    executor.shutdown(); // Disable new tasks from being submitted
    try
    {
      // Wait a while for existing tasks to terminate
      if (!executor.awaitTermination(60, TimeUnit.SECONDS))
      {
        // Cancel currently executing tasks
        executor.shutdownNow();

        // Wait a while for tasks to respond to being cancelled
        if (!executor.awaitTermination(60, TimeUnit.SECONDS))
        {
          System.err.println("Pool did not terminate");
        }
      }
    }
    catch (InterruptedException ie)
    {
      // (Re-)Cancel if current thread also interrupted
      executor.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

}