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

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;

public interface ODMServiceIF
{

  TaskOutputResponse taskOutput(String uuid);

  TaskRemoveResponse taskRemove(String uuid);

  CloseableFile taskDownload(String uuid);

  /*
   * Uploads the zip to ODM using the init / upload / commit paradigm, which is
   * recommended for large file uploads.
   * 
   * https://github.com/OpenDroneMap/NodeODM/blob/master/docs/index.adoc#post-
   * tasknewinit
   */
  NewResponse taskNew(ApplicationResource images, boolean isMultispectral, ODMProcessConfiguration configuration, Collection col, AbstractWorkflowTask task);

  NewResponse taskNewInit(int imagesCount, boolean isMultispectral, ODMProcessConfiguration configuration);

  ODMResponse taskNewUpload(String uuid, ApplicationResource image);

  ODMResponse taskNewCommit(String uuid);

  InfoResponse taskInfo(String uuid);

}