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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

/**
 * Represents a component which you are allowed to upload and associate products with
 * 
 * @author nathan
 * @author rrowlands
 */
public interface ImageryComponent
{
  public static final String RAW      = "raw";

  public static final String VIDEO    = "video";

  public static final String PRODUCTS = "products";

  public static final String PTCLOUD  = "ptcloud";

  public static final String DEM      = "dem";

  public static final String ORTHO    = "ortho";

  public static final String GEOREF   = "georef";

  public String getOid();

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget, ProductIF product);

  public List<UasComponentIF> getAncestors();

  public String buildRawKey();

  public UasComponentIF getUasComponent();

  public SiteObjectsResultSet getSiteObjects(String folder, Long pageNumber, Long pageSize);

  public String getS3location();

  public String getS3location(ProductIF product, String folder);

  public String getName();

  /**
   * If the @param uploadTarget is null or blank, then return the raw key.
   * 
   * @param uploadTarget
   * @param productName
   *          TODO
   * @return S3 upload key or the raw upload key
   */
  public default String buildUploadKey(ProductIF product, String uploadTarget)
  {
    if (uploadTarget != null && !uploadTarget.trim().equals(""))
    {
      return this.getS3location() + uploadTarget + "/";
    }
    else
    {
      return this.buildRawKey();
    }
  }

  public static boolean isValidTarget(String uploadTarget)
  {
    List<String> list = Arrays.asList(DEM, GEOREF, ORTHO, PTCLOUD, RAW, VIDEO);

    return list.contains(uploadTarget);
  }

}