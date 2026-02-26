package gov.geoplatform.uasdm.processing;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.graph.ProcessingRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskStatus;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.OutputLogEvent;

public class FargateProcessingFinalizer
{
  private static final Logger logger = LoggerFactory.getLogger(FargateProcessingFinalizer.class);
  
  protected FargateTaskIF task;
  
  protected TaskResult result;
  
  protected boolean reponseProcessorSetMessageAndStatus = false;
  
  protected boolean missingRequiredArtifact = false;
  
  public FargateProcessingFinalizer(FargateTaskIF task, TaskResult result) {
    this.task = task;
    this.result = result;
  }
  
  public void finalize() {
    try {
      if (result.getStatus().equals(TaskStatus.COMPLETED))
      {
        handleArtifacts();
      }
      
      addOutputToTask();
      
      if (!reponseProcessorSetMessageAndStatus) {
        final String exitCode = result.getExitCode() == null ? " (the job never ran)" : " (exit code " + result.getExitCode() + ")";
        task.appLock();
        task.setMessage("Encountered a fatal error during processing" + exitCode);
        task.setStatus(ProcessingTaskStatus.FAILED.getLabel());
        task.apply();
      }
      
      RemoteFileFacade.deleteObjects(FargateProcessingTask.JOBS + "/" + task.getOid());
      
      if (missingRequiredArtifact)
      {
        task.appLock();
        task.setStatus(ProcessingTaskStatus.FAILED.getLabel());
        task.setMessage("Required artifacts were not produced during processing. See messages for more information.");
        task.apply();
      }
      else if (result.getStatus().equals(TaskStatus.COMPLETED))
      {
        task.appLock();
        task.setStatus(ProcessingTaskStatus.COMPLETED.getLabel());
        
        final String seeWarnings = task.getActions().size() > 0 ? ", but with warnings. See messages for more information." : ".";
        task.setMessage("Artifacts archived successfully" + seeWarnings);
        
        task.apply();
      }
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while archiving files for " + task.getTaskArn(), t);

      String msg = RunwayException.localizeThrowable(t, Session.getCurrentLocale());

      task.lock();
      task.setStatus(ODMStatus.FAILED.getLabel());
      task.setMessage(msg);
      task.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
    }
  }
  
  protected void handleArtifacts() {
    final StatusMonitorIF monitor = new WorkflowTaskMonitor((AbstractWorkflowTask) task);
    final UasComponentIF component = task.getComponentInstance();
    final ProcessConfiguration config = task.getConfiguration();
    final Product product = (Product) component.createProductIfNotExist(config.getProductName());
    
    List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, FargateProcessingTask.JOBS + "/" + task.getProcessingJobId(), new LinkedList<SiteObject>(), null, null).getObjects();
    
    handleArtifact(".*\\.copc\\.laz", ImageryComponent.PTCLOUD + "/pointcloud.copc.laz", (config.toLidar().isGenerateCopc() && !alreadyHasCopc()), items, product, component, monitor);
    handleArtifact("m_Classification_veg_density.cog.tif", ImageryComponent.ORTHO + "/m_Classification_veg_density.cog.tif", config.toLidar().isGenerateTreeCanopyCover(), items, product, component, monitor);
    handleArtifact("m_Z_diff.cog.tif", ImageryComponent.ORTHO + "/m_Z_diff.cog.tif", config.toLidar().isGenerateTreeStructure(), items, product, component, monitor);
    handleArtifact("m_Z_max.cog.tif", ImageryComponent.ORTHO + "/m_Z_max.cog.tif", config.toLidar().isGenerateGSM(), items, product, component, monitor);
    handleArtifact("m_Z_min.cog.tif", ImageryComponent.ORTHO + "/m_Z_min.cog.tif", config.toLidar().isGenerateTerrainModel(), items, product, component, monitor);
    
