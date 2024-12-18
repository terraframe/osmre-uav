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
package gov.geoplatform.uasdm.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.ProcessConfiguration.ProcessType;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;

public class TestProductInfo
{
  public static final String DEFAULT_PRODUCT_NAME = "DEFAULT";
  
  protected TestUasComponentInfo component;
  
  protected String productName;
  
  protected List<TestDocumentInfo> outputs;
  
  protected List<TestDocumentInfo> inputs;

  public TestProductInfo(TestCollectionInfo component, String productName, TestDocumentInfo[] output, TestDocumentInfo... input)
  {
    this.component = component;
    this.productName = productName;
    this.outputs = Arrays.asList(output);
    this.inputs = Arrays.asList(input);
  }

  public Product apply()
  {
    UasComponent component = this.component.getServerObject();
    
    Product product = (Product) component.createProductIfNotExist(productName);
    
    List<Document> inDocs = new ArrayList<Document>();
    for (TestDocumentInfo obj : inputs)
    {
      Document document = obj.apply();
      product.addDocumentGeneratedProductParent(document).apply();
      inDocs.add(document);
    }
    
    List<Document> outDocs = new ArrayList<Document>();
    for (TestDocumentInfo obj : outputs)
    {
      Document document = obj.apply();
      product.addProductHasDocumentChild(document).apply();
      outDocs.add(document);
    }
    
    return product;
  }
  
  public List<TestDocumentInfo> getInputDocuments()
  {
    return this.inputs;
  }
  
  public List<TestDocumentInfo> getOutputDocuments()
  {
    return this.outputs;
  }
  
  public List<Document> getServerInputDocuments()
  {
    UasComponent collection = this.component.getServerObject();
    
    return this.inputs.stream().map(i -> Document.find(collection.getS3location() + i.getKey())).collect(Collectors.toList());
  }
  
  public List<Document> getServerOutputDocuments()
  {
    UasComponent collection = this.component.getServerObject();
    
    return this.outputs.stream().map(i -> Document.find(collection.getS3location() + i.getKey())).collect(Collectors.toList());
  }

  public Product getServerObject()
  {
    UasComponent collection = this.component.getServerObject();
    
    return (Product) collection.getProduct(productName).orElse(null);
  }

  public void delete()
  {
    Product server = this.getServerObject();

    if (server != null)
    {
      server.delete();
    }
  }
}
