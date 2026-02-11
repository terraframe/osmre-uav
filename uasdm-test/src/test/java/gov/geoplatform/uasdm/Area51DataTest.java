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
package gov.geoplatform.uasdm;

import org.junit.After;
import org.junit.Before;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.Site;
import gov.geoplatform.uasdm.test.Area51DataSet;

public abstract class Area51DataTest implements InstanceTestClassListener
{
  protected static Area51DataSet testData;

  @Override
  public void beforeClassSetup() throws Exception
  {
    testData = new Area51DataSet();
    testData.setUpSuiteData();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }
  
  @Before
  @Request
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn();
  }

  @After
  @Request
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }
}
