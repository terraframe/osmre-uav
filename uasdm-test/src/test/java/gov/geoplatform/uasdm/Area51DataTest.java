package gov.geoplatform.uasdm;

import org.junit.After;
import org.junit.Before;

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

}
