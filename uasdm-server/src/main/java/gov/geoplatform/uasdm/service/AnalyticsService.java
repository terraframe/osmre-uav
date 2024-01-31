package gov.geoplatform.uasdm.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.CollectionStatusQuery;
import gov.geoplatform.uasdm.bus.CollectionQuery;

@Service
public class AnalyticsService
{
  @Request(RequestType.SESSION)
  public InputStream generate(String sessionId, Date since)
  {
    final QueryFactory qf = new QueryFactory();
    final ValueQuery vq = new ValueQuery(qf);
    
    CollectionStatusQuery csq = new CollectionStatusQuery(qf);
    // AbstractWorkflowTaskQuery tq = new AbstractWorkflowTaskQuery(new QueryFactory());
    
    CollectionQuery cq = new CollectionQuery(qf);
    vq.WHERE(cq.getOid().EQ(csq.getComponent()));
    
    vq.WHERE(csq.getStatus().EQ("Failed"));
    
    if (since != null)
    {
      vq.WHERE(csq.getLastUpdateDate().GE(since));
    }
    
    vq.SELECT(cq.getName("colName"));
    vq.SELECT(cq.getS3location("s3Path"));
    vq.SELECT(null);
    
    vq.getIterator();
    
    return new ByteArrayInputStream("test 123".getBytes());
  }
}
