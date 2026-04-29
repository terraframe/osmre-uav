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
package gov.geoplatform.uasdm.processing.gcp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;

/**
 * 
 * @author jsmethie, rrowlands
 *
 */
abstract public class GroundControlPointFileValidator
{
  protected FileFormat geoLocationFormat;
  
  protected ApplicationFileResource gcpFile;
  
  protected Set<String> imageNames = new HashSet<String>();

  public GroundControlPointFileValidator(FileFormat geoLocationFormat, ApplicationFileResource gcpFile, Set<String> imageNames)
  {
    this.geoLocationFormat = geoLocationFormat;
    this.gcpFile = gcpFile;
    this.imageNames = imageNames;
  }
  
  abstract public GroundControlPointValidationResults validate();

  public static GroundControlPointValidationResults validate(FileFormat geoLocationFormat, ApplicationFileResource gcpFile, Set<String> imageNames)
  {
    switch(geoLocationFormat)
    {
      case ODM:
        return new ODMGroundControlPointFileValidator(geoLocationFormat, gcpFile, imageNames).validate();
      default:
        throw new UnsupportedOperationException("Invalid format " + geoLocationFormat);
    }
  }
  
  public static void validate(FileFormat geoLocationFormat, ApplicationFileResource gcpFile, Set<String> imageNames, AbstractWorkflowTask task)
  {
    GroundControlPointValidationResults results = validate(geoLocationFormat, gcpFile, imageNames);
    
    results.getErrors().stream().forEach(er -> task.createAction(er, TaskActionType.ERROR));
    
    if (results.hasErrors())
    {
      throw new GroundControlPointFileInvalidFormatException("View messages for more information.");
    }
  }
  
}
