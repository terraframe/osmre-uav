package gov.geoplatform.uasdm;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.account.ExternalProfileQuery;

public class Sandbox
{
  public static void main(String[] args)
  {
    request();
  }
  
  @Request
  public static void request()
  {
    ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
    query.WHERE(query.getEmail().EQi("lyndsay.johnson@usda.gov"));
    System.out.println(query.getCount());
  }
}
