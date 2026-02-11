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
package gov.geoplatform.uasdm.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONArray;

import gov.geoplatform.uasdm.view.RequestParserIF;

public class MockRequestParser implements RequestParserIF
{
  private String              filename;

  private FileItem            uploadItem;

  private boolean             generateError;

  private int                 partIndex    = -1;

  private long                totalFileSize;

  private int                 totalParts;

  private String              uuid;

  private String              originalFilename;

  private String              method;

  private Map<String, String> customParams = new HashMap<>();

  private String              uasComponentOid;

  private String              uploadTarget;

  private String              description;

  private String              tool;

  private Integer             ptEpsg;

  private String              orthoCorrectionModel;

  private String              projectionName;

  private Boolean             processUpload;

  private Boolean             processPtcloud;

  private Boolean             processOrtho;

  private Boolean             processDem;

  private JSONArray           selections;

  public MockRequestParser(String uasComponentOid)
  {
    this.uasComponentOid = uasComponentOid;
    this.uuid = UUID.randomUUID().toString();
    this.uploadTarget = "raw";
    this.description = "Test Upload";
    this.tool = "ODM";
    this.ptEpsg = 4326;
    this.orthoCorrectionModel = "UNKNOWN";
    this.processUpload = false;
    this.processPtcloud = true;
    this.processOrtho = true;
    this.processDem = true;
    this.filename = "test.zip";
    this.originalFilename = "original_test.zip";
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public FileItem getUploadItem()
  {
    return uploadItem;
  }

  public void setUploadItem(FileItem uploadItem)
  {
    this.uploadItem = uploadItem;
  }

  public boolean isGenerateError()
  {
    return generateError;
  }

  public void setGenerateError(boolean generateError)
  {
    this.generateError = generateError;
  }

  public int getPartIndex()
  {
    return partIndex;
  }

  public void setPartIndex(int partIndex)
  {
    this.partIndex = partIndex;
  }

  public long getTotalFileSize()
  {
    return totalFileSize;
  }

  public void setTotalFileSize(long totalFileSize)
  {
    this.totalFileSize = totalFileSize;
  }

  public int getTotalParts()
  {
    return totalParts;
  }

  public void setTotalParts(int totalParts)
  {
    this.totalParts = totalParts;
  }

  public String getUuid()
  {
    return uuid;
  }

  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  public String getOriginalFilename()
  {
    return originalFilename;
  }

  public void setOriginalFilename(String originalFilename)
  {
    this.originalFilename = originalFilename;
  }

  public String getMethod()
  {
    return method;
  }

  public void setMethod(String method)
  {
    this.method = method;
  }

  public Map<String, String> getCustomParams()
  {
    return customParams;
  }

  public void setCustomParams(Map<String, String> customParams)
  {
    this.customParams = customParams;
  }

  public String getUasComponentOid()
  {
    return uasComponentOid;
  }

  public void setUasComponentOid(String uasComponentOid)
  {
    this.uasComponentOid = uasComponentOid;
  }

  public String getUploadTarget()
  {
    return uploadTarget;
  }

  public void setUploadTarget(String uploadTarget)
  {
    this.uploadTarget = uploadTarget;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getTool()
  {
    return tool;
  }

  public void setTool(String tool)
  {
    this.tool = tool;
  }

  @Override
  public Integer getPtEpsg()
  {
    return this.ptEpsg;
  }

  public void setPtEpsg(Integer ptEpsg)
  {
    this.ptEpsg = ptEpsg;
  }

  public String getOrthoCorrectionModel()
  {
    return orthoCorrectionModel;
  }

  public void setOrthoCorrectionModel(String orthoCorrectionModel)
  {
    this.orthoCorrectionModel = orthoCorrectionModel;
  }
  
  @Override
  public String getProjectionName()
  {
    return this.projectionName;
  }
  
  public void setProjectionName(String projectionName)
  {
    this.projectionName = projectionName;
  }

  public Boolean getProcessUpload()
  {
    return processUpload;
  }

  public void setProcessUpload(Boolean processUpload)
  {
    this.processUpload = processUpload;
  }

  public Boolean getProcessPtcloud()
  {
    return processPtcloud;
  }

  public void setProcessPtcloud(Boolean processPtcloud)
  {
    this.processPtcloud = processPtcloud;
  }

  public Boolean getProcessOrtho()
  {
    return processOrtho;
  }

  public void setProcessOrtho(Boolean processOrtho)
  {
    this.processOrtho = processOrtho;
  }

  public Boolean getProcessDem()
  {
    return processDem;
  }

  public void setProcessDem(Boolean processDem)
  {
    this.processDem = processDem;
  }

  public JSONArray getSelections()
  {
    return selections;
  }

  public void setSelections(JSONArray selections)
  {
    this.selections = selections;
  }

  @Override
  public boolean generateError()
  {
    return false;
  }

  @Override
  public boolean isResume()
  {
    return false;
  }

  @Override
  public boolean isFirst()
  {
    return false;
  }

  @Override
  public int getPercent()
  {
    return 100;
  }

  @Override
  public String getProductName()
  {
<<<<<<< HEAD
    // TODO Auto-generated method stub
=======
>>>>>>> refs/remotes/origin/master
    return null;
  }

}
