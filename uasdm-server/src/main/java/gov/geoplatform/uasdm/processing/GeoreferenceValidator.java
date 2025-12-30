/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.processing;

import java.util.stream.StreamSupport;

import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.GenericException;

public class GeoreferenceValidator
{
  public boolean isValid(ApplicationFileResource res)
  {
    boolean isArchive = res.getName().endsWith(".zip") || res.getName().endsWith(".tar.gz");

    if (isArchive)
    {
      final ApplicationFileResource tif = StreamSupport.stream(res.getChildrenFiles().spliterator(), false) //
          .filter(a -> a.getNameExtension().toLowerCase().equals("tiff") || a.getNameExtension().toLowerCase().equals("tif")) //
          .findFirst() //
          .orElse(null);

      if (tif != null)
      {
        String auxFileName = tif.getName() + ".aux.xml";

        // Make sure there is a corresponding .tiff.aux.xml file
        return StreamSupport.stream(res.getChildrenFiles().spliterator(), false) //
            .filter(a -> a.getName().equals(auxFileName)) //
            .findFirst() //
            .isPresent();
      }
    }

    return false;
  }
}