    // Designate a primary product if it makes sense
    designatePrimaryProduct(component, config);
  }
  
  protected void manageRemote(String s3Path, SiteObject item, Product product, UasComponentIF component, StatusMonitorIF monitor) {
    RemoteFileObject remote = RemoteFileFacade.download(item.getKey());
    
    try(CloseableFile downloaded = remote.openNewFile()) {
      new ManagedDocument(s3Path, product, component, monitor).process(new FileResource(downloaded));
    }
  }
  
  protected boolean alreadyHasCopc() {
    DocumentIF laz = FargateProcessingTask.selectLazForProcessing(task.getComponentInstance());
    
    // This works so long as we never allow them to store a non-copc format with this extension.
    return laz.getName().endsWith(".copc.laz");
  }
  
  protected void handleArtifact(String regex, String s3Path, boolean required, List<SiteObject> items, Product product, UasComponentIF component, StatusMonitorIF monitor) {
    if (!required)
      return;
    
    for (var item : items) {
      if (item.getName().matches(regex)) {
        manageRemote(s3Path, item, product, component, monitor);
        return;
      }
    }
    
    task.createAction("Processing job did not generated expected file [" + s3Path + "]", TaskActionType.ERROR);
    missingRequiredArtifact = true;
  }
  
  protected void designatePrimaryProduct(UasComponentIF component, ProcessConfiguration config) {
    if (component.getPrimaryProduct().isEmpty()) {
      for (ProductIF product : component.getProducts()) {
        if (!product.getProductName().equals(config.getProductName()) && product.getProductName().contains(config.getProductName())) {
          ((Product)product).setPrimary(true);
          ((Product)product).apply();
          break;
        }
      }
    }
  }
  
  protected void addOutputToTask()
  {
    try {
      String output = fetchLogs();
      
      if (!StringUtils.isBlank(output))
      {
     // TODO : Is this needed for the front-end? Guess we'll find out.
//        for (int i = 0; i < output.length(); ++i)
//        {
//          sb.append(output.getString(i));
//          sb.append("&#13;&#10;");
//        }

//      TODO : archive on s3?
//      writeODMtoS3(output);

        new FargateResponseProcessor().process(task, output);
        
        ProcessingRun run = task.getProcessingRun();
        if (run != null)
        {
          run.setOutput(output);
          run.setRunEnd(new Date());
          run.apply();
        }
        
        reponseProcessorSetMessageAndStatus = true;
      }
      else {
        task.createAction("CloudWatch logs empty for " + task.getTaskArn(), TaskActionType.ERROR);
      }
    } catch (Throwable t) {
      task.createAction("Failed to fetch CloudWatch logs: " + t.getClass().getSimpleName() + ": " + safe(t.getMessage()), TaskActionType.ERROR);
    }
  }
  
  protected String fetchLogs() {
    final Region region = Region.of(AppProperties.getBucketRegion());

    final String taskArn = task.getTaskArn();
    final String taskId = taskArn.substring(taskArn.lastIndexOf("/") + 1);
    final String logGroup  = AppProperties.getFargateAutoscalerLogGroup();
    final String logStream = AppProperties.getFargateAutoscalerLogStreamPrefix() + "/" + AppProperties.getFargateAutoscalerContainerName() + "/" + taskId;

    AwsBasicCredentials creds = AwsBasicCredentials.create(
        AppProperties.getFargateAutoscalerECSAccessKey(),
        AppProperties.getFargateAutoscalerECSSecretKey()
    );

    try (CloudWatchLogsClient cwl = CloudWatchLogsClient.builder()
        .region(region)
        .credentialsProvider(StaticCredentialsProvider.create(creds))
        .build())
    {
      StringBuilder sb = new StringBuilder(64_000);

      String nextToken = null;
      int events = 0;
      final int maxEvents = 50_000; // guardrail

      while (true) {
        var resp = cwl.getLogEvents(GetLogEventsRequest.builder()
            .logGroupName(logGroup)
            .logStreamName(logStream)
            .startFromHead(true)
            .nextToken(nextToken)
            .limit(Math.min(10_000, maxEvents - events))
            .build());

        if (resp.events() == null || resp.events().isEmpty()) break;

        for (OutputLogEvent e : resp.events()) {
          if (e.message() != null) sb.append(e.message());
          if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') sb.append('\n');
          events++;
          if (events >= maxEvents) break;
        }

        String newToken = resp.nextForwardToken();
        if (newToken == null || newToken.equals(nextToken) || events >= maxEvents) break;
        nextToken = newToken;
      }

      return sb.toString();

    }
  }

  protected String safe(Object o) {
    return o == null ? "" : String.valueOf(o);
  }
}
