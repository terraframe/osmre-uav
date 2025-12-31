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

import java.util.List;

import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.GenericException;

public abstract class ResourceUtil
{
  public static ApplicationFileResource getResource(ApplicationFileResource res)
  {
    boolean isArchive = res.getName().endsWith(".zip") || res.getName().endsWith(".tar.gz");

    if (isArchive)
    {
      List<ApplicationFileResource> files = res.getChildrenFiles().getAll();

      if (files.size() > 1)
      {
        GenericException ex = new GenericException();
        ex.setUserMessage("Uploaded archives must contain only a single geotiff or a tiff and an .aux.xml file");

        throw ex;
      }

      return files.get(0);
    }

    return res;
  }

}
