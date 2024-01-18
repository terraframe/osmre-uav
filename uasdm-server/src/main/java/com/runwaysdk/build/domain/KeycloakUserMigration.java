/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.runwaysdk.build.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.dataaccess.MdEntityDAOIF;
import com.runwaysdk.dataaccess.MigrationUtil;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.database.ServerIDGenerator;
import com.runwaysdk.dataaccess.metadata.MdEntityDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.util.IDGenerator;
import com.runwaysdk.util.IdParser;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.account.ExternalProfile;

public class KeycloakUserMigration
{
  public static void main(String[] args) throws Exception
  {
    try
    {
      new KeycloakUserMigration().migrate();
    }
    finally
    {
      CacheShutdown.shutdown();
    }
  }
  
  public KeycloakUserMigration()
  {
  }
  
  @Request
  public void migrate() throws Exception
  {
    List<String> migrated = new ArrayList<String>();
    
    GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
    
    for (GeoprismUser user : query.getIterator())
    {
      if (user.getUsername().equals(("admin"))) { continue; }
      
      migrateUser(user);
      
      migrated.add(user.getUsername());
    }
    
    String msg = "Successfully migrated " + migrated.size() + " users.";
    
    if (migrated.size() < 30)
    {
      msg = msg + " [" + StringUtils.join(migrated, ", ") + "]";
    }
    
    System.out.println(msg);
  }
  
  @Transaction
  public static void migrateUser(GeoprismUser oldUser)
  {
    final List<String> statements = new LinkedList<String>();
    
    // Step 1 : Migrate the old user over to an id with the ExternalProfile RootId
    MdEntityDAOIF newMdClass = MdEntityDAO.getMdEntityDAO(ExternalProfile.CLASS);
    
    String newId = IdParser.buildId(ServerIDGenerator.generateId(IDGenerator.nextID()), newMdClass.getRootId());
    
    if (!MigrationUtil.updateEntityDAOId(oldUser.businessDAO(), newId))
    {
      throw new ProgrammingErrorException("Could not migrate user [" + oldUser.getUsername() + "].");
    }
    
    // Step 2 : Create an entry in the external profile table with this new oid
    List<String> columnNames = Arrays.asList("oid", "remote_id", "display_name", "username", "first_name", "last_name", "phone_number", "email");

    List<Object> values = Arrays.asList(newId, oldUser.getUsername(), oldUser.getUsername(), oldUser.getUsername(), oldUser.getFirstName(), oldUser.getLastName(), oldUser.getPhoneNumber(), oldUser.getEmail());

    List<String> attributeTypes = Arrays.asList(
        MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS,
        MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS,
        MdAttributeCharacterInfo.CLASS, MdAttributeCharacterInfo.CLASS
    );

    statements.add(Database.buildSQLinsertStatement("external_profile", columnNames, values, attributeTypes));

    // Step 3 : Delete the entries for the old user in the Users and GeoprismUser tables.
    statements.add(Database.buildSQLDeleteStatement("geoprism_user", newId));
    statements.add(Database.buildSQLDeleteStatement("users", newId));
    
    // Step 4 : There is type information encoded in the Actor table. We need to change the explicit type information
    statements.add("UPDATE actor SET type='net.geoprism.account.ExternalProfile' WHERE oid='" + newId + "'");
    
    Database.executeBatch(statements);
  }
}
