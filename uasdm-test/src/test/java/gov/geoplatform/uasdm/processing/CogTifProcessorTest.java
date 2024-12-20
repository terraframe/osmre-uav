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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.resource.ClasspathResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

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

  @SuppressWarnings("resource")
  @Test
  @Request
  public void testValidateCogDev()
  {
    StatusMonitorIF monitor = new InMemoryMonitor();

    CogTifValidator validator = new CogTifValidator(monitor);

    try (CloseableFile f = new ClasspathResource("all/odm_orthophoto/odm_orthophoto.cog.tif").openNewFile()) {
      Assert.assertTrue(validator.isValidCog(new FileResource(f)));
    }
    try (CloseableFile f = new ClasspathResource("all/odm_orthophoto/odm_orthophoto.tif").openNewFile()) {
      Assert.assertFalse(validator.isValidCog(new FileResource(f)));
    }
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
