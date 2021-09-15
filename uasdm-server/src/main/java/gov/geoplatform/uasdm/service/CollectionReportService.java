package gov.geoplatform.uasdm.service;

import java.io.InputStream;

import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.CollectionReport;

public class CollectionReportService
{
  @Request(RequestType.SESSION)
  public JSONObject page(String sessionId, JSONObject criteria)
  {
    return CollectionReport.page(criteria).toJSON();
  }

  @Request(RequestType.SESSION)
  public InputStream exportCSV(String sessionId, JSONObject criteria)
  {
    return CollectionReport.exportCSV(criteria);
  }

}
