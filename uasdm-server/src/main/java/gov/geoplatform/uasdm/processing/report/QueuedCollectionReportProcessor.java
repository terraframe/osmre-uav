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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuedCollectionReportProcessor implements CollectionReportProcessor
{
  private static final Logger                 logger    = LoggerFactory.getLogger(QueuedCollectionReportProcessor.class);

  private ExecutorService                     executor;

  private ExecutorService                     scheduler = Executors.newFixedThreadPool(1);

  private BlockingQueue<CollectionReportTask> queue;

  public QueuedCollectionReportProcessor()
  {
    this.executor = Executors.newFixedThreadPool(1);
    this.queue = new ArrayBlockingQueue<CollectionReportTask>(1000);

    this.scheduler.execute(() -> {
      while (true)
      {
        try
        {
          executor.execute(queue.take());
        }
        catch (Throwable e)
        {
          logger.error("Error processing the report task", e);
        }
      }
    });
  }

  @Override
  public void process(CollectionReportTask task)
  {
    this.queue.offer(task);
  }

}
