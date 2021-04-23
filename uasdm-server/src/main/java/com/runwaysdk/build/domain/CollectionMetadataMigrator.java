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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionSubfolder;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;


public class CollectionMetadataMigrator
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionMetadataMigrator.class);
  
  public static void main(String[] args) throws InterruptedException
  {
    doIt();
  }
  
  @Request
  public static void doIt() throws InterruptedException
  {
    List<Collection> collections = Collection.getAll();
    
    for (Collection col : collections)
    {
      if (col.getMetadataUploaded())
      {
        deleteOldMetadataIfExist(col);
      }
    }
  }
  
  @Transaction
  private static void deleteOldMetadataIfExist(Collection collection)
  {
    String key = collection.getS3location() + CollectionSubfolder.RAW.getFolderName() + "/" + collection.getName() + "_uasmetadata.xml";
    
    Boolean exists = RemoteFileFacade.objectExists(key);
    
    if (exists)
    {
      String log = "Deleting metadata file [" + key + "] and setting metadata uploaded to false.";
      System.out.println(log);
      logger.info(log);
      
      collection.setMetadataUploaded(false);
      collection.apply();
      
      RemoteFileFacade.deleteObject(key);
    }
    else
    {
      String log = "Clearing metadata uploaded for collection [" + collection.getOid() + "]. Could not find S3 artifact.";
      System.out.println(log);
      logger.info(log);
      
      collection.setMetadataUploaded(false);
      collection.apply();
    }
  }
}
