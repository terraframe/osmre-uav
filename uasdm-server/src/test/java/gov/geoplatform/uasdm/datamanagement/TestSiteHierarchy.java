package gov.geoplatform.uasdm.datamanagement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;


public class TestSiteHierarchy
{

  
  @BeforeClass
  @Request
  public static void classSetUp()
  {
    createSiteHierarchyTransaction();
  }
  
  @Transaction
  private static void createSiteHierarchyTransaction()
  {
    Site site = new Site();
    site.setName("Cottonwood");
    site.apply();
    
    Project project1 = new Project();
    project1.setName("Project 1");
    project1.apply();
    
    site.addProjects(project1).apply();
    
    Mission mission1 = new Mission();
    mission1.setName("Mission 1");
    mission1.apply();
    
    project1.addMissions(mission1).apply();
    
    Collection collection1 = new Collection();
    collection1.setName("Collection 1");
    collection1.apply();
    
    mission1.addCollections(collection1).apply();
    
  }
  
  
  @AfterClass
  @Request
  public static void classTearDown()
  {
    classTearDownTransaction();
  }
  
  @Transaction
  public static void classTearDownTransaction()
  {
    QueryFactory qf = new QueryFactory();
    
    SiteQuery sq = new SiteQuery(qf);
    
    OIterator<? extends Site> i = sq.getIterator();
    
    try
    {
      for(Site site : i)
      {
        site.delete();
        System.out.println("Site deleted: "+site.getName());
      }
    }
    finally
    {
      i.close();
    }
  }
  
  
  @Test
  public void test1()
  {
    System.out.println("Hello World!");
  }
  
}
