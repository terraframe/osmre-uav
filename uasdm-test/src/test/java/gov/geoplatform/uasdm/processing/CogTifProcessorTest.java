package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.test.Area51DataSet;

public class CogTifProcessorTest
{

  // private static Area51DataSet testData;
  //
  // @BeforeClass
  // public static void setUpClass()
  // {
  // testData = new Area51DataSet();
  // testData.setUpMetadata();
  // }
  //
  // @AfterClass
  // public static void cleanUpClass()
  // {
  // if (testData != null)
  // {
  // testData.tearDownMetadata();
  // }
  // }
  //
  // @Before
  // public void setUp()
  // {
  // testData.setUpInstanceData();
  // }
  //
  // @After
  // public void tearDown()
  // {
  // testData.tearDownInstanceData();
  // }

  @Test
  @Request
  public void testValidateCogDev()
  {
    StatusMonitorIF monitor = new InMemoryMonitor();

    CogTifValidator validator = new CogTifValidator(monitor);

    Assert.assertTrue(validator.isValidCog(new File("/tmp/odm_orthophoto.cog.tif")));

    Assert.assertFalse(validator.isValidCog(new File("/tmp/odm_orthophoto.tif")));
  }

  @Test
  @Request
  public void testValidateCogProd()
  {
    String[] cmds = CogTifValidator.getCogValidatorCommand(AppProperties.PROD_VALIDATOR_CMD, "test123");

    Assert.assertEquals("python3 /usr/local/tomcat/validate_cloud_optimized_geotiff.py test123", StringUtils.join(cmds, " "));
  }

  @Test(expected = UnsupportedOperationException.class)
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
