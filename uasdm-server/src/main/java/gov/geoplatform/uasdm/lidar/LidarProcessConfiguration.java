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
package gov.geoplatform.uasdm.lidar;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.view.RequestParserIF;

public class LidarProcessConfiguration implements ProcessConfiguration
{
  public static final String GENERATE_COPC              = "generateCopc";

  public static final String GENERATE_TREE_CANOPY_COVER = "generateTreeCanopyCover";

  public static final String GENERATE_GSM               = "generateGSM";

  public static final String GENERATE_TREE_STRUCTURE    = "generateTreeStructure";

  public static final String GENERATE_TERRAIN_MODEL     = "generateTerrainModel";

  public static final String PRODUCT_NAME               = "productName";

  private boolean            generateCopc;

  private boolean            generateTreeCanopyCover;

  private boolean            generateGSM;

  private boolean            generateTreeStructure;

  private boolean            generateTerrainModel;

  private String             productName;

  public LidarProcessConfiguration()
  {
    this("");
  }

  public LidarProcessConfiguration(String outFileNamePrefix)
  {
    this.generateCopc = false;
    this.generateTreeCanopyCover = false;
    this.generateGSM = false;
    this.generateTreeStructure = false;
    this.generateTerrainModel = false;
    this.productName = Long.valueOf(System.currentTimeMillis()).toString();
  }

  @Override
  public ProcessType getType()
  {
    return ProcessType.LIDAR;
  }

  public boolean isGenerateCopc()
  {
    return generateCopc;
  }

  public void setGenerateCopc(boolean generateCopc)
  {
    this.generateCopc = generateCopc;
  }

  public boolean isGenerateTreeCanopyCover()
  {
    return generateTreeCanopyCover;
  }

  public void setGenerateTreeCanopyCover(boolean generateTreeCanopyCover)
  {
    this.generateTreeCanopyCover = generateTreeCanopyCover;
  }

  public boolean isGenerateGSM()
  {
    return generateGSM;
  }

  public void setGenerateGSM(boolean generateGSM)
  {
    this.generateGSM = generateGSM;
  }

  public boolean isGenerateTreeStructure()
  {
    return generateTreeStructure;
  }

  public void setGenerateTreeStructure(boolean generateTreeStructure)
  {
    this.generateTreeStructure = generateTreeStructure;
  }

  public boolean isGenerateTerrainModel()
  {
    return generateTerrainModel;
  }

  public void setGenerateTerrainModel(boolean generateTerrainModel)
  {
    this.generateTerrainModel = generateTerrainModel;
  }

  public String getProductName()
  {
    return productName;
  }

  public void setProductName(String productName)
  {
    this.productName = productName;
  }
  
  public boolean hasProcess()
  {
    return (
        this.generateCopc ||
        this.generateGSM ||
        this.generateTerrainModel ||
        this.generateTreeCanopyCover ||
        this.generateTreeStructure);
  }


  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty(TYPE, this.getType().name());
    object.addProperty(GENERATE_COPC, this.generateCopc);
    object.addProperty(GENERATE_GSM, this.generateGSM);
    object.addProperty(GENERATE_TERRAIN_MODEL, this.generateTerrainModel);
    object.addProperty(GENERATE_TREE_CANOPY_COVER, this.generateTreeCanopyCover);
    object.addProperty(GENERATE_TREE_STRUCTURE, this.generateTreeStructure);
    object.addProperty(PRODUCT_NAME, this.productName);

    return object;
  }

  public static LidarProcessConfiguration parse(String jsonString)
  {
    JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

    LidarProcessConfiguration configuration = new LidarProcessConfiguration();

    if (object.has(PRODUCT_NAME))
    {
      JsonElement element = object.get(PRODUCT_NAME);

      if (!element.isJsonNull())
      {
        configuration.setProductName(object.get(PRODUCT_NAME).getAsString());
      }
    }

    if (object.has(GENERATE_COPC))
    {
      JsonElement element = object.get(GENERATE_COPC);

      if (!element.isJsonNull())
      {
        configuration.setGenerateCopc(object.get(GENERATE_COPC).getAsBoolean());
      }
    }

    if (object.has(GENERATE_GSM))
    {
      JsonElement element = object.get(GENERATE_GSM);

      if (!element.isJsonNull())
      {
        configuration.setGenerateGSM(object.get(GENERATE_GSM).getAsBoolean());
      }
    }

    if (object.has(GENERATE_TERRAIN_MODEL))
    {
      JsonElement element = object.get(GENERATE_TERRAIN_MODEL);

      if (!element.isJsonNull())
      {
        configuration.setGenerateTerrainModel(object.get(GENERATE_TERRAIN_MODEL).getAsBoolean());
      }
    }

    if (object.has(GENERATE_TREE_CANOPY_COVER))
    {
      JsonElement element = object.get(GENERATE_TREE_CANOPY_COVER);

      if (!element.isJsonNull())
      {
        configuration.setGenerateTreeCanopyCover(object.get(GENERATE_TREE_CANOPY_COVER).getAsBoolean());
      }
    }

    if (object.has(GENERATE_TREE_STRUCTURE))
    {
      JsonElement element = object.get(GENERATE_TREE_STRUCTURE);

      if (!element.isJsonNull())
      {
        configuration.setGenerateTreeStructure(object.get(GENERATE_TREE_STRUCTURE).getAsBoolean());
      }
    }

    return configuration;
  }

  public static LidarProcessConfiguration parse(RequestParserIF parser)
  {
    LidarProcessConfiguration configuration = new LidarProcessConfiguration();

    if (!StringUtils.isEmpty(parser.getCustomParams().get(GENERATE_COPC)))
    {
      Boolean value = Boolean.valueOf(parser.getCustomParams().get(GENERATE_COPC));
      configuration.setGenerateCopc(value);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get(GENERATE_GSM)))
    {
      Boolean value = Boolean.valueOf(parser.getCustomParams().get(GENERATE_GSM));
      configuration.setGenerateGSM(value);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get(GENERATE_TERRAIN_MODEL)))
    {
      Boolean value = Boolean.valueOf(parser.getCustomParams().get(GENERATE_TERRAIN_MODEL));
      configuration.setGenerateTerrainModel(value);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get(GENERATE_TREE_CANOPY_COVER)))
    {
      Boolean value = Boolean.valueOf(parser.getCustomParams().get(GENERATE_TREE_CANOPY_COVER));
      configuration.setGenerateTreeCanopyCover(value);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get(GENERATE_TREE_STRUCTURE)))
    {
      Boolean value = Boolean.valueOf(parser.getCustomParams().get(GENERATE_TREE_STRUCTURE));
      configuration.setGenerateTreeStructure(value);
    }

    if (!StringUtils.isEmpty(parser.getCustomParams().get(PRODUCT_NAME)))
    {
      configuration.setProductName(parser.getCustomParams().get(PRODUCT_NAME));
    }

    return configuration;
  }

}
