/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.odm;

import java.util.List;

import org.json.JSONArray;

import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;

public interface ODMProcessingTaskIF extends ImageryWorkflowTaskIF
{
  public ODMProcessConfiguration getConfiguration();

  public void setConfiguration(ODMProcessConfiguration configuration);

  public String getOdmUUID();

  public String getUploadId();

  public void setOdmUUID(String value);

  public void setOdmOutput(String value);

  public String getImageryComponentOid();

  /**
   * Writes the ODM output to a log file on S3, if supported by the individual
   * task implementation.
   * 
   * @param odmOutput
   */
  public void writeODMtoS3(JSONArray odmOutput);

  public List<String> getFileList();

  public Boolean getProcessDem();

  public Boolean getProcessOrtho();

  public Boolean getProcessPtcloud();
}
