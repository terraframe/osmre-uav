package gov.geoplatform.uasdm.account;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.opencsv.CSVReader;
import com.runwaysdk.RunwayException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.UserInfo;
import net.geoprism.account.ExternalProfile;
import net.geoprism.account.ExternalProfileQuery;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.business.AccountBusinessService;
import net.geoprism.spring.ApplicationContextHolder;

public class CSVUserImporter
{
  private ApplicationResource resource;
  
  private String adminRoleId;
  
  private String fieldWorkerRoleId;
  
  public CSVUserImporter(ApplicationResource resource)
  {
    this.resource = resource;
  }
  
  @Transaction
  public void doImport()
  {
    adminRoleId = Roles.getByKey("Roles.geoprism.admin.Administrator").getOid();
    fieldWorkerRoleId = Roles.getByKey("Roles.geoprism.admin.DashboardBuilder").getOid();
    
    try (CSVReader reader = new CSVReader(new InputStreamReader(resource.openNewStream())))
    {
      Iterator<String[]> it = reader.iterator();
      
      String[] header = it.next();
      
      int iFirstName = -1;
      int iLastName = -1;
      int iEmail = -1;
      int iRole = -1;
      int iOrg = -1;
      for (int i = 0; i < header.length; ++i)
      {
        String s = header[i];
        
        if (StringLike(s, "firstname"))
        {
          iFirstName = i;
        }
        else if (StringLike(s, "lastname"))
        {
          iLastName = i;
        }
        else if (StringLike(s, "email"))
        {
          iEmail = i;
        }
        else if (StringLike(s, "role"))
        {
          iRole = i;
        }
        else if (StringLike(s, "organization") || StringLike(s, "org"))
        {
          iOrg = i;
        }
      }
      
      ValidateField(iFirstName, "firstname");
      ValidateField(iLastName, "lastname");
      ValidateField(iEmail, "email");
      ValidateField(iOrg, "organization");
      
      int lineNum = 2;
      
      while (it.hasNext())
      {
        try
        {
          String[] line = it.next();
          
          String email = line[iEmail];
          
          ExternalProfileQuery query = new ExternalProfileQuery(new QueryFactory());
          query.WHERE(query.getEmail().EQ(email));
          if (query.getCount() > 0) { throw new RuntimeException("A user with the email [" + email + "] already exists. This importer does not support updates at this time."); }
          
          ExternalProfile ep = new ExternalProfile();
          ep.setRemoteId(email);
          ep.setEmail(email);
          
          if (iFirstName != -1) { ep.setFirstName(line[iFirstName]); }
          if (iLastName != -1) { ep.setLastName(line[iLastName]); }
          
          Set<String> roleIds = new HashSet<String>();
          if (iRole != -1)
          {
            String role = line[iRole];
            
            if (StringLike(role, "admin"))
            {
              roleIds.add(adminRoleId);
            }
          }
          else { roleIds.add(fieldWorkerRoleId); }
          
          AccountBusinessService service = ApplicationContextHolder.getBean(AccountBusinessService.class);
          service.applyUserWithRoles(ep, roleIds);
          
          UserInfo info = new UserInfo();
          info.setGeoprismUser(ep);
          info.apply();
          
          if (iOrg != -1)
          {
            Organization organization = Organization.getByCode(line[iOrg]);
  
            info.addOrganization(organization).apply();
          }
        }
        catch(RuntimeException ex)
        {
          UserImportProblem uip = new UserImportProblem();
          uip.setLine(lineNum);
          uip.setReason(RunwayException.localizeThrowable(ex, Session.getCurrentLocale()));
          throw uip;
        }
        
        lineNum++;
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  private void ValidateField(int index, String name)
  {
    if (index != -1) { return; }
    
    UserImportProblem uip = new UserImportProblem();
    uip.setLine(1);
    uip.setReason("Field [" + name + "] is required and should be specified as a header in the first line of the uploaded file.");
    throw uip;
  }
  
  private boolean StringLike(String test, String expected)
  {
    return test.trim().toLowerCase().replaceAll("\\s", "").equals(expected.toLowerCase());
  }
}
