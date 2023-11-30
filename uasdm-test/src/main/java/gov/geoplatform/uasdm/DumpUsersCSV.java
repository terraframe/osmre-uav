package gov.geoplatform.uasdm;

import java.util.ArrayList;
import java.util.List;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;

public class DumpUsersCSV
{
  public static void main(String[] args)
  {
    inReq();
  }
  
  @Request
  public static void inReq()
  {
    GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
    
    List<String> lines = new ArrayList<String>();
    
    for (GeoprismUser user : query.getIterator())
    {
      lines.add(user.getUsername() + "," + user.getUsername() + "," + user.getEmail());
    }
    
    for (String line : lines)
    {
      System.out.println(line);
    }
  }
}
