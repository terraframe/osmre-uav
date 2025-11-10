package gov.geoplatform.uasdm.processing;

import com.runwaysdk.resource.ApplicationFileResource;

public class ProcessResult
{
  public static enum Status {
    SUCCESS, FAIL
  }

  private Status status;

  private Object resource;

  public ProcessResult(Status status)
  {
    super();
    this.status = status;
    this.resource = null;
  }

  public ProcessResult(Status status, Object resource)
  {
    super();
    this.status = status;
    this.resource = resource;
  }

  public Status getStatus()
  {
    return status;
  }

  public void setStatus(Status status)
  {
    this.status = status;
  }

  @SuppressWarnings("unchecked")
  public <T> T getResource()
  {
    return (T) resource;
  }

  public void setResource(Object resource)
  {
    this.resource = resource;
  }

  public boolean success()
  {
    return this.status.equals(Status.SUCCESS);
  }

  public boolean failure()
  {
    return this.status.equals(Status.FAIL);
  }

  public boolean isResouce()
  {
    return this.resource != null && ( this.resource instanceof ApplicationFileResource );
  }

  public static ProcessResult success(Object o)
  {
    return new ProcessResult(Status.SUCCESS, o);
  }

  public static ProcessResult fail()
  {
    return new ProcessResult(Status.FAIL);
  }

  public static ProcessResult join(ProcessResult a, ProcessResult b)
  {
    Status statues = a.success() && b.success() ? Status.SUCCESS : Status.FAIL;

    return new ProcessResult(statues, null);
  }
}
