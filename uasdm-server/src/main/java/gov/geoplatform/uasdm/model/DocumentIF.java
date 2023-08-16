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
package gov.geoplatform.uasdm.model;

import java.util.Date;

import org.json.JSONObject;

import com.runwaysdk.ComponentIF;

import gov.geoplatform.uasdm.remote.RemoteFileObject;

public interface DocumentIF extends ComponentIF, JSONSerializable
{
  public static class Metadata
  {
    private String description;

    private String tool;

    private String ptEpsg;

    private String orthoCorrectionModel;

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

    public String getPtEpsg()
    {
      return ptEpsg;
    }

    public void setPtEpsg(String ptEpsg)
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

    public static Metadata build(String description, String tool, String ptEpsg, String orthoCorrectionModel)
    {
      Metadata metadata = new Metadata();
      metadata.setDescription(description);
      metadata.setTool(tool);
      metadata.setPtEpsg(ptEpsg);
      metadata.setOrthoCorrectionModel(orthoCorrectionModel);

      return metadata;
    }
  }

  public String getS3location();

  public String getName();

  public void addGeneratedProduct(ProductIF product);

  public void delete();

  public void delete(boolean removeFromS3);

  public JSONObject toJSON();

  public Boolean getExclude();

  public void setExclude(Boolean exclude);

  public void apply();

  public UasComponentIF getComponent();

  public Date getLastModified();

  public String getDescription();

  public String getTool();

  public String getPtEpsg();

  public String getOrthoCorrectionModel();

  public RemoteFileObject download();

  public boolean isMappable();

}
