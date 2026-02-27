package gov.geoplatform.uasdm.processing;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.RunwayException;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.ProcessingRun;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.EmptyFileSetException;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskStatus;
import gov.geoplatform.uasdm.odm.ProcessingTaskStatusServer;
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

public class FargateProcessingTask extends FargateProcessingTaskBase implements FargateTaskIF
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
        this.setStatus(ProcessingTaskStatus.FAILED.getLabel());
        this.setMessage(ecsResp.has("message") ? ecsResp.get("message").getAsString() : "ECS RunTask failed.");
        this.apply();
        return;
      }

      String taskArn = ecsResp.get("taskArn").getAsString();
      ProcessingRun run = ProcessingRun.createAndApplyFor(this, taskArn, taskDef);

      this.appLock();
      this.setProcessingRun(run);
      this.setStatus(ProcessingTaskStatus.RUNNING.getLabel());
      this.setTaskArn(ecsResp.get("taskArn").getAsString());
      this.setTaskDefinitionArn(taskDef.getArn());
      
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
      this.setStatus(ProcessingTaskStatus.COMPLETED.getLabel());
      this.setMessage("No image files to be processed.");
      this.apply();
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while initiating fargate processing.", t);

      this.appLock();
      this.setStatus(ProcessingTaskStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      this.apply();
    }
  }
  
  public TaskResult checkStatus()
  {
    TaskResult result = new TaskResult(); // defaults to ACTIVE

    // If something already marked it terminal, don't keep thrashing.
    String current = this.getStatus();
    if (ProcessingTaskStatus.COMPLETED.getLabel().equals(current))
    {
      result.setStatus(TaskStatus.COMPLETED);
      return result;
    }
    else if (ProcessingTaskStatus.FAILED.getLabel().equals(current))
    {
      result.setStatus(TaskStatus.ERROR);
      return result;
    }

    final String taskArn = this.getTaskArn();
    if (taskArn == null || taskArn.isBlank())
    {
      this.appLock();
      this.setStatus(ProcessingTaskStatus.FAILED.getLabel());
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
        this.setStatus(ProcessingTaskStatus.FAILED.getLabel());
        this.setMessage("ECS DescribeTasks failure: " + (f.arn() == null ? "" : f.arn())
            + " : " + f.reason() + (f.detail() == null ? "" : (". " + f.detail())));
        this.apply();

        result.setStatus(TaskStatus.ERROR);
        return result;
      }

      if (resp.tasks() == null || resp.tasks().isEmpty())
      {
        this.appLock();
        this.setStatus(ProcessingTaskStatus.FAILED.getLabel());
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
        this.setStatus(ProcessingTaskStatus.RUNNING.getLabel());
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
        this.appLock();
        this.setStatus(ProcessingTaskStatus.COMPLETED.getLabel());
        this.setMessage("Processing completed successfully.");
        this.apply();

        result.setStatus(TaskStatus.COMPLETED);
      }
      else
      {
        this.appLock();
        this.setStatus(ProcessingTaskStatus.FAILED.getLabel());

        String msg = "Processing failed on ECS.";
        if (exitCode != null) msg += " Exit code: " + exitCode + ".";
        if (stopReason != null && !stopReason.isBlank()) msg += " Reason: " + stopReason;
        this.setMessage(msg);

        this.apply();

        result.setStatus(TaskStatus.ERROR);
      }
      
      result.setExitCode(exitCode);

      return result;
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while checking Fargate status.", t);

      this.appLock();
      this.setStatus(ProcessingTaskStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      this.apply();

      result.setStatus(TaskStatus.ERROR);
      return result;
    }
  }
  
  public static List<DocumentIF> getRaw(UasComponentIF component) {
    List<DocumentIF> documents;
    if (component instanceof Collection)
        documents = ((Collection) component).getRaw();
    else
        documents = component.getDocuments();
    
    documents = documents.stream()
        .filter(d -> !Boolean.TRUE.equals(d.getExclude()))
        .collect(Collectors.toList());
    
    return documents;
  }
  
  public static DocumentIF selectLazForProcessing(UasComponentIF component) {
    var documents = getRaw(component).stream()
        .filter(d -> FilenameUtils.getExtension(d.getName()).equalsIgnoreCase("laz"))
        .collect(Collectors.toList());
    
    if (documents.size() > 1) {
      throw new RuntimeException("Expected a single laz file, but there were " + documents.size());
    } else if (documents.size() == 0) {
      throw new EmptyFileSetException();
    }
    
    return documents.get(0);
  }
  
  protected FargateTaskDefinition taskForRequest() {
    long totalBytes = 0L;
  
    if (this.getConfiguration().isLidar()) {
      var laz = selectLazForProcessing(this.getComponentInstance());
  
      totalBytes = laz.getFileSize();
    } else {
      for (var doc : getRaw(this.getComponentInstance())) {
          totalBytes += doc.getFileSize();
      }
    }

    long colSizeMb = totalBytes / (1024L * 1024L);
    int sizeGb = (int) Math.min(colSizeMb, Integer.MAX_VALUE); // Guards against an overflow
    
    return FargateTaskDefinition.select(sizeGb);
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
              KeyValuePair.builder().name("S3_COMPONENT").value("s3://" + AppProperties.getBucketName() + "/" + component.getS3location()).build(),
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
  
  public String getProcessingRunOid() {
    return getValue(PROCESSINGRUN);
  }
  
  @Override
  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();

    String runId = this.getProcessingRunOid();
    if (runId != null)
    {
      obj.put("processingRunId", runId);
    }
    
    // TODO : Runtime estimates?
//    if (StringUtils.isNotBlank(getRuntimeEstimateJson()))
//      obj.put("runtimeEstimate", new JSONObject(getRuntimeEstimateJson()));

    return obj;
  }

  public TaskResult poll()
  {
    TaskResult status = checkStatus();
    
    if (status.getStatus().equals(TaskStatus.COMPLETED)) {
      storeResults(status);
    } else if (status.getStatus().equals(TaskStatus.ERROR)) {
      FargateProcessingFinalizer.factory(this, status).finalize();
    }
    
    return status;
  }
  
  protected void storeResults(TaskResult status) {
    var store = new FargateStoreTask();
    store.setComponent(this.getComponent());
    store.setUploadId(this.getUploadId());
    store.setGeoprismUserId(this.getGeoprismUserOid());
    store.setStatus(ProcessingTaskStatus.RUNNING.getLabel());
    store.setMessage("Artifacts from processing are being archived.");
    store.setTaskArn(this.getTaskArn());
    store.setTaskDefinitionArn(this.getTaskDefinitionArn());
    store.setProcessingType(this.getProcessingType());
    store.setConfiguration(getConfiguration());
    store.setProcessingRun(getProcessingRun());
    store.setProcessingJobId(this.getProcessingJobId());
    store.apply();
    store.finalize(status);
  }
  
  public ProcessConfiguration getConfiguration()
  {
    String json = this.getConfigurationJson();
    
    if (!StringUtils.isEmpty(json))
    {
      return ProcessConfiguration.parse(json);
    }

    return null;
  }

  public void setConfiguration(ProcessConfiguration configuration)
  {
    this.setConfigurationJson(configuration.toJson().toString());
  }

  @Override
  public String getProcessingJobId()
  {
    return this.getOid();
  }
}
