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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;


public class DocumentLayerMigrator
{
  private static final Logger logger = LoggerFactory.getLogger(DocumentLayerMigrator.class);
  
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }
  
  @Request
  public static void doIt() throws InterruptedException
  {
    Product.refreshAllDocuments();
//    logger.info(Product.get("46281467-ead7-41ae-bb4c-201c5f92a98a").getComponent().getS3location());
//    Product.get("c48c58b5-dfe4-4540-8dae-6b9c4e0fd7c8").refreshDocuments(processingConfigs);
  }
}
