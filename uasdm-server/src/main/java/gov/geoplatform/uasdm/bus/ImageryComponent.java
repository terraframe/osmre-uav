package gov.geoplatform.uasdm.bus;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;

/**
 * Assumes that this is implemented ONLY by {@link UasComponent}
 * 
 * @author nathan
 *
 */
public interface ImageryComponent
{
  public static final int    BUFFER_SIZE      = 1024;
  
  public static final String RAW              = "raw";

  public static final String PTCLOUD          = "ptcloud";

  public static final String DEM              = "dem";
  
  public static final String ORTHO            = "ortho";
  
  public static final String GEOREF           = "georef";

  
  public void uploadArchive(AbstractWorkflowTask task, File archive, String uploadTarget);
  
  public void uploadZipArchive(AbstractWorkflowTask task, File archive, String uploadTarget);
  
  public Logger getLog();
  
  public List<UasComponent> getAncestors();
  
  public String buildRawKey();
  
  public UasComponent getUasComponent();

  public String getStoreName(String key);
  
  public void createImageServices();
  
  public String getS3location();
  
  public String getName();
  
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
}