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

import com.runwaysdk.RunwayException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

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
import net.lingala.zip4j.ZipFile;
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

      task.createAction("Problem occured while sending email. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()), "error");
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

      task.createAction("Problem occured while cleaning up data from ODM. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()), "error");
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
  
        InfoResponse resp = null;
        try
        {
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
            task.setMessage(RunwayException.localizeThrowable(ex, Session.getCurrentLocale()));
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
  
              String sProcessingTime = String.format("%d hours, %d minutes", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
  
              if (resp.getImagesCount() == 1) // This happens when not using ODM's newer "chunk" functionality, and it only happens when a auto-scaling node is spinning up.
              {
                task.setMessage("Your images are being processed. Check back later for updates. An email will be sent when the processing is complete.");
              }
              else
              {
                task.setMessage("Processing of " + resp.getImagesCount() + " images has been running for " + sProcessingTime + ". An email will be sent when the processing is complete");
              }
              
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
  
              String sProcessingTime = String.format("%d hours, %d minutes", TimeUnit.MILLISECONDS.toHours(millis), TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
  
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
        catch (Throwable t)
        {
          task.appLock();
          task.setStatus(ODMStatus.FAILED.getLabel());
          task.setMessage("The job encountered an unspecified error.");
          task.apply();

          if (resp != null && resp.getHTTPResponse() != null)
          {
            task.setOdmOutput("HTTP communication with ODM has failed [" + resp.getHTTPResponse().getStatusCode() + "]. " + resp.getHTTPResponse().getResponse());
          }
          else
          {
            task.setOdmOutput("HTTP communication with ODM has failed.");
          }

          it.remove();
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
        
        processODMOutputAndCreateTasks(output, task);
      }
    }
    
    /**
     * Read through the ODM output and attempt to identify issues that may have occurred.
     */
    private void processODMOutputAndCreateTasks(JSONArray output, ODMProcessingTaskIF task)
    {
      String sOutput = output.toString();
      
      for (int i = 0; i < output.length(); ++i)
      {
        String line = output.getString(i);
        
        if (line.contains("MICA-CODE:1"))
        {
          task.createAction("Unable to find image panel. This may effect image color quality. Did you include a panel with naming convention IMG_0000_*.tif? Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("MICA-CODE:2"))
        {
          task.createAction("Alignment image not found. Did you include an alignment image with naming convention IMG_0001_*.tif? Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("MICA-CODE:3"))
        {
          task.createAction("Image [\" + fileName + \"] does not match the naming convention. Are you following the proper naming convention for your images? Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("MICA-CODE:4"))
        {
          task.createAction("Image alignment has failed. Take the time to look through your image collection and weed out any bad images, i.e. pictures of the camera inside the case, or pictures which are entirely black, etc. You can also try using a different alignment image (by renaming a better image set to IMG_0001_*.tif). Finally, make sure this collection is of a single flight and that there were no 'shock' events. Refer to the multispectral documentation for more information.", "error");
        }
        else if (line.contains("Panels not detected in all images")) // Micasense
        {
          task.createAction("Your upload includes images with naming convention IMG_0000_*.tif, however those images do not appear to be of a panel. Make sure that IMG_0000_*.tif, if included, is of a panel, and that there are no other panels anywhere else in your images. Refer to the multispectral documentation for more information.", "error");
        }
        else if (
            line.contains("[Errno 2] No such file or directory: '/var/www/data/0182028a-c14f-40fe-bd6f-e98362ec48c7/opensfm/reconstruction.json'") // 0.9.1 output
            || line.contains("The program could not process this dataset using the current settings. Check that the images have enough overlap") // 0.9.8 output
            || (line.contains("IndexError: list index out of range")
                && output.getString(i-1).contains("reconstruction = data[0]")
                && sOutput.contains("Traceback (most recent call last):")) // 0.9.1
            )
        {
          task.createAction("ODM failed to produce a reconstruction from the image matches. Check that the images have enough overlap, that there are enough recognizable features and that the images are in focus. (more info: https://github.com/OpenDroneMap/ODM/issues/524)", "error");
        }
        else if (line.contains("Not enough supported images in")) // 0.9.1
        {
          task.createAction("Couldn't find any usable images. The orthomosaic image data must contain at least two images with extensions '.jpg','.jpeg','.png'", "error");
        }
      }
    }
  }

  private static class S3ResultsUploadThread extends Thread
  {
    private ODMUploadTaskIF uploadTask;

    protected S3ResultsUploadThread(String name, ODMUploadTaskIF uploadTask)
    {
      super(name);

      this.uploadTask = uploadTask;
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
        product.createImageService();

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
          msg = "The upload failed. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale());
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
    
    private CloseableFile getZip()
    {
      if (DevProperties.runOrtho())
      {
        return ODMFacade.taskDownload(uploadTask.getOdmUUID());
      }
      else
      {
        return DevProperties.orthoResults();
      }
    }

//    @Transaction
    public ProductIF runInTrans() throws ZipException, SpecialException, InterruptedException
    {
      try (CloseableFile unzippedParentFolder = new CloseableFile(FileUtils.getTempDirectory(), "odm-" + uploadTask.getOdmUUID()))
      {
        try (CloseableFile zip = getZip())
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
      }
      finally
      {
        if (DevProperties.runOrtho())
        {
          removeFromOdm(this.uploadTask, this.uploadTask.getOdmUUID());
        }
      }
    }

    private void processChildren(File parentDir, String s3FolderPrefix, ODMFolderProcessingConfig config, String filePrefix, List<DocumentIF> documents) throws InterruptedException
    {
      File[] children = parentDir.listFiles();

      if (children == null)
      {
        logger.error("Problem occurred while listing files of directory [" + parentDir.getAbsolutePath() + "].");
        return;
      }

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
