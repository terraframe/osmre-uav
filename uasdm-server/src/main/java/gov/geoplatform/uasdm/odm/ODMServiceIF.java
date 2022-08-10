package gov.geoplatform.uasdm.odm;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

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
  NewResponse taskNew(ApplicationResource images, boolean isMultispectral);

  NewResponse taskNewInit(int imagesCount, boolean isMultispectral);

  ODMResponse taskNewUpload(String uuid, ApplicationResource image);

  ODMResponse taskNewCommit(String uuid);

  InfoResponse taskInfo(String uuid);

}