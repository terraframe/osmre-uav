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
import java.util.List;
import java.util.stream.Collectors;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.ImageryComponent;

public class TestProductInfo
{
  public static final String DEFAULT_PRODUCT_NAME = "DEFAULT";
  
  public static int COUNTING_SEQUENCE = 0;
  
  protected TestUasComponentInfo component;
  
  protected String productName;
  
  protected List<TestDocumentInfo> outputs = new ArrayList<TestDocumentInfo>();
  
  protected List<TestDocumentInfo> inputs = new ArrayList<TestDocumentInfo>();
  
  public TestProductInfo(TestCollectionInfo component)
  {
    this.component = component;
    this.productName = COUNTING_SEQUENCE++ + "!@#" + DEFAULT_PRODUCT_NAME;
  }

  public TestProductInfo(TestCollectionInfo component, String productName)
  {
    this.component = component;
    this.productName = productName;
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
  
  public String getProductName()
  {
    return this.productName;
  }
  
  public void setProductName(String name)
  {
    this.productName = name;
  }
  
  public String getS3location()
  {
    return ImageryComponent.PRODUCTS + "/" + this.getProductName() + "/";
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
    return this.inputs.stream().map(i -> i.getServerObject()).collect(Collectors.toList());
  }
  
  public List<Document> getServerOutputDocuments()
  {
    return this.outputs.stream().map(i -> i.getServerObject()).collect(Collectors.toList());
  }

  public Product getServerObject()
  {
    return (Product) getProduct(this.productName);
  }

  public void delete()
  {
    Product server = this.getServerObject();

    if (server != null)
    {
      server.delete();
    }
  }
  
  /**
   * The assumption here is that product names are unique. This won't be true in production but we can make it true in our test harness.
   * 
   * @param productName
   * @return
   */
  public static Product getProduct(String productName)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Product.CLASS);
    final String attr = mdVertex.definesAttribute(Product.PRODUCTNAME).getColumnName();
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName() + " WHERE " + attr + " = :param");

    final GraphQuery<Product> query = new GraphQuery<Product>(statement.toString());
    query.setParameter("param", productName);
    
    return query.getSingleResult();
  }
}
