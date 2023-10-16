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
