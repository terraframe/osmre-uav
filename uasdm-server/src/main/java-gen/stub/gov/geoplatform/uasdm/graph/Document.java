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
package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

public class Document extends DocumentBase implements DocumentIF
{
  private static final long serialVersionUID = -1445705168;

  public Document()
  {
    super();
  }

  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();
    
    this.setLastModified(new Date());

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_DOCUMENT).apply();
    }
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    if (removeFromS3 && !this.getS3location().trim().equals(""))
    {
      new RemoteFileDeleteCommand(this.getS3location(), AppProperties.getBucketName(), this.getComponent()).doIt();
      
      if (this.isMappable())
      {
        Optional<Product> product = this.getProductHasDocumentParentProducts().stream().findFirst();
        
        if (product.isPresent() && product.get().getPublished())
        {
          new RemoteFileDeleteCommand(this.getS3location(), AppProperties.getPublicBucketName(), this.getComponent()).doIt();
        }
      }
    }

    super.delete();
  }
  
  @Override
  public boolean isMappable()
  {
    final String s3 = this.getS3location();
    
    return s3.matches(Product.MAPPABLE_ORTHO_REGEX) || s3.matches(Product.MAPPABLE_DEM_REGEX);
  }
  
  public RemoteFileObject download()
  {
    return RemoteFileFacade.download(this.getS3location());
  }

  public UasComponent getComponent()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
    final List<UasComponent> parents = this.getParents(mdEdge, UasComponent.class);

    return parents.get(0);
  }

  @Override
  protected String buildKey()
  {
    return this.getS3location();
  }

  public static Document createIfNotExist(UasComponentIF uasComponent, String key, String name, String description, String tool)
  {
    Document document = Document.find(key);

    if (document == null)
    {
      document = new Document();
      document.setS3location(key);
    }
    else
    {
    }

    document.setName(name);
    document.setDescription(description);
    document.setTool(tool);
    document.apply(uasComponent);

    return document;
  }

  public static Document find(String key)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Document.CLASS);

    String statement = "SELECT FROM " + mdVertex.getDBClassName() + " WHERE s3location = :s3location";

    final GraphQuery<Document> query = new GraphQuery<Document>(statement);
    query.setParameter("s3location", key);

    return query.getSingleResult();
  }

  public void addGeneratedProduct(ProductIF product)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.DOCUMENT_GENERATED_PRODUCT);

    this.addChild((Product) product, mdEdge).apply();
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.getOid());
    object.put("key", this.getS3location());
    object.put("name", this.getName());
    object.put("component", this.getComponent().getOid());

    return object;
  }

  @Override
  public String toString()
  {
    return this.getS3location();
  }
}
