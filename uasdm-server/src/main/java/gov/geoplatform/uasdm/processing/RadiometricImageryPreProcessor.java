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
 * ALL radiometric datasets must have a header "PhotometricInterpretation" set to "BlackIsZero". ODM does not process
 * them correctly if this header is missing.
 * 
 * Some versions of the Workswell Wiris thermal sensor (as well as some other sensors) omit this header and ODM
 * DOES NOT handle it properly - it fails to process the imagery.
 * 
 * The job of this pre-processor (must be invoked before sending the imagery into ODM), is to add this header into
 * a collection of images. If the header is already present it will not be overwritten.
 * 
 * Rather than hardcode a list of all thermal sensors where this problem exists, we are simply going to invoke this
 * on all thermal collections.
 * 
 * @author rrowlands
 */
public class RadiometricImageryPreProcessor
{
  private Logger logger = LoggerFactory.getLogger(RadiometricImageryPreProcessor.class);
  
  public RadiometricImageryPreProcessor()
  {
  }
  
  public void process(ApplicationFileResource res)
  {
    new SystemProcessExecutor().execute(new String[] {
        "exiftool",
        "-overwrite_original",
        "-if", "not defined $PhotometricInterpretation",
        "-PhotometricInterpretation=BlackIsZero",
        res.getAbsolutePath()
      });
  }
}
