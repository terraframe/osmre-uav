/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */

package gov.geoplatform.uasdm.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.runwaysdk.ClientSession;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.resource.ClasspathResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.VaultFileQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdClass;
import com.runwaysdk.system.metadata.MdClassQuery;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.ExecutableJobQuery;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.UserInfoQuery;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.graph.UasComponent;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.NullGeoserverService;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierQuery;

abstract public class TestDataSet
{
  public static interface RequestExecutor
  {
    public void execute(ClientRequestIF request) throws Exception;
  }

  public static final String                 ADMIN_USER_NAME                 = "admin";

  public static final String                 ADMIN_PASSWORD                  = "_nm8P4gfdWxGqNRQ#8";

  public static final TestUserInfo           USER_ADMIN                      = new TestUserInfo(ADMIN_USER_NAME, ADMIN_PASSWORD, null, null);

  public static final String                 WKT_DEFAULT_POLYGON             = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";

  public static final String                 DEFAULT_SITE_POINT              = "Point(-104.06395534307124, 39.79736514172822)";

  public static final String                 WKT_DEFAULT_MULTIPOLYGON        = "MULTIPOLYGON (((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2)))";

  public static final String                 WKT_POLYGON_2                   = "MULTIPOLYGON(((1 1,10 1,10 10,1 10,1 1),(2 2, 3 2, 3 3, 2 3,2 2)))";

  protected int                              debugMode                       = 0;
  
  protected ArrayList<TestSensorInfo>        managedSensors                  = new ArrayList<TestSensorInfo>();

  protected ArrayList<TestPlatformInfo>      managedPlatforms                = new ArrayList<TestPlatformInfo>();

  protected ArrayList<TestUavInfo>           managedUavs                     = new ArrayList<TestUavInfo>();

  protected ArrayList<TestSiteInfo>          managedSites                    = new ArrayList<TestSiteInfo>();
  
  protected ArrayList<TestSiteInfo>          managedSitesExtras              = new ArrayList<TestSiteInfo>();
  
  protected ArrayList<TestProjectInfo>       managedProjects                 = new ArrayList<TestProjectInfo>();
  
  protected ArrayList<TestProjectInfo>       managedProjectsExtras           = new ArrayList<TestProjectInfo>();
 
  protected ArrayList<TestMissionInfo>       managedMissions                 = new ArrayList<TestMissionInfo>();
  
  protected ArrayList<TestMissionInfo>       managedMissionsExtras           = new ArrayList<TestMissionInfo>();
  
  protected ArrayList<TestCollectionInfo>    managedCollections              = new ArrayList<TestCollectionInfo>();
  
  protected ArrayList<TestCollectionInfo>    managedCollectionsExtras        = new ArrayList<TestCollectionInfo>();

  protected ArrayList<TestUserInfo>          managedUsers                    = new ArrayList<TestUserInfo>();

  public ClientSession                       clientSession                   = null;

  public ClientRequestIF                     clientRequest                   = null;

  abstract public String getTestDataKey();

  public TestDataSet()
  {
    checkDuplicateClasspathResources();
    LocalProperties.setSkipCodeGenAndCompile(true);
    GeoserverFacade.setService(new NullGeoserverService());
  }

  public ArrayList<TestSiteInfo> getManagedSites()
  {
    ArrayList<TestSiteInfo> all = new ArrayList<TestSiteInfo>();

    all.addAll(managedSites);
    all.addAll(managedSitesExtras);

    return all;
  }

  public ArrayList<TestUserInfo> getManagedUsers()
  {
    ArrayList<TestUserInfo> all = new ArrayList<TestUserInfo>();

    all.addAll(managedUsers);

    return all;
  }

  public void logIn()
  {
    this.logIn(null);
  }

  public void logIn(TestUserInfo user)
  {
    if (user == null)
    {
      this.clientSession = ClientSession.createUserSession(ADMIN_USER_NAME, ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
      this.clientRequest = clientSession.getRequest();
    }
    else
    {
      this.clientSession = ClientSession.createUserSession(user.getUsername(), user.getPassword(), new Locale[] { CommonProperties.getDefaultLocale() });
      this.clientRequest = clientSession.getRequest();
    }
  }

  public void logOut()
  {
    if (clientSession != null && clientRequest != null && clientRequest.isLoggedIn())
    {
      clientSession.logout();
    }
  }

  @Request
  public void setUpMetadata()
  {
    tearDownMetadata();

    setUpMetadataInTrans();

    setUpClassRelationships();
  }

  public void setUpClassRelationships()
  {

  }

  @Transaction
  protected void setUpMetadataInTrans()
  {
    for (TestUserInfo user : managedUsers)
    {
      user.apply();
    }
  }

  @Request
  public void setUpInstanceData()
  {
    tearDownInstanceData();

    setUpTestInTrans();

    setUpRelationships();

    setUpAfterApply();
  }

//  @Transaction
  protected void setUpTestInTrans()
  {
    for (TestSensorInfo obj : managedSensors)
    {
      obj.apply();
    }
    
    for (TestPlatformInfo obj : managedPlatforms)
    {
      obj.apply();
    }
    
    for (TestUavInfo obj : managedUavs)
    {
      obj.apply();
    }
    
    for (TestSiteInfo obj : managedSites)
    {
      obj.apply();
    }
    
    for (TestProjectInfo obj : managedProjects)
    {
      obj.apply();
    }
    
    for (TestMissionInfo obj : managedMissions)
    {
      obj.apply();
    }
    
    for (TestCollectionInfo obj : managedCollections)
    {
      obj.apply();
    }
  }

  protected void setUpRelationships()
  {

  }

  protected void setUpAfterApply()
  {

  }

  @Request
  public void tearDownMetadata()
  {
    cleanUpClassInTrans();
  }

  @Transaction
  protected void cleanUpClassInTrans()
  {
    for (TestSiteInfo obj : managedSites)
    {
      obj.delete();
    }
    
    for (TestProjectInfo obj : managedProjects)
    {
      obj.delete();
    }
    
    for (TestMissionInfo obj : managedMissions)
    {
      obj.delete();
    }
    
    for (TestCollectionInfo obj : managedCollections)
    {
      obj.delete();
    }
    
    for (TestUserInfo user : this.getManagedUsers())
    {
      user.delete();
    }
  }

  @Request
  public void reloadPermissions()
  {
    SessionFacade.getSessionForRequest(this.clientRequest.getSessionId()).reloadPermissions();
  }

  public void tearDownInstanceData()
  {
    tearDownInstanceDataInRequest();
  }

  @Request
  public void tearDownInstanceDataInRequest()
  {
    cleanUpTestInTrans();
  }

  @Transaction
  protected void cleanUpTestInTrans()
  {
    // for (TestGeoObjectInfo go : managedGeoObjectInfos)
    // {
    // go.delete();
    // }
    // for (TestGeoObjectInfo go : managedGeoObjectInfosExtras)
    // {
    // go.delete();
    // }

    deleteAllWorkflowTasks();
    deleteAllSites();
    deleteAllSensors();
    deleteAllUavs();
    deleteAllPlatforms();

    managedSitesExtras = new ArrayList<TestSiteInfo>();
    managedProjectsExtras = new ArrayList<TestProjectInfo>();
    managedMissionsExtras = new ArrayList<TestMissionInfo>();
    managedCollectionsExtras = new ArrayList<TestCollectionInfo>();
  }
  
  @Request
  private void deleteAllUavs()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM uav");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    for (VertexObject vObject : vObjects)
    {
      vObject.delete();
    }
  }
  
