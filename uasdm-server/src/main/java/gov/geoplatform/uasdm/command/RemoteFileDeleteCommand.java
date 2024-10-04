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
package gov.geoplatform.uasdm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.processing.report.CollectionReportTask;
import gov.geoplatform.uasdm.processing.report.CollectionReportTask.Type;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class RemoteFileDeleteCommand implements Command
{
  private Logger         log = LoggerFactory.getLogger(RemoteFileDeleteCommand.class);

  private String         key;

  private String         bucket;

  private UasComponentIF component;

  public RemoteFileDeleteCommand(String key, UasComponentIF component)
  {
    this.key = key;
    this.component = component;
    this.bucket = AppProperties.getBucketName();
  }

  public RemoteFileDeleteCommand(String key, String bucket, UasComponentIF component)
  {
    this.key = key;
    this.bucket = bucket;
    this.component = component;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Deleting key [" + this.key + "] from S3 bucket [" + this.bucket + "]");

    try
    {
      RemoteFileFacade.deleteObjects(this.key, this.bucket);

      if (this.component instanceof CollectionIF)
      {
        CollectionReportFacade.process(new CollectionReportTask(Type.SIZE, this.component));
      }
    }
    catch (RuntimeException e)
    {
      log.error("Error indexing stac item", e);

      throw e;
    }

  }

  /**
   * Executes the undo in this Command, and closes the connection.
   */
  public void undoIt()
  {
  }

  /**
   * Returns a human readable string describing what this command is trying to
   * do.
   * 
   * @return human readable string describing what this command is trying to do.
   */
  public String doItString()
  {
    return "Deleting from S3 with the following key [" + key + "]";
  }

  public String undoItString()
  {
    return null;
  }

  /*
   * Indicates if this Command deletes something.
   * 
   * @return <code><b>true</b></code> if this Command deletes something.
   */
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void doFinally()
  {
  }

}
