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
