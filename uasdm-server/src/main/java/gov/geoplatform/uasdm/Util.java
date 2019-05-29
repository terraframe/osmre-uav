package gov.geoplatform.uasdm;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.odm.ODMStatusServer;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class Util
{
  private static Logger logger = LoggerFactory.getLogger(ODMStatusServer.class);
  
  public static void uploadFileToS3(File child, String key, AbstractWorkflowTaskIF task)
  {
    try
    {
      BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
      TransferManager tx = new TransferManager(new StaticCredentialsProvider(awsCreds));

      try
      {
        Upload myUpload = tx.upload(AppProperties.getBucketName(), key, child);

        if (myUpload.isDone() == false)
        {
          logger.info("Source: " + child.getAbsolutePath());
          logger.info("Destination: " + myUpload.getDescription());
          
          if (task != null)
          {
            task.lock();
            task.setMessage(myUpload.getDescription());
            task.apply();
          }
        }

        myUpload.addProgressListener(new ProgressListener()
        {
          int count = 0;

          @Override
          public void progressChanged(ProgressEvent progressEvent)
          {
            if (count % 2000 == 0)
            {
              long total = myUpload.getProgress().getTotalBytesToTransfer();
              long current = myUpload.getProgress().getBytesTransferred();

              logger.info(current + "/" + total + "-" + ( (int) ( (double) current / total * 100 ) ) + "%");

              count = 0;
            }

            count++;
          }
        });

        myUpload.waitForCompletion();
      }
      finally
      {
        tx.shutdownNow();
      }
    }
    catch (Exception e)
    {
      if (task != null)
      {
        task.createAction(e.getMessage(), "error");
      }
      logger.error("Exception occured while uploading [" + key + "].", e);
    }
  }
}
