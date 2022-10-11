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

import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.InMemoryMonitor;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import net.geoprism.GeoprismProperties;

/**
 * Pulls all tifs from S3 and reuploads them as cog tiffs.
 * 
 * !Important! If this is run outside of a transaction then the deletes will happen at the end, after we've
 *   created the new cogs, resulting in this patcher just deleting all existing cogs.
 * 
 * @author rrowlands
 */
public class CogTiffPatcher
{
  private static final Logger logger = LoggerFactory.getLogger(CogTiffPatcher.class);
  
  protected Set<String> deleted = new HashSet<String>();
  
  public static void main(String[] args) throws Throwable
  {
    new CogTiffPatcher().doIt();
  }
  
  public CogTiffPatcher()
  {
    
  }
  
  @Request
  public void doIt() throws Throwable
  {
    this.refreshAllProducts();
  }
  
  public void refreshAllProducts() throws Throwable
  {
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    
    final MdVertexDAOIF mdProduct = MdVertexDAO.getMdVertexDAO(Product.CLASS);

    StringBuilder sb = new StringBuilder();

    sb.append("SELECT FROM " + mdProduct.getDBClassName());

    GraphQuery<Product> gq = new GraphQuery<Product>(sb.toString());

    List<Product> results = gq.getResults();
    
    List<String> errors = new LinkedList<String>();
    
    int num = 0;

    for (Product product : results)
    {
      this.deleteAllExistingCogTiff(product);
      
      logger.info("Generating cog tifs for product [" + product.getName() + " : " + product.getOid() + "].");
      
      final CollectionIF collection = (CollectionIF) product.getComponent();

      List<DocumentIF> documents = product.getDocuments();
      
      // TODO : Filter to only hillshade and ortho?
//      documents = documents.stream().filter(document -> document.getS3location().endsWith(ODMZipPostProcessor.DEM_GDAL + "/dsm.tif") || document.getS3location().endsWith(ImageryComponent.ORTHO + "/odm_orthophoto.tif")).collect(Collectors.toList());
      
      for (DocumentIF document : documents)
      {
        Thread.yield();
        
        if (document.getName().endsWith(".tif") && !document.getName().endsWith(CogTifProcessor.COG_EXTENSION))
        {
//          boolean alreadyExists = documents.stream().filter(doc -> doc.getName().equals(FilenameUtils.getBaseName(document.getName()) + CogTifProcessor.COG_EXTENSION)).collect(Collectors.toList()).size() > 0;
//          if (alreadyExists)
//          {
//            logger.info("Skipping [" + document.getS3location() + "].");
//            continue;
//          }
          
          logger.info("Converting [" + document.getS3location() + "] to a cog tif.");
          
          RemoteFileObject remote = document.download();
          
          try (CloseableFile tif = GeoprismProperties.newTempFile())
          {
            java.nio.file.Files.copy(
              remote.openNewStream(),
              tif.toPath(),
              StandardCopyOption.REPLACE_EXISTING);
            
            final InMemoryMonitor monitor = new InMemoryMonitor();
            
            String s3Path = document.getS3location();
            s3Path = s3Path.substring(0, s3Path.lastIndexOf(".")) + CogTifProcessor.COG_EXTENSION;
            
            CogTifProcessor processor = new CogTifProcessor(s3Path, product, collection, monitor);
            processor.process(new FileResource(tif));
            
            if (monitor.getErrors().size() > 0 || WorkflowTaskStatus.ERROR.equals(monitor.getStatus()))
            {
              // throw new RuntimeException("Error encountered when processing [" + document.getS3location() + "] [" + document.getOid() + "].");
              errors.addAll(monitor.getErrors());
            }
          }
          
          num = num + 1;
        }
      }
    }
    
    if (errors.size() > 0)
    {
      logger.error("Patching completed. " + num + " documents were converted. The following errors were generated: \n" + StringUtils.join(errors, "\n"));
    }
    else
    {
      logger.info("Patching completed. " + num + " documents were converted.");
    }
  }
  
  private void deleteAllExistingCogTiff(Product product)
  {
    List<DocumentIF> documents = product.getDocuments().stream().filter(doc -> doc.getName().endsWith(CogTifProcessor.COG_EXTENSION)).collect(Collectors.toList());
    
    logger.info("Deleting " + documents.size() + " existing cog tifs");
    
    for (DocumentIF document: documents)
    {
      if (!deleted.contains(document.getOid()))
      {
        deleted.add(document.getOid());
        document.delete(true);
      }
    }
  }
}
