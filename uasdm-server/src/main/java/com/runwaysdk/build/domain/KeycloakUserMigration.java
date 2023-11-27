package com.runwaysdk.build.domain;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.opencsv.CSVReader;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.dataaccess.MdEntityDAOIF;
import com.runwaysdk.dataaccess.MigrationUtil;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.database.ServerIDGenerator;
import com.runwaysdk.dataaccess.metadata.MdEntityDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.util.IDGenerator;
import com.runwaysdk.util.IdParser;

import net.geoprism.GeoprismUser;
import net.geoprism.account.ExternalProfile;

public class KeycloakUserMigration
{
  public static void main(String[] args) throws Exception
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
  
  @Request
  public void migrate() throws Exception
  {
    if (users == null) { throw new RuntimeException("Argument is null"); }
    
    List<String> migrated = new ArrayList<String>();
    
    try (CSVReader reader = new CSVReader(new InputStreamReader(users.openNewStream())))
    {
      String[] line = null;

      while ( ( line = reader.readNext() ) != null)
      {
        String username = line[0];
        String email = line[1];
        
        migrateUser(username, email);
        
        migrated.add(username);
      }
    }
    
    System.out.println("Successfully migrated " + migrated.size() + " users. [" + StringUtils.join(migrated, ", ") + "]");
  }
  
  @Transaction
  public static void migrateUser(String username, String email)
  {
    final List<String> statements = new LinkedList<String>();
    
    // Step 1 : Migrate the old user over to an id with the ExternalProfile RootId
    MdEntityDAOIF newMdClass = MdEntityDAO.getMdEntityDAO(ExternalProfile.CLASS);
    GeoprismUser oldUser = GeoprismUser.getByUsername(username); // TODO : Will the geoprism username be the same as the keycloak username?
    
    String newId = IdParser.buildId(ServerIDGenerator.generateId(IDGenerator.nextID()), newMdClass.getRootId());
    
    if (!MigrationUtil.updateEntityDAOId(oldUser.businessDAO(), newId))
    {
      throw new ProgrammingErrorException("Could not migrate MdEmbeddedGraphClass oid.");
    }
    
    // Step 2 : Create an entry in the external profile table with this new oid
    List<String> columnNames = Arrays.asList("oid", "remote_id", "display_name", "username", "first_name", "last_name", "phone_number", "email");

    List<Object> values = Arrays.asList(newId, username, username, username, oldUser.getFirstName(), oldUser.getLastName(), oldUser.getPhoneNumber(), email);

    List<String> attributeTypes = Arrays.asList(
        MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS,
        MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS,
        MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS
    );

    statements.add(Database.buildSQLinsertStatement("external_profile", columnNames, values, attributeTypes));

    // Step 3 : Delete the entries for the old user in the Users and GeoprismUser tables.
    statements.add(Database.buildSQLDeleteStatement("geoprism_user", newId));
    statements.add(Database.buildSQLDeleteStatement("users", newId));
    
    Database.executeBatch(statements);
  }
  
  private void orientdbMigrateExistingUsers(List<ExternalProfile> newUsers)
  {
    
  }
}
