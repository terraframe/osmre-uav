package gov.geoplatform.uasdm.odm;

import java.util.List;

import org.json.JSONArray;

import gov.geoplatform.uasdm.bus.ImageryWorkflowTaskIF;

public interface ODMProcessingTaskIF extends ImageryWorkflowTaskIF
{
  public String getFilePrefix();

  public void setFilePrefix(String value);

  public String getOdmUUID();

  public String getUploadId();

  public void setOdmUUID(String value);

  public void setOdmOutput(String value);

  public String getImageryComponentOid();

  /**
   * Writes the ODM output to a log file on S3, if supported by the individual
   * task implementation.
   * 
   * @param odmOutput
   */
  public void writeODMtoS3(JSONArray odmOutput);

  public List<String> getFileList();
}
