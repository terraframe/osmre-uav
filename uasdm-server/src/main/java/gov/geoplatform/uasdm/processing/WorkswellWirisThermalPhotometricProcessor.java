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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationFileResource;

/**
 * The Workswell Wiris thermal sensor is not including the PhotometricInterpretation EXIF header in its images.
 * This is causing ODM To not process the data. The purpose of this processor is to inject this EXIF header
 * into a collection of images.
 * 
 * @author rrowlands
 *
 */
public class WorkswellWirisThermalPhotometricProcessor
{
  private Logger logger = LoggerFactory.getLogger(WorkswellWirisThermalPhotometricProcessor.class);
  
  public WorkswellWirisThermalPhotometricProcessor()
  {
  }
  
  public void process(ApplicationFileResource res)
  {
    new SystemProcessExecutor().execute(new String[] {
        "exiftool", "-overwrite_original", "-PhotometricInterpretation=BlackIsZero", res.getAbsolutePath()
    });
  }
}
