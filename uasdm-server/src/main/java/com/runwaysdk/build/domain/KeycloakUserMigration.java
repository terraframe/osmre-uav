package com.runwaysdk.build.domain;

import java.io.File;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.FileResource;

public class KeycloakUserMigration
{
  public static void main(String[] args)
  {
    String usersPath = null;
    
    if (args.length > 0)
    {
      usersPath = args[0];
    }
    else
    {
      throw new RuntimeException("Argument [usersPath] is required.");
    }
    
    File users = new File(usersPath);
    
    new KeycloakUserMigration(new FileResource(users)).migrate();
  }
  
  private ApplicationResource users;
  
  public KeycloakUserMigration(ApplicationResource users)
  {
    this.users = users;
  }
  
  public void migrate()
  {
    if (users == null) { throw new RuntimeException("Argument is null"); }
  }
}
