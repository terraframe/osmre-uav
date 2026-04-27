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
 * @author jsmethie
 *
 */
abstract public class GroundControlPointFileValidator
{
  protected FileFormat geoLocationFormat;
  
  protected ApplicationFileResource gcpFile;
  
  protected ArchiveFileResource archive;
  
  protected Set<String> imageNames = new HashSet<String>();

  public GroundControlPointFileValidator(FileFormat geoLocationFormat, ApplicationFileResource gcpFile, ArchiveFileResource archive)
  {
    this.geoLocationFormat = geoLocationFormat;
    this.gcpFile = gcpFile;
    this.archive = archive;
    
    calculateImageNames();
  }
  
  private void calculateImageNames() {
    imageNames.clear();
    
    Queue<ApplicationFileResource> queue = new LinkedList<>();
    queue.add(archive);
    while(!queue.isEmpty())
    {
      var res = queue.poll();
      
      if (res.hasChildren())
      {
        for (var child : res.getChildrenFiles())
          queue.add(child);
        
        continue;
      }
      
      imageNames.add(res.getName());
    }
  }
  
  abstract public GroundControlPointValidationResults validate();

  public static GroundControlPointValidationResults validate(FileFormat geoLocationFormat, ApplicationFileResource gcpFile, ArchiveFileResource archive)
  {
    switch(geoLocationFormat)
    {
      case ODM:
        return new ODMGroundControlPointFileValidator(geoLocationFormat, gcpFile, archive).validate();
      default:
        throw new UnsupportedOperationException("Invalid format " + geoLocationFormat);
    }
  }
  
  public static void validate(FileFormat geoLocationFormat, ApplicationFileResource gcpFile, ArchiveFileResource archive, AbstractWorkflowTask task)
  {
    GroundControlPointValidationResults results = validate(geoLocationFormat, gcpFile, archive);
    
    results.getErrors().stream().forEach(er -> task.createAction(er, TaskActionType.ERROR));
    
    if (results.hasErrors())
    {
      throw new GroundControlPointFileInvalidFormatException("View messages for more information.");
    }
  }
  
}
