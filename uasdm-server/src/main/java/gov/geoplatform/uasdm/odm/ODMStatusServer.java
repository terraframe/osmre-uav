package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.DevProperties;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery;
import gov.geoplatform.uasdm.model.AbstractWorkflowTaskIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.SolrService;
import net.geoprism.EmailSetting;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ODMStatusServer
{
  private static final Integer               ODM_STATUS_UPDATE_INTERVAL = 5000;                                          // in
                                                                                                                         // miliseconds

  private static ODMStatusThread             statusThread;

  private static List<S3ResultsUploadThread> uploadThreads              = new ArrayList<S3ResultsUploadThread>();

  private static Logger                      logger                     = LoggerFactory.getLogger(ODMStatusServer.class);

  public synchronized static void startup()
  {
    if (statusThread != null)
    {
      return;
    }

    statusThread = new ODMStatusThread("ODM Status Server");
    statusThread.setDaemon(true);

    restartRunningJobs();

    statusThread.start();
  }

  public synchronized static void shutdown()
  {
    if (statusThread == null)
    {
      return;
    }

    statusThread.interrupt();

    for (Thread t : uploadThreads)
    {
      if (t != null)
      {
        t.interrupt();
      }
    }
  }

  // ODMProcessingTaskIF
  // AbstractWorkflowTask
  private static void sendEmail(AbstractWorkflowTaskIF task)
  {
    try
    {
      if (task.getStatus().equals(ODMStatus.FAILED.getLabel()))
      {
        final String subject = "Orthorectification Processing Failed";

        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has failed. " + task.getMessage();

        EmailSetting.sendEmail(subject, body, new String[] { task.getGeoprismUser().getEmail() });
      }
      else if (task.getStatus().equals(ODMStatus.COMPLETED.getLabel()))
      {
        final String subject = "Orthorectification Processing Completed";

        final String body = "The orthorectification processing for [" + task.getComponentLabel() + "] has completed. Log into the UASDM again to see the results.";

        EmailSetting.sendEmail(subject, body, new String[] { task.getGeoprismUser().getEmail() });
      }
      else
      {
        logger.error("Unable to send email of unknown task status [" + task.getStatus() + "].");
      }
    }
    catch (Throwable t)
    {
      logger.error("Problem sending email for task [" + task.getTaskLabel() + "] with status [" + task.getStatus() + "].", t);

      task.createAction("Problem occured while sending email. " + t.getLocalizedMessage(), "error");
    }
  }

  /**
   * Subscribes a task for polling updates from the ODM processing server.
   * 
   * @param task
   */
  public static void addTask(ODMProcessingTaskIF task)
  {
    statusThread.addTask(task);
  }

  private static void restartRunningJobs()
  {
    AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getStatus().EQ(ODMStatus.RUNNING.getLabel()).OR(query.getStatus().EQ(ODMStatus.QUEUED.getLabel()).OR(query.getStatus().EQ(ODMStatus.NEW.getLabel())).OR(query.getStatus().EQ("Processing"))));

    try (OIterator<? extends AbstractWorkflowTask> it = query.getIterator())
    {
      while (it.hasNext())
      {
        AbstractWorkflowTask task = it.next();

        // Heads up: Imagery
        if (task instanceof ODMProcessingTaskIF)
        {
          ODMProcessingTaskIF processingTask = (ODMProcessingTaskIF) task;

          statusThread.addTask(processingTask);
        }
        else if (task instanceof ODMUploadTask)
        {
          ODMUploadTask uploadTask = (ODMUploadTask) task;

          S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
          thread.setDaemon(true);
          uploadThreads.add(thread);
          thread.start();
        }
        else
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage("The server was shut down while the task was running.");
          task.apply();
        }
      }
    }
  }

  private static void removeFromOdm(AbstractWorkflowTaskIF task, String uuid)
  {
    try
    {
      TaskRemoveResponse resp = ODMFacade.taskRemove(uuid);

      if (!resp.isSuccess())
      {
        int code = resp.getHTTPResponse().getStatusCode();
        logger.error("Error occurred while removing task [" + uuid + "] [" + task.getTaskLabel() + "] from ODM. ODM returned status code [" + code + "]. " + code);
        task.createAction("Error occurred while cleaning up data from ODM. ODM returned status code [" + code + "].", "error");
      }
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while removing task [" + uuid + "] [" + task.getTaskLabel() + "] from ODM.", t);

      task.createAction("Problem occured while cleaning up data from ODM. " + t.getLocalizedMessage(), "error");
    }
  }

  private static class ODMStatusThread extends Thread
  {
    /**
     * A list of tasks that require status updates.
     */
    private List<ODMProcessingTaskIF> activeTasks  = new ArrayList<ODMProcessingTaskIF>();

    /**
     * A way to add additional tasks that require status updates to our list.
     */
    private List<ODMProcessingTaskIF> pendingTasks = new ArrayList<ODMProcessingTaskIF>();

    protected ODMStatusThread(String name)
    {
      super(name);
    }

    protected void addTask(ODMProcessingTaskIF task)
    {
      synchronized (pendingTasks)
      {
        pendingTasks.add(task);
      }
    }

    @Override
    public void run()
    {
      while (!Thread.interrupted())
      {
        try
        {
          Thread.sleep(ODM_STATUS_UPDATE_INTERVAL);

          runInRequest();
        }
        catch (Throwable t)
        {
          if (t instanceof InterruptedException)
          {
            return;
          }

          logger.error("ODM status server encountered an error!", t);
        }
      }
    }

    @Request
    public void runInRequest()
    {
      runInTrans();
    }

//    @Transaction
    public void runInTrans()
    {
      synchronized (pendingTasks)
      {
        activeTasks.addAll(pendingTasks);
        pendingTasks.clear();
      }

      Iterator<ODMProcessingTaskIF> it = activeTasks.iterator();

      while (it.hasNext())
      {
        if (Thread.interrupted())
        {
          Thread.currentThread().interrupt();
          return;
        }

        ODMProcessingTaskIF task = it.next();

        InfoResponse resp;
        if (DevProperties.runOrtho())
        {
          resp = ODMFacade.taskInfo(task.getOdmUUID());
        }
        else
        {
          resp = DevProperties.getMockOdmTaskInfo();
        }

        if (resp.getHTTPResponse().isUnreachableHost())
        {
          String msg = "Unable to reach ODM server. code: " + resp.getHTTPResponse().getStatusCode() + " response: " + resp.getHTTPResponse().getResponse();
          logger.error(msg);
          UnreachableHostException ex = new UnreachableHostException(msg);

          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage(ex.getLocalizedMessage());
          task.apply();

          it.remove();

          sendEmail(task);

//          throw ex;
          continue;
        }
        else if (resp.hasError())
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage(resp.getError());
          task.apply();

          it.remove();

          sendEmail(task);
        }
        else if (!resp.hasError() && resp.getHTTPResponse().isError())
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage("The job encountered an unspecified error.");
          task.apply();

          task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse());

          it.remove();
        }
        else
        {
          if (resp.getStatus().equals(ODMStatus.RUNNING))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());

            long millis = resp.getProcessingTime();

            String sProcessingTime = String.format("%d hours, %d minutes", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));

            task.setMessage("Processing of " + resp.getImagesCount() + " images has been running for " + sProcessingTime + ". An email will be sent when the processing is complete");
            task.apply();
          }
          else if (resp.getStatus().equals(ODMStatus.QUEUED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage("Job is queued."); // TODO : Position in queue?
            task.apply();
          }
          else if (resp.getStatus().equals(ODMStatus.CANCELED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage("Job was canceled.");
            task.apply();

            it.remove();

            // TODO : Remove from ODM?
          }
          else if (resp.getStatus().equals(ODMStatus.FAILED))
          {
            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage(resp.getStatusError());
            addOutputToTask(task);
            task.apply();

            it.remove();

            sendEmail(task);

            removeFromOdm(task, task.getOdmUUID());
          }
          else if (resp.getStatus().equals(ODMStatus.COMPLETED))
          {
            long millis = resp.getProcessingTime();

            String sProcessingTime = String.format("%d hours, %d minutes", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));

            task.appLock();
            task.setStatus(resp.getStatus().getLabel());
            task.setMessage("Processing of " + resp.getImagesCount() + " images completed in " + sProcessingTime);
            addOutputToTask(task);
            task.apply();

            it.remove();

            uploadResultsToS3(task);
          }
        }
      }
    }

    private void uploadResultsToS3(ODMProcessingTaskIF task)
    {
      ODMUploadTaskIF uploadTask = null;

      if (task instanceof ImageryODMProcessingTask)
      {
        ImageryODMUploadTask imageryOdmUploadTask = new ImageryODMUploadTask();
        imageryOdmUploadTask.setUploadId(task.getUploadId());
        imageryOdmUploadTask.setImagery(task.getImageryComponent().getOid());
        imageryOdmUploadTask.setGeoprismUser(task.getGeoprismUser());
        imageryOdmUploadTask.setOdmUUID(task.getOdmUUID());
        imageryOdmUploadTask.setStatus(ODMStatus.RUNNING.getLabel());
        imageryOdmUploadTask.setProcessingTask(task);
//        imageryOdmUploadTask.setTaskLabel("Uploading Orthorectification Artifacts for [" + task.getCollection().getName() + "].");
        imageryOdmUploadTask.setTaskLabel("UAV data orthorectification upload for collection [" + task.getImageryComponent().getName() + "]");
        imageryOdmUploadTask.setMessage("The results of the Orthorectification processing are being uploaded to S3. Currently uploading orthorectification artifacts for ['" + task.getImageryComponent().getName() + "']. Check back later for updates.");
        imageryOdmUploadTask.apply();

        uploadTask = imageryOdmUploadTask;
      }
      else
      {
        ODMUploadTask odmUploadTask = new ODMUploadTask();
        odmUploadTask.setUploadId(task.getUploadId());
        odmUploadTask.setComponent(task.getImageryComponent().getOid());
        odmUploadTask.setGeoprismUser(task.getGeoprismUser());
        odmUploadTask.setOdmUUID(task.getOdmUUID());
        odmUploadTask.setStatus(ODMStatus.RUNNING.getLabel());
        odmUploadTask.setProcessingTask(task);
//        uploadTask.setTaskLabel("Uploading Orthorectification Artifacts for [" + task.getCollection().getName() + "].");
        odmUploadTask.setTaskLabel("UAV data orthorectification upload for collection [" + task.getImageryComponent().getName() + "]");
        odmUploadTask.setMessage("The results of the Orthorectification processing are being uploaded to S3. Currently uploading orthorectification artifacts for ['" + task.getImageryComponent().getName() + "']. Check back later for updates.");
        odmUploadTask.apply();

        uploadTask = odmUploadTask;
      }

      S3ResultsUploadThread thread = new S3ResultsUploadThread("S3Uploader for " + uploadTask.getOdmUUID(), uploadTask);
      uploadThreads.add(thread);
      thread.start();
    }

    private void addOutputToTask(ODMProcessingTaskIF task)
    {
      TaskOutputResponse resp = ODMFacade.taskOutput(task.getOdmUUID());

      if (resp.hasOutput())
      {
        JSONArray output = resp.getOutput();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < output.length(); ++i)
        {
          sb.append(output.getString(i));
          sb.append("&#13;&#10;");
        }

        task.setOdmOutput(sb.toString());

        task.writeODMtoS3(output);
      }
    }
  }

  private static class S3ResultsUploadThread extends Thread
  {
    private ODMUploadTaskIF uploadTask;

    private File            zip;

    private File            unzippedParentFolder;

    protected S3ResultsUploadThread(String name, ODMUploadTaskIF uploadTask)
    {
      super(name);

      this.uploadTask = uploadTask;
      this.unzippedParentFolder = new File(FileUtils.getTempDirectory(), "odm-" + uploadTask.getOdmUUID());

      if (DevProperties.runOrtho())
      {
        this.zip = ODMFacade.taskDownload(uploadTask.getOdmUUID());
      }
      else
      {
        this.zip = DevProperties.orthoResults();
      }
    }

    @Override
    public void run()
    {
      try
      {
        runInRequest();
      }
      finally
      {
        uploadThreads.remove(this);
      }
    }

    @Request
    public void runInRequest()
    {
      try
      {
        ProductIF product = runInTrans();

        uploadTask.lock();
        uploadTask.setStatus(ODMStatus.COMPLETED.getLabel());
        uploadTask.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
        uploadTask.apply();

        // Create image services
        uploadTask.getImageryComponent().createImageServices();

        // Calculate bounding boxes
        product.updateBoundingBox();

        ODMStatusServer.sendEmail(uploadTask);
      }
      catch (Throwable t)
      {
        logger.error("Error occurred while uploading S3 files for " + uploadTask.getOdmUUID(), t);

        String msg;
        if (t instanceof SpecialException)
        {
          msg = t.getLocalizedMessage();
        }
        else
        {
          msg = "The upload failed. " + t.getLocalizedMessage();
        }

        uploadTask.lock();
        uploadTask.setStatus(ODMStatus.FAILED.getLabel());
        uploadTask.setMessage(msg);
        uploadTask.apply();
      }
    }

    private List<ODMFolderProcessingConfig> buildProcessingConfig()
    {
      List<ODMFolderProcessingConfig> processingConfigs = new ArrayList<ODMFolderProcessingConfig>();

      processingConfigs.add(new ODMFolderProcessingConfig("odm_dem", ImageryComponent.DEM, new String[] { "dsm.tif", "dtm.tif" }));

      processingConfigs.add(new ODMFolderProcessingConfig("odm_georeferencing", ImageryComponent.PTCLOUD, new String[] { "odm_georeferenced_model.laz" }));

      processingConfigs.add(new ODMFolderProcessingConfig("odm_orthophoto", ImageryComponent.ORTHO, new String[] { "odm_orthophoto.png", "odm_orthophoto.tif" }));

      processingConfigs.add(new ODMFolderProcessingConfig("micasense", "micasense", null));

      return processingConfigs;
    }

//    @Transaction
    public ProductIF runInTrans() throws ZipException, SpecialException, InterruptedException
    {
      List<ODMFolderProcessingConfig> processingConfigs = buildProcessingConfig();

      /**
       * Upload the full all.zip file to S3 for archive purposes.
       */
      String allKey = uploadTask.getImageryComponent().getS3location() + "odm_all" + "/" + zip.getName();

      if (DevProperties.uploadAllZip())
      {
        Util.uploadFileToS3(zip, allKey, uploadTask);
      }

      ImageryComponent ic = uploadTask.getImageryComponent();

      // Determine the raw documents which were used for to generate this ODM
      // output
      UasComponentIF component = ic.getUasComponent();

      List<DocumentIF> raws = component.getDocuments().stream().filter(doc -> {
        return doc.getS3location().contains("/raw/");
      }).collect(Collectors.toList());

      String filePrefix = this.uploadTask.getProcessingTask().getFilePrefix();

      /**
       * Unzip the ODM all.zip file and selectively upload files that interest
       * us to S3.
       */
      try
      {
        try
        {
          new ZipFile(zip).extractAll(unzippedParentFolder.getAbsolutePath());
        }
        catch (ZipException e)
        {
          throw new SpecialException("ODM did not return any results. (There was a problem unzipping ODM's results zip file)", e);
        }

        List<DocumentIF> documents = new LinkedList<DocumentIF>();

        for (ODMFolderProcessingConfig config : processingConfigs)
        {
          if (Thread.interrupted())
          {
            throw new InterruptedException();
          }

          File parentDir = new File(unzippedParentFolder, config.odmFolderName);

          if (parentDir.exists())
          {
            processChildren(parentDir, config.s3FolderName, config, filePrefix, documents);
          }

          List<String> unprocessed = config.getUnprocessedFiles();
          if (unprocessed.size() > 0)
          {
            for (String name : unprocessed)
            {
              uploadTask.createAction("ODM did not produce an expected file [" + config.s3FolderName + "/" + name + "].", "error");
            }
          }
        }

        ODMProcessingTaskIF processingTask = this.uploadTask.getProcessingTask();
        List<String> list = processingTask.getFileList();

        ProductIF product = ic.getUasComponent().createProductIfNotExist();
        product.clear();

        product.addDocuments(documents);

        for (DocumentIF raw : raws)
        {
          if (list.size() == 0 || list.contains(raw.getName()))
          {
            raw.addGeneratedProduct(product);
          }
        }

        return product;
      }
      finally
      {
        if (DevProperties.runOrtho())
        {
          FileUtils.deleteQuietly(zip);
          removeFromOdm(this.uploadTask, this.uploadTask.getOdmUUID());
        }
        FileUtils.deleteQuietly(unzippedParentFolder);
      }
    }

    private void processChildren(File parentDir, String s3FolderPrefix, ODMFolderProcessingConfig config, String filePrefix, List<DocumentIF> documents) throws InterruptedException
    {
      File[] children = parentDir.listFiles();
      for (File child : children)
      {
        if (Thread.interrupted())
        {
          throw new InterruptedException();
        }

        String name = child.getName();

        if (filePrefix != null && filePrefix.length() > 0)
        {
          name = filePrefix + "_" + name;
        }

        if (!child.isDirectory() && UasComponentIF.isValidName(name) && config.shouldProcessFile(child))
        {
          ImageryComponent ic = uploadTask.getImageryComponent();
          final UasComponentIF component = ic.getUasComponent();

          String key = ic.getS3location() + s3FolderPrefix + "/" + name;

          Util.uploadFileToS3(child, key, uploadTask);

          documents.add(component.createDocumentIfNotExist(key, name));

          SolrService.updateOrCreateDocument(ic.getAncestors(), component, key, name);
        }
        else if (child.isDirectory())
        {
          processChildren(child, s3FolderPrefix + "/" + child.getName(), config, filePrefix, documents);
        }
      }
    }

    private class SpecialException extends Exception
    {
      private static final long serialVersionUID = 1L;

      public SpecialException(String string, ZipException e)
      {
        super(string, e);
      }
    }

    private class ODMFolderProcessingConfig
    {
      private String            odmFolderName;

      private String            s3FolderName;

      private String[]          mandatoryFiles;

      private ArrayList<String> processedFiles;

      protected ODMFolderProcessingConfig(String odmFolderName, String s3FolderName, String[] mandatoryFiles)
      {
        this.odmFolderName = odmFolderName;
        this.s3FolderName = s3FolderName;
        this.mandatoryFiles = mandatoryFiles;
        this.processedFiles = new ArrayList<String>();
      }

      protected boolean shouldProcessFile(File file)
      {
        if (this.mandatoryFiles == null)
        {
          return true;
        }

        if (ArrayUtils.contains(mandatoryFiles, file.getName()) && DevProperties.shouldUploadProduct(file.getName()))
        {
          processedFiles.add(file.getName());
          return true;
        }

        return false;
      }

      protected List<String> getUnprocessedFiles()
      {
        ArrayList<String> unprocessed = new ArrayList<String>();

        if (mandatoryFiles == null)
        {
          return unprocessed;
        }

        for (String file : mandatoryFiles)
        {
          if (!processedFiles.contains(file))
          {
            unprocessed.add(file);
          }
        }

        return unprocessed;
      }
    }
  }
}
