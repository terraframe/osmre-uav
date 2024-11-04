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
package gov.geoplatform.uasdm.processing;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.test.Area51DataSet;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
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

    Assert.assertTrue(validator.isValidCog(new FileResource(new File("/tmp/odm_orthophoto.cog.tif"))));

    Assert.assertFalse(validator.isValidCog(new FileResource(new File("/tmp/odm_orthophoto.tif"))));
  }

//  @Test
//  @Request
//  public void testValidateCogProd()
//  {
//    String[] cmds = CogTifValidator.getCogValidatorCommand(AppProperties.PROD_VALIDATOR_CMD, "test123");
//
//    Assert.assertEquals("python3 /usr/local/tomcat/validate_cloud_optimized_geotiff.py test123", StringUtils.join(cmds, " "));
//  }

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