  @Request
  private void deleteAllPlatforms()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM platform0");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    for (VertexObject vObject : vObjects)
    {
      vObject.delete();
    }
  }

  @Request
  private void deleteAllSensors()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM sensor0");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    for (VertexObject vObject : vObjects)
    {
      vObject.delete();
    }
  }
  
  @Request
  private void deleteAllWorkflowTasks()
  {
    AbstractWorkflowTaskQuery query = new AbstractWorkflowTaskQuery(new QueryFactory());
    
    OIterator<? extends AbstractWorkflowTask> it = query.getIterator();
    
    try
    {
      while (it.hasNext())
      {
        AbstractWorkflowTask task = it.next();
        
        task.delete();
      }
    }
    finally
    {
      it.close();
    }
  }
  
  @Request
  private void deleteAllSites()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM site0");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    for (VertexObject vObject : vObjects)
    {
      vObject.delete();
    }
  }

  // private void rebuildAllpaths()
  // {
  // Classifier.getStrategy().initialize(ClassifierIsARelationship.CLASS);
  // Universal.getStrategy().initialize(com.runwaysdk.system.gis.geo.AllowedIn.CLASS);
  // GeoEntity.getStrategy().initialize(com.runwaysdk.system.gis.geo.LocatedIn.CLASS);
  //
  // if (new AllowedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
  // {
  // Universal.getStrategy().reinitialize(com.runwaysdk.system.gis.geo.AllowedIn.CLASS);
  // }
  //
  // if (new LocatedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
  // {
  // GeoEntity.getStrategy().reinitialize(com.runwaysdk.system.gis.geo.LocatedIn.CLASS);
  // }
  //
  // if (new ClassifierIsARelationshipAllPathsTableQuery(new
  // QueryFactory()).getCount() == 0)
  // {
  // Classifier.getStrategy().reinitialize(ClassifierIsARelationship.CLASS);
  // }
  // }

  @Request
  public static UasComponent getComponent(String folderName)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM uas_component0 WHERE folderName=:folderName");

    final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(statement.toString());
    query.setParameter("folderName", folderName);
    
    return query.getSingleResult();
  }
  
  @Request
  public static Sensor getSensor(String name)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM sensor0 WHERE name=:name");

    final GraphQuery<Sensor> query = new GraphQuery<Sensor>(statement.toString());
    query.setParameter("name", name);
    
    return query.getSingleResult();
  }
  
  @Request
  public static Platform getPlatform(String name)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM platform0 WHERE name=:name");

    final GraphQuery<Platform> query = new GraphQuery<Platform>(statement.toString());
    query.setParameter("name", name);
    
    return query.getSingleResult();
  }
  
  @Request
  public static UAV getUav(String serialNumber)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM uav0 WHERE serialNumber=:serialNumber");

    final GraphQuery<UAV> query = new GraphQuery<UAV>(statement.toString());
    query.setParameter("serialNumber", serialNumber);
    
    return query.getSingleResult();
  }
  
  @Request
  public static void deleteAllSchedulerData()
  {
    JobHistoryQuery jhq = new JobHistoryQuery(new QueryFactory());

    OIterator<? extends JobHistory> it = jhq.getIterator();

    while (it.hasNext())
    {
      it.next().delete();
    }
    
    ExecutableJobQuery ejq = new ExecutableJobQuery(new QueryFactory());
    
    try (OIterator<? extends ExecutableJob> jobit = ejq.getIterator())
    {
      while (jobit.hasNext())
      {
        jobit.next().delete();
      }
    }
  }
  
  @Request
  public static void deleteAllVaultFiles()
  {
    VaultFileQuery vfq = new VaultFileQuery(new QueryFactory());

    OIterator<? extends VaultFile> it = vfq.getIterator();

    while (it.hasNext())
    {
      it.next().delete();
    }
  }

  @Request
  public static void deleteGeoEntity(String key)
  {
    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    geq.WHERE(geq.getKeyName().EQ(key));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while (git.hasNext())
      {
        GeoEntity ge = git.next();

        ge.delete();
      }
    }
    finally
    {
      git.close();
    }
  }

  public static Classifier getClassifierIfExist(String classifierId)
  {
    ClassifierQuery query = new ClassifierQuery(new QueryFactory());
    query.WHERE(query.getClassifierId().EQ(classifierId));
    OIterator<? extends Classifier> it = query.getIterator();
    try
    {
      while (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

  @Request
  public static GeoprismUser createUser(String username, String password, String email, String[] roleNameArray)
  {
    GeoprismUser geoprismUser = new GeoprismUser();
    geoprismUser.setUsername(username);
    geoprismUser.setPassword(password);
    geoprismUser.setFirstName(username);
    geoprismUser.setLastName(username);
    geoprismUser.setEmail(email);
    geoprismUser.apply();

    if (roleNameArray != null)
    {
      List<Roles> newRoles = new LinkedList<Roles>();

      Set<String> roleIdSet = new HashSet<String>();
      for (String roleName : roleNameArray)
      {
        Roles role = Roles.findRoleByName(roleName);

        roleIdSet.add(role.getOid());
        newRoles.add(role);
      }

      UserDAOIF user = UserDAO.get(geoprismUser.getOid());

//      Set<String> organizationSet = new HashSet<String>();
      for (Roles role : newRoles)
      {
        RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
        roleDAO.assignMember(user);

//        RegistryRole registryRole = new RegistryRoleConverter().build(role);
//        if (registryRole != null)
//        {
//          String organizationCode = registryRole.getOrganizationCode();
//
//          if (organizationCode != null && !organizationCode.equals("") && !organizationSet.contains(organizationCode))
//          {
//            Organization organization = Organization.getByCode(organizationCode);
//            organization.addUsers(geoprismUser).apply();
//            organizationSet.add(organizationCode);
//          }
//        }
      }
    }

    UserInfo info = new UserInfo();
    info.setGeoprismUser(geoprismUser);
    info.apply();

    return geoprismUser;
  }

  @Request
  public static void deleteUser(String username)
  {
    QueryFactory qf = new QueryFactory();

    ValueQuery vq = new ValueQuery(qf);

    UserInfoQuery uiq = new UserInfoQuery(qf);

    GeoprismUserQuery guq = new GeoprismUserQuery(qf);

    vq.SELECT(uiq.getOid("userInfoOid"));
    vq.SELECT(guq.getOid("geoprismUserOid"));

    vq.WHERE(guq.getUsername().EQ(username));
    vq.AND(uiq.getGeoprismUser().EQ(guq));

    OIterator<? extends ValueObject> it = vq.getIterator();

    try
    {
      while (it.hasNext())
      {
        ValueObject vo = it.next();

        UserInfo ui = UserInfo.get(vo.getValue("userInfoOid"));
        GeoprismUser gu = GeoprismUser.get(vo.getValue("geoprismUserOid"));

        // Delete all referenced VaultFiles
        VaultFileQuery vfq = new VaultFileQuery(new QueryFactory());
        vfq.WHERE(vfq.getOwner().EQ(gu));
        OIterator<? extends VaultFile> vfit = vfq.getIterator();
        try
        {
          while (vfit.hasNext())
          {
            vfit.next().delete();
          }
        }
        finally
        {
          vfit.close();
        }

        ui.delete();
        gu.delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Request
  public static void deleteClassifier(String classifierId)
  {
    Classifier clazz = getClassifierIfExist(classifierId);

    if (clazz != null)
    {
      clazz.delete();
    }
  }

  public static MdClass getMdClassIfExist(String pack, String type)
  {
    MdClassQuery mbq = new MdClassQuery(new QueryFactory());
    mbq.WHERE(mbq.getPackageName().EQ(pack));
    mbq.WHERE(mbq.getTypeName().EQ(type));
    OIterator<? extends MdClass> it = mbq.getIterator();
    try
    {
      while (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

  @Request
  public static void deleteMdClass(String pack, String type)
  {
    MdClass mdBiz = getMdClassIfExist(pack, type);

    if (mdBiz != null)
    {
      mdBiz.delete();
    }
  }

  @Request
  public static Universal getUniversalIfExist(String universalId)
  {
    UniversalQuery uq = new UniversalQuery(new QueryFactory());
    uq.WHERE(uq.getUniversalId().EQ(universalId));
    OIterator<? extends Universal> it = uq.getIterator();
    try
    {
      while (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

  @Request
  public static void deleteUniversal(String code)
  {
    Universal uni = getUniversalIfExist(code);

    if (uni != null)
    {
      uni = Universal.get(uni.getOid());
      uni.delete();
    }
  }

  /**
   * Duplicate resources on the classpath may cause issues. This method checks
   * the runwaysdk directory because conflicts there are most common.
   */
  public static void checkDuplicateClasspathResources()
  {
    Set<ClasspathResource> existingResources = new HashSet<ClasspathResource>();

    List<ClasspathResource> resources = ClasspathResource.getResourcesInPackage("runwaysdk");
    for (ClasspathResource resource : resources)
    {
      ClasspathResource existingRes = null;

      for (ClasspathResource existingResource : existingResources)
      {
        if (existingResource.getAbsolutePath().equals(resource.getAbsolutePath()))
        {
          existingRes = existingResource;
          break;
        }
      }

      if (existingRes != null)
      {
        System.out.println("WARNING : resource path [" + resource.getAbsolutePath() + "] is overloaded.  [" + resource.getURL() + "] conflicts with existing resource [" + existingRes.getURL() + "].");
      }

      existingResources.add(resource);
    }
  }

  public static void runAsUser(TestUserInfo user, RequestExecutor executor)
  {
    ClientSession session = null;

    try
    {
      session = ClientSession.createUserSession(user.getUsername(), user.getPassword(), new Locale[] { CommonProperties.getDefaultLocale() });

      ClientRequestIF request = session.getRequest();

      try
      {
        executor.execute(request);
      }
      catch (RuntimeException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
    finally
    {
      if (session != null)
      {
        session.logout();
      }
    }
  }
}