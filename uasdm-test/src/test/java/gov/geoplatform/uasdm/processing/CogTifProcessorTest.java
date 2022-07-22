package gov.geoplatform.uasdm.processing;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.test.Area51DataSet;

public class CogTifProcessorTest
{
  
  private static Area51DataSet testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = new Area51DataSet();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }
  
  @Before
  public void setUp()
  {
    testData.setUpInstanceData();
  }

  @After
  public void tearDown()
  {
    testData.tearDownInstanceData();
  }
  
  @Test
  @Request
  public void testProcess()
  {
    StatusMonitorIF monitor = new InMemoryMonitor();
    
    String s3Path = "";
    
    Product product = null;
    
    CogTifProcessor processor = new CogTifProcessor(s3Path, product, Area51DataSet.COLLECTION_FISHBED.getServerObject(), monitor);
    
    throw new UnsupportedOperationException();
  }
}
