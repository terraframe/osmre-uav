package gov.geoplatform.uasdm.service;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.CollectionReport;

public class CollectionReportService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, Integer pageSize, Integer pageNumber, JSONArray criteria)
  {
    return CollectionReport.page(pageSize, pageNumber, criteria).toJSON();
  }

}
