package gov.geoplatform.uasdm.odm;

public interface TaskService
{
  public void addTask(ODMProcessingTaskIF task);

  public void startup();

  public void shutdown();
}
