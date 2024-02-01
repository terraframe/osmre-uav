package gov.geoplatform.uasdm.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.ErrorReportQuery;

@Service
public class AnalyticsService
{
  @Request(RequestType.SESSION)
  public InputStream generate(String sessionId, Date since)
  {
    final QueryFactory qf = new QueryFactory();
    final ValueQuery vq = new ValueQuery(qf);
    
    ErrorReportQuery erq = new ErrorReportQuery(qf);
    
    if (since != null)
    {
      vq.WHERE(erq.getErrorDate().GE(since));
    }
    
    vq.SELECT(erq.getCollectionName("colName"));
    vq.SELECT(erq.getCollectionS3Path("s3Path"));
    vq.SELECT(erq.getErrorDate("errorDate"));
    vq.SELECT(erq.getSensorName("sensorName"));
    vq.SELECT(erq.getSensorType("sensorType"));
    vq.SELECT(erq.getFailReason("failReason"));
    vq.SELECT(erq.getCollectionSize("colSize"));
    vq.SELECT(erq.getOdmConfig("odmConfig"));
    vq.SELECT(erq.getCollectionPocName("pocName"));
    vq.SELECT(erq.getUavSerialNumber("uavSerial"));
    vq.SELECT(erq.getUavFaaId("uavFAA"));
    
    vq.ORDER_BY(erq.getErrorDate("errorDate"), SortOrder.DESC);
    
    StringWriter sw = new StringWriter();
    try (CSVWriter csv = new CSVWriter(sw))
    {
      csv.writeNext(new String[] {"collectionName", "collectionSize", "failReason", "errorDate", "pocName", "collectionS3Path", "uavSerial", "uavFAA", "sensorName", "sensorType", "odmConfig"});
      
      try (OIterator<ValueObject> it = vq.getIterator())
      {
        for (ValueObject vo : it)
        {
          csv.writeNext(new String[] {
              vo.getValue("colName"),
              vo.getValue("colSize"),
              vo.getValue("failReason"),
              vo.getValue("errorDate"),
              vo.getValue("pocName"),
              vo.getValue("s3Path"),
              vo.getValue("uavSerial"),
              vo.getValue("uavFAA"),
              vo.getValue("sensorName"),
              vo.getValue("sensorType"),
              vo.getValue("odmConfig")
          });
        }
      }
      
      csv.flush();
      
      return new ByteArrayInputStream(sw.toString().getBytes());
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
