package gov.geoplatform.uasdm.service;

import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
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
      if (RemoteFileFacade.objectExists(component.getS3location(product, ODMZipPostProcessor.POTREE) + "ept.json"))
      {
        return product.getS3location() + ODMZipPostProcessor.POTREE + "/" + "ept.json";
      }
      else if (RemoteFileFacade.objectExists(component.getS3location(product, ODMZipPostProcessor.POTREE) + "metadata.json"))
      {
        return product.getS3location() + ODMZipPostProcessor.POTREE + "/" + "metadata.json";
      }
      else if (RemoteFileFacade.objectExists(component.getS3location(product, LEGACY_POTREE_SUPPORT) + "cloud.js"))
      {
        return product.getS3location() + LEGACY_POTREE_SUPPORT + "/" + "cloud.js";
      }

      return null;

    }).orElse(null);

  }
}
