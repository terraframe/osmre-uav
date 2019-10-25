package gov.geoplatform.uasdm.model;

import java.util.List;

import org.slf4j.Logger;

import com.runwaysdk.resource.ApplicationResource;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.view.SiteObject;

/**
 * Assumes that this is implemented ONLY by {@link UasComponent}
 * 
 * @author nathan
 *
 */
public interface ImageryComponent
{
  public static final int    BUFFER_SIZE = 1024;

  public static final String RAW         = "raw";

  public static final String PTCLOUD     = "ptcloud";

  public static final String DEM         = "dem";

  public static final String ORTHO       = "ortho";

  public static final String GEOREF      = "georef";

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget);

  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget);

  public Logger getLog();

  public List<UasComponentIF> getAncestors();

  public String buildRawKey();

  public UasComponentIF getUasComponent();

  public String getStoreName(String key);

  public List<SiteObject> getSiteObjects(String folder);

  public void createImageServices();

  public String getS3location();

  public String getName();

  /**
   * If the @param uploadTarget is null or blank, then return the raw key.
   * 
   * @param uploadTarget
   * 
   * @return S3 upload key or the raw upload key
   */
  public default String buildUploadKey(String uploadTarget)
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

  public String getOid();
}