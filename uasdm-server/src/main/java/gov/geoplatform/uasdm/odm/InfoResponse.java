package gov.geoplatform.uasdm.odm;

import java.util.Date;

import org.json.JSONObject;

public interface InfoResponse extends ODMResponse
{

  String getUUID();

  Date getDateCreated();

  Long getProcessingTime();

  Long getImagesCount();

  JSONObject getOptions();

  ODMStatus getStatus();

  String getStatusError();

}