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

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringInstanceTestClassRunner extends SpringJUnit4ClassRunner
{

  private InstanceTestClassListener InstanceSetupListener;

  public SpringInstanceTestClassRunner(Class<?> clazz) throws InitializationError
  {
    super(clazz);
  }

  @Override
  protected Object createTest() throws Exception
  {
    Object test = super.createTest();
    // Note that JUnit4 will call this createTest() multiple times for each
    // test method, so we need to ensure to call "beforeClassSetup" only once.
    if (test instanceof InstanceTestClassListener && InstanceSetupListener == null)
    {
      InstanceSetupListener = (InstanceTestClassListener) test;
      InstanceSetupListener.beforeClassSetup();
    }
    return test;
  }

  @Override
  public void run(RunNotifier notifier)
  {
    super.run(notifier);

    if (InstanceSetupListener != null)
    {
      try
      {
        InstanceSetupListener.afterClassSetup();
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
  }
}
