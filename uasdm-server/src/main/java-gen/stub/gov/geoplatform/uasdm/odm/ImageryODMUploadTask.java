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

public class ImageryODMUploadTask extends ImageryODMUploadTaskBase implements ODMUploadTaskIF
{
  private static final long serialVersionUID = -1076802948;

  public ImageryODMUploadTask()
  {
    super();
  }

  @Override
  public Boolean getProcessDem()
  {
    return false;
  }

  @Override
  public Boolean getProcessOrtho()
  {
    return false;
  }

  @Override
  public Boolean getProcessPtcloud()
  {
    return false;
  }

  public String getImageryComponentOid()
  {
    return this.getImagery();
  }

  public void setProcessingTask(ODMProcessingTaskIF odmProcessingTask)
  {
    this.setProcessingTask((ImageryODMProcessingTask) odmProcessingTask);
  }
}
