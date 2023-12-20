package com.runwaysdk.build.domain;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.Roles;

import net.geoprism.account.ExternalProfile;

public class KeycloakAdminUserRole
{
  public static void main(String[] args)
  {
    new KeycloakAdminUserRole().inReq();
  }
  
  @Request
  public void inReq()
  {
    trans();
  }
  
  @Transaction
  public void trans()
  {
    ExternalProfile admin = ExternalProfile.getByKey("fc1606c0-d269-3ad0-9130-f76d3100052a");
    
    admin.addAssignedRole(Roles.findRoleByName("geoprism.admin.Administrator")).apply();
  }
}
