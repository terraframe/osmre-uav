package gov.geoplatform.uasdm.bus;

import java.util.List;

import org.junit.Test;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.view.SiteObject;
import junit.framework.Assert;

public class SiteObjectTest
{

  @Test
  @Request
  public void testGetCollectionSiteObjeccts()
  {
    CollectionQuery query = new CollectionQuery(new QueryFactory());

    List<? extends Collection> collections = query.getIterator().getAll();
    Collection collection = collections.get(0);

    List<SiteObject> objects = collection.getSiteObjects(null);

    System.out.println();
    
    Assert.assertEquals(4, objects.size());    
  }
  
  @Test
  @Request
  public void testGetCollectionSiteObjecctsRaw()
  {
    CollectionQuery query = new CollectionQuery(new QueryFactory());
    
    List<? extends Collection> collections = query.getIterator().getAll();
    Collection collection = collections.get(0);
    
    List<SiteObject> objects = collection.getSiteObjects(Collection.RAW);
    
    System.out.println();
    
    Assert.assertEquals(3, objects.size());    
  }
  
  @Test
  @Request
  public void testGetMissionSiteObjeccts()
  {
    MissionQuery query = new MissionQuery(new QueryFactory());
    
    List<? extends Mission> collections = query.getIterator().getAll();
    Mission collection = collections.get(0);
    
    List<SiteObject> objects = collection.getSiteObjects(null);
    
    System.out.println();
    
    Assert.assertEquals(1, objects.size());    
  }
}
