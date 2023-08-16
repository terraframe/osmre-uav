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
package gov.geoplatform.uasdm.view;

import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONArray;

public interface RequestParserIF
{

  String getFilename();

  // only non-null for MPFRs
  FileItem getUploadItem();

  boolean generateError();

  int getPartIndex();

  long getTotalFileSize();

  int getTotalParts();

  String getUuid();

  String getUasComponentOid();

  Boolean getProcessUpload();

  Boolean getProcessDem();

  Boolean getProcessOrtho();

  Boolean getProcessPtcloud();

  JSONArray getSelections();

  String getUploadTarget();

  String getDescription();

  String getTool();

  String getOriginalFilename();

  String getMethod();

  boolean isResume();

  boolean isFirst();

  Map<String, String> getCustomParams();

  int getPercent();

  Integer getPtEpsg();

  String getOrthoCorrectionModel();

  String getProjectionName();

}