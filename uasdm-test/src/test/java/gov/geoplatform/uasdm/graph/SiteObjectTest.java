package gov.geoplatform.uasdm.graph;

import java.util.List;

import org.junit.Test;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import junit.framework.Assert;

public class SiteObjectTest
{

  @Test
  @Request
  public void testGetCollectionSiteObjeccts()
  {
    final List<Collection> collections = getCollections();

    if (collections.size() > 0)
    {
      Collection collection = collections.get(0);

      SiteObjectsResultSet result = collection.getSiteObjects(null, 1, 100);
      final List<SiteObject> objects = result.getObjects();

      System.out.println();

      Assert.assertEquals(4, objects.size());
    }
  }

  @Test
  @Request
  public void testGetCollectionSiteObjecctsRaw()
  {
    final List<Collection> collections = getCollections();

    if (collections.size() > 0)
    {
      Collection collection = collections.get(0);

      SiteObjectsResultSet result = collection.getSiteObjects(Collection.RAW, 1, 100);
      final List<SiteObject> objects = result.getObjects();

      System.out.println();

      Assert.assertEquals(3, objects.size());
    }
  }

  @Test
  @Request
  public void testGetMissionSiteObjeccts()
  {
    final List<Mission> missions = getMissions();

    if (missions.size() > 0)
    {
      Mission mission = missions.get(0);

      SiteObjectsResultSet result = mission.getSiteObjects(null, 1, 100);
      final List<SiteObject> objects = result.getObjects();

      System.out.println();

      Assert.assertEquals(1, objects.size());
    }
  }

  public List<Collection> getCollections()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Collection.CLASS);

    final StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    final GraphQuery<Collection> query = new GraphQuery<Collection>(statement.toString());
    final List<Collection> collections = query.getResults();
    return collections;
  }

  public List<Mission> getMissions()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Mission.CLASS);

    final StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    final GraphQuery<Mission> query = new GraphQuery<Mission>(statement.toString());
    final List<Mission> missions = query.getResults();
    return missions;
  }
}
