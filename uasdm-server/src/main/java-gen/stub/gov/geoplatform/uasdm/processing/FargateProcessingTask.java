package gov.geoplatform.uasdm.processing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.ProcessingRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.EmptyFileSetException;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskStatus;
import gov.geoplatform.uasdm.odm.ProcessingTaskStatusServer;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.SiteObject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.AwsVpcConfiguration;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.Failure;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.LaunchType;
import software.amazon.awssdk.services.ecs.model.NetworkConfiguration;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.RunTaskResponse;
import software.amazon.awssdk.services.ecs.model.TaskOverride;

public class FargateProcessingTask extends FargateProcessingTaskBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -377855652;
  
  public static final String JOBS = "jobs";
  
  private static final Logger logger = LoggerFactory.getLogger(FargateProcessingTask.class);
  
  public FargateProcessingTask()
  {
    super();
  }
  
  public void initiate(Set<String> excludes)
  {
    try
    {
      FargateTaskDefinition taskDef = taskForRequest();
      
      JsonObject ecsResp = invokeFargate(excludes, taskDef);
      
      if (!"success".equals(ecsResp.get("status").getAsString()))
      {
        this.appLock();
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage(ecsResp.has("message") ? ecsResp.get("message").getAsString() : "ECS RunTask failed.");
        this.apply();
        return;
      }

      String taskArn = ecsResp.get("taskArn").getAsString();
      ProcessingRun.createAndApplyFor(this, taskArn, taskDef);

      this.appLock();
      this.setStatus(ODMStatus.RUNNING.getLabel());
      this.setTaskArn(ecsResp.get("taskArn").getAsString());
      
      // TODO : Runtime estimate?
//      JSONObject estimate = new ODMRunService().estimateRuntimeInRequest(this.getImageryComponentOid(), this.getConfigurationJson());
//      if (estimate != null)
//        this.setRuntimeEstimateJson(estimate.toString());
      
      this.setMessage("Your images are being processed. Check back later for updates.");
      this.apply();

      ProcessingTaskStatusServer.addTask(this);
    }
    catch (EmptyFileSetException e)
    {
      this.appLock();
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("No image files to be processed.");
      this.apply();
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while initiating ODM Processing.", t);

      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      this.apply();
    }
  }
  
  public TaskResult checkStatus()
  {
    TaskResult result = new TaskResult(); // defaults to ACTIVE

    // If something already marked it terminal, don't keep thrashing.
    String current = this.getStatus();
    if (ODMStatus.COMPLETED.getLabel().equals(current))
    {
      result.setStatus(TaskStatus.FINALIZING);
      // Optional: Finalize?
      return result;
    }
    else if (ODMStatus.FAILED.getLabel().equals(current))
    {
      result.setStatus(TaskStatus.ERROR);
      return result;
    }

    final String taskArn = this.getTaskArn();
    if (taskArn == null || taskArn.isBlank())
    {
      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage("Missing ECS task ARN (resourceId). Cannot check status.");
      this.apply();

      result.setStatus(TaskStatus.ERROR);
      return result;
    }

    final Region region = Region.of(AppProperties.getBucketRegion());

    AwsBasicCredentials creds = AwsBasicCredentials.create(
        AppProperties.getFargateAutoscalerECSAccessKey(),
        AppProperties.getFargateAutoscalerECSSecretKey()
    );

    try (EcsClient client = EcsClient.builder()
        .region(region)
        .credentialsProvider(StaticCredentialsProvider.create(creds))
        .build())
    {
      var req = software.amazon.awssdk.services.ecs.model.DescribeTasksRequest.builder()
          .cluster(AppProperties.getFargateAutoscalerCluster())
          .tasks(taskArn)
          .build();

      var resp = client.describeTasks(req);

      // ECS could not describe it
      if (resp.failures() != null && !resp.failures().isEmpty())
      {
        var f = resp.failures().get(0);

        this.appLock();
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage("ECS DescribeTasks failure: " + (f.arn() == null ? "" : f.arn())
            + " : " + f.reason() + (f.detail() == null ? "" : (". " + f.detail())));
        this.apply();

        result.setStatus(TaskStatus.ERROR);
        return result;
      }

      if (resp.tasks() == null || resp.tasks().isEmpty())
      {
        this.appLock();
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage("ECS DescribeTasks returned no tasks for ARN: " + taskArn);
        this.apply();

        result.setStatus(TaskStatus.ERROR);
        return result;
      }

      var task = resp.tasks().get(0);
      String lastStatus = task.lastStatus().toUpperCase(); // PROVISIONING, PENDING, ACTIVATING, RUNNING, STOPPED, etc.

      // Still running (or not yet started)
      if (!List.of("STOPPED", "DELETED", "DEPROVISIONING", "STOPPING", "DEACTIVATING").contains(lastStatus))
      {
        this.appLock();
        this.setStatus(ODMStatus.RUNNING.getLabel());
        this.setMessage("Processing is running on ECS (" + lastStatus + ").");
        this.apply();

        result.setStatus(TaskStatus.ACTIVE);
        return result;
      }

      // STOPPED -> determine success from exit code
      Integer exitCode = null;
      String stopReason = task.stoppedReason();

      if (task.containers() != null && !task.containers().isEmpty())
      {
        // If multiple containers matter, you may want: any non-zero => fail
        var c = task.containers().get(0);
        exitCode = c.exitCode();
        if (stopReason == null || stopReason.isBlank())
        {
          stopReason = c.reason();
        }
      }

      boolean success = (exitCode != null && exitCode.intValue() == 0);

      this.appLock();
      if (success)
      {
        this.setStatus(ODMStatus.COMPLETED.getLabel());
        this.setMessage("Processing completed successfully. Finalizing...");
        this.apply();

        result.setStatus(TaskStatus.FINALIZING);

        // TODO: attach downstream upload task, if your pipeline needs it.
        // Example placeholders (replace with your real way of creating/uploading):
        // ODMUploadTaskIF uploadTask = new ODMUploadTask(...);
        // result.setDownstreamTask(uploadTask);
      }
      else
      {
        this.setStatus(ODMStatus.FAILED.getLabel());

        String msg = "Processing failed on ECS.";
        if (exitCode != null) msg += " Exit code: " + exitCode + ".";
        if (stopReason != null && !stopReason.isBlank()) msg += " Reason: " + stopReason;
        this.setMessage(msg);

        this.apply();

        result.setStatus(TaskStatus.ERROR);
      }

      return result;
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while checking Fargate status.", t);

      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      this.apply();

      result.setStatus(TaskStatus.ERROR);
      return result;
    }
  }
  
  private FargateTaskDefinition taskForRequest() {
    UasComponent component = (UasComponent) getComponentInstance();

    List<DocumentIF> documents;
    if (component instanceof Collection)
        documents = ((Collection) component).getRaw();
    else
        documents = component.getDocuments();
    
    documents = documents.stream().filter(d -> !d.getExclude()).collect(Collectors.toList());

    long totalBytes = 0L;
    for (var doc : documents) {
        totalBytes += doc.getFileSize();
    }

    long colSizeMb = totalBytes / (1024L * 1024L);
    int sizeMb = (int) Math.min(colSizeMb, Integer.MAX_VALUE); // Guards against an overflow
    int rawCount = documents.size();
    
    if (rawCount <= 0)
      throw new EmptyFileSetException();
    
    // TODO : Different task sizes
    return FargateTaskDefinition.SMALL;
}
  
  public JsonObject invokeFargate(Set<String> excludes, FargateTaskDefinition taskDef)
  {
    UasComponent component = (UasComponent) this.getComponentInstance();

    final Region region = Region.of(AppProperties.getBucketRegion());

    AwsBasicCredentials creds = AwsBasicCredentials.create(
        AppProperties.getFargateAutoscalerECSAccessKey(),
        AppProperties.getFargateAutoscalerECSSecretKey()
    );

    try (EcsClient client = EcsClient.builder()
        .region(region)
        .credentialsProvider(StaticCredentialsProvider.create(creds))
        .build())
    {
      ContainerOverride containerOverride = ContainerOverride.builder()
          .name(AppProperties.getFargateAutoscalerContainerName())
          .environment(
              KeyValuePair.builder().name("EXCLUDES").value(String.join(",", excludes)).build(),
              KeyValuePair.builder().name("JOB_ID").value(this.getOid()).build(),
              KeyValuePair.builder().name("S3_COMPONENT").value(component.getS3location()).build(),
              KeyValuePair.builder().name("AWS_REGION").value(region.id()).build(),
              KeyValuePair.builder().name("AWS_ACCESS_KEY_ID").value(AppProperties.getS3AccessKey()).build(),
              KeyValuePair.builder().name("AWS_SECRET_ACCESS_KEY").value(AppProperties.getS3SecretKey()).build()
          )
          .build();

      TaskOverride overrides = TaskOverride.builder()
          .containerOverrides(containerOverride)
          .build();

      AwsVpcConfiguration vpcConfig = AwsVpcConfiguration.builder()
          .subnets(AppProperties.getFargateAutoscalerSubnets())
          .assignPublicIp(AssignPublicIp.ENABLED)
          .build();

      NetworkConfiguration networkConfig = NetworkConfiguration.builder()
          .awsvpcConfiguration(vpcConfig)
          .build();

      RunTaskRequest request = RunTaskRequest.builder()
          .taskDefinition(taskDef.getArn())
          .cluster(AppProperties.getFargateAutoscalerCluster())
          .launchType(LaunchType.FARGATE)
          .count(1)
          .overrides(overrides)
          .networkConfiguration(networkConfig)
          .build();

      RunTaskResponse response = client.runTask(request);
      
      JsonObject resp = new JsonObject();

      if (response.failures() != null && !response.failures().isEmpty())
      {
        resp.addProperty("status", "failure");

        Failure failure = response.failures().get(0);
        resp.addProperty("message",
            (failure.arn() == null ? "" : failure.arn()) +
            " : " + failure.reason() +
            ". " + (failure.detail() == null ? "" : failure.detail())
        );
        return resp;
      }

      if (response.tasks() == null || response.tasks().isEmpty() || response.tasks().get(0).taskArn() == null)
      {
        resp.addProperty("status", "failure");
        resp.addProperty("message", "ECS RunTask succeeded but returned no taskArn.");
        return resp;
      }

      String taskArn = response.tasks().get(0).taskArn();
      resp.addProperty("status", "success");
      resp.addProperty("taskArn", taskArn);

      return resp;
    }
  }

  public TaskResult poll()
  {
    TaskResult result = checkStatus();
    
    final String jobKey = JOBS + "/" + this.getOid();
    
    if (result.getStatus().equals(TaskStatus.FINALIZING))
    {
      final StatusMonitorIF monitor = new WorkflowTaskMonitor(this);
      final UasComponentIF component = getComponentInstance();
      final ProcessConfiguration config = this.getConfiguration();
      final Product product = (Product) component.createProductIfNotExist(config.getProductName());
      
      List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, jobKey, new LinkedList<SiteObject>(), null, null).getObjects();
      
      handleArtifact(".*\\.copc\\.laz", ImageryComponent.PTCLOUD + "/pointcloud.copc.laz", config.toLidar().isGenerateCopc(), items, product, component, monitor);
      handleArtifact("m_Classification_veg_density.cog.tif", ImageryComponent.ORTHO + "/m_Classification_veg_density.cog.tif", config.toLidar().isGenerateTreeCanopyCover(), items, product, component, monitor);
      handleArtifact("m_Z_diff.cog.tif", ImageryComponent.ORTHO + "/m_Z_diff.cog.tif", config.toLidar().isGenerateTreeStructure(), items, product, component, monitor);
      handleArtifact("m_Z_max.cog.tif", ImageryComponent.ORTHO + "/m_Z_max.cog.tif", config.toLidar().isGenerateGSM(), items, product, component, monitor);
      handleArtifact("m_Z_min.cog.tif", ImageryComponent.ORTHO + "/m_Z_min.cog.tif", config.toLidar().isGenerateTerrainModel(), items, product, component, monitor);
      
      // Designate a primary product if it makes sense
      designatePrimaryProduct(component, config);

      this.appLock();
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("Lidar processing is complete");
      this.apply();
      
      RemoteFileFacade.deleteObjects(jobKey);
    } else if (result.getStatus().equals(TaskStatus.ERROR)) {
      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage("Lidar processing encountered errors. View messages for more information.");
      this.apply();
      
      RemoteFileFacade.deleteObjects(jobKey);
    }
    
    return result;
  }
  
  private void handleArtifact(String regex, String s3Path, boolean required, List<SiteObject> items, Product product, UasComponentIF component, StatusMonitorIF monitor) {
    for (var item : items) {
      if (item.getName().matches(regex)) {
        manageRemote(s3Path, item, product, component, monitor);
        return;
      }
    }
    
    if (required) {
      this.createAction("Processing job did not generated expected file [" + s3Path + "]", TaskActionType.ERROR);
    }
  }
  
  private void designatePrimaryProduct(UasComponentIF component, ProcessConfiguration config) {
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
  
  private void manageRemote(String s3Path, SiteObject item, Product product, UasComponentIF component, StatusMonitorIF monitor) {
    RemoteFileObject remote = RemoteFileFacade.download(item.getKey());
    
    try(CloseableFile downloaded = remote.openNewFile()) {
      new ManagedDocument(s3Path, product, component, monitor).process(new FileResource(downloaded));
    }
  }
}
