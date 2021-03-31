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
package com.runwaysdk.build.domain;

import java.util.ArrayList;
import java.util.List;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader.BasicODMFile;


public class PointcloudS3Migrator
{
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }
  
  @Request
  public static void doIt() throws InterruptedException
  {
    List<BasicODMFile> processingConfigs = new ArrayList<BasicODMFile>();
    
    processingConfigs.add(new BasicODMFile("potree_pointcloud", "odm_all/potree", new String[]{"cloud.js"}, false));
    processingConfigs.add(new BasicODMFile("potree_pointcloud", "odm_all/potree", new String[]{"data"}, true));
    
    Product.refreshAllDocuments(processingConfigs);
  }
}
