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
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.ResourceException;

public class LoggingArchiveFileResource extends ArchiveFileResource
{
  private static final Logger logger = LoggerFactory.getLogger(LoggingArchiveFileResource.class);

  public LoggingArchiveFileResource(ApplicationFileResource archive)
  {
    super(archive);
  }

  @Override
  public CloseableFile extract() throws ResourceException
  {
    boolean log = this.extractedParent == null;

    CloseableFile file = super.extract();

    if (log)
    {
      try
      {
        logger.info("Extracting archive resource [" + this.getAbsolutePath() + "] to [" + file.getAbsolutePath() + "]");
      }
      catch (Exception e)
      {

      }
    }

    return file;
  }

  @Override
  public void close()
  {
    logger.info("Deleting archive resource [" + this.getAbsolutePath() + "]");

    if (this.extractedParent != null)
    {
      logger.info("Deleting extracted parent [" + this.extractedParent.getAbsolutePath() + "]");
    }

    super.close();

    if (this.archive != null && this.archive.exists())
    {
      logger.error("Delete of archive [" + this.archive.getAbsolutePath() + "] failed");
    }

    if (this.extractedParent != null && this.extractedParent.exists())
    {
      logger.error("Delete of extracted parent [" + this.extractedParent.getAbsolutePath() + "] failed");
    }
  }

  @Override
  public void delete()
  {
    logger.info("Deleting archive resource [" + this.getAbsolutePath() + "]");

    if (this.extractedParent != null)
    {
      logger.info("Deleting extracted parent [" + this.extractedParent.getAbsolutePath() + "]");
    }

    super.delete();

    if (this.archive != null && this.archive.exists())
    {
      logger.error("Delete of archive [" + this.archive.getAbsolutePath() + "] failed");
    }

    if (this.extractedParent != null && this.extractedParent.exists())
    {
      logger.error("Delete of extracted parent [" + this.extractedParent.getAbsolutePath() + "] failed");
    }
  }

}
