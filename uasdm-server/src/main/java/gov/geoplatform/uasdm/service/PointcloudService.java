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
package gov.geoplatform.uasdm.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class PointcloudService
{
  /**
   * Support for the "legacy" older 'potree' data format, as opposed to the
   * newer 'entwine' format. Older versions of ODM used to generate pointcloud
   * data in this format.
   */
  public static final String LEGACY_POTREE_SUPPORT = Product.ODM_ALL_DIR + "/potree";

  @Request(RequestType.SESSION)
  public String getPointcloudResource(String sessionId, String componentId, String productName)
  {
    UasComponent component = UasComponent.get(componentId);

    return component.getProduct(productName).map(product -> {
      List<DocumentIF> docs = product.getDocuments();

      Optional<String> optional = null;

      if (docs.stream().anyMatch(d -> d.getS3location().endsWith(".copc.laz")))
      {
        return docs.stream().filter(d -> d.getS3location().endsWith(".copc.laz")).findFirst().get().getS3location();
      }
      // POTREE outputs can be prefixed, so you cannot do an exact lookup for
      // the ept.json or other files
      else if ( ( optional = RemoteFileFacade.findSuffix(component.getS3location(product, ODMZipPostProcessor.POTREE), "ept.json") ).isPresent())
      {
        return product.getS3location() + ODMZipPostProcessor.POTREE + "/" + optional //
            .map(key -> key.substring(key.lastIndexOf('/') + 1)). //
            orElse("ept.json");
      }
      else if ( ( optional = RemoteFileFacade.findSuffix(component.getS3location(product, ODMZipPostProcessor.POTREE), "metadata.json") ).isPresent())
      {
        return product.getS3location() + ODMZipPostProcessor.POTREE + "/" + optional //
            .map(key -> key.substring(key.lastIndexOf('/') + 1)). //
            orElse("metadata.json");
      }
      else if ( ( optional = RemoteFileFacade.findSuffix(component.getS3location(product, LEGACY_POTREE_SUPPORT), "cloud.js") ).isPresent())
      {
        return product.getS3location() + LEGACY_POTREE_SUPPORT + "/" + optional //
            .map(key -> key.substring(key.lastIndexOf('/') + 1)). //
            orElse("cloud.js");
      }

      return null;

    }).orElse(null);

  }
}
