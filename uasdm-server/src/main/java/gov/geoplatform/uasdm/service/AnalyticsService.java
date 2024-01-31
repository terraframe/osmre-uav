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

import gov.geoplatform.uasdm.CollectionStatusQuery;

@Service
public class AnalyticsService
{
  @Request(RequestType.SESSION)
  public InputStream generate(String sessionId, Date since)
  {
    final QueryFactory qf = new QueryFactory();
    final ValueQuery vq = new ValueQuery(qf);
    
    CollectionStatusQuery csq = new CollectionStatusQuery(qf);
    
    vq.WHERE(csq.getStatus().EQ("Failed"));
    
    if (since != null)
    {
      vq.WHERE(csq.getLastModificationDate().GE(since));
    }
    
    vq.SELECT(csq.getCollectionName("colName"));
    vq.SELECT(csq.getCollectionS3Path("s3Path"));
    vq.SELECT(csq.getLastModificationDate("lastUpdate"));
    vq.SELECT(csq.getSensorName("sensorName"));
    vq.SELECT(csq.getSensorType("sensorType"));
    vq.SELECT(csq.getFailReason("failReason"));
    vq.SELECT(csq.getCollectionSize("colSize"));
    vq.SELECT(csq.getOdmConfig("odmConfig"));
    
    vq.ORDER_BY(csq.getLastModificationDate("lastUpdate"), SortOrder.DESC);
    
    StringWriter sw = new StringWriter();
    try (CSVWriter csv = new CSVWriter(sw))
    {
      csv.writeNext(new String[] {"collectionName", "collectionSize", "failReason", "lastUpdate", "collectionS3Path", "sensorName", "sensorType", "odmConfig"});
      
      try (OIterator<ValueObject> it = vq.getIterator())
      {
        for (ValueObject vo : it)
        {
          csv.writeNext(new String[] {
              vo.getValue("colName"),
              vo.getValue("colSize"),
              vo.getValue("failReason"),
              vo.getValue("lastUpdate"),
              vo.getValue("s3Path"),
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
