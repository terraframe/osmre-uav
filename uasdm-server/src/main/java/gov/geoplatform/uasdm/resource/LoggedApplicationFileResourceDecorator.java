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
package gov.geoplatform.uasdm.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;

public class LoggedApplicationFileResourceDecorator extends ApplicationFileResourceDecorator
{
  private static final Logger logger = LoggerFactory.getLogger(LoggedApplicationFileResourceDecorator.class);

  public LoggedApplicationFileResourceDecorator(ApplicationFileResource resource)
  {
    super(resource);
  }
  
  @Override
  public void close()
  {
    logger.info("Closing file resource [" + this.getAbsolutePath() + "]");

    super.close();
  }

  @Override
  public void delete()
  {
    logger.info("Deleting file resource [" + this.getAbsolutePath() + "]");

    super.delete();
  }
}
