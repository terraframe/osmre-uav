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
package gov.geoplatform.uasdm.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdGraphClassDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.SSLLocalhostTrustConfiguration;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.cog.CloudOptimizedGeoTiff;
import gov.geoplatform.uasdm.cog.CloudOptimizedGeoTiff.BBoxView;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.SiteObject;

public class Product extends ProductBase implements ProductIF
{
  static
  {
    SSLLocalhostTrustConfiguration.trustLocalhost();
  }

  public static final String ODM_ALL_DIR = "odm_all";

  private static final Logger logger = LoggerFactory.getLogger(Product.class);

  private static final long serialVersionUID = -1476643617;

  private String imageKey = null;

  public Product()
  {
    super();
  }

  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_PRODUCT).apply();
    }

    CollectionReport.update(this);
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      document.delete(removeFromS3);
    }

    CollectionReport.handleDelete(this);

    super.delete();
  }

  public UasComponent getComponent()
  {
    final List<UasComponent> parents = this.getParents(EdgeType.COMPONENT_HAS_PRODUCT, UasComponent.class);

    return parents.get(0);
  }

  public List<DocumentIF> getDocuments()
  {
    return this.getChildren(EdgeType.PRODUCT_HAS_DOCUMENT, DocumentIF.class);
  }

  public List<DocumentIF> getGeneratedFromDocuments()
  {
    return this.getParents(EdgeType.DOCUMENT_GENERATED_PRODUCT, DocumentIF.class);
  }

  @Override
  public Page<DocumentIF> getGeneratedFromDocuments(Integer pageNumber, Integer pageSize)
  {
    final Integer count = this.getCountGeneratedFromDocuments();

    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.DOCUMENT_GENERATED_PRODUCT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(IN('" + mdEdge.getDBClassName() + "'))");
    statement.append(" FROM :rid");
    statement.append(" ORDER BY name");
    statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    final GraphQuery<DocumentIF> query = new GraphQuery<DocumentIF>(statement.toString());
    query.setParameter("rid", this.getRID());

    return new Page<DocumentIF>(count, pageNumber, pageSize, query.getResults());
  }

  public Integer getCountGeneratedFromDocuments()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.DOCUMENT_GENERATED_PRODUCT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT IN('" + mdEdge.getDBClassName() + "').size()");
    statement.append(" FROM :rid");

    final GraphQuery<Integer> query = new GraphQuery<Integer>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getSingleResult();
  }

  public void addDocuments(List<DocumentIF> documents)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PRODUCT_HAS_DOCUMENT);

    for (DocumentIF document : documents)
    {
      this.addChild((Document) document, mdEdge).apply();
    }
  }

  public static Product createIfNotExist(UasComponentIF uasComponent)
  {
    Product product = find(uasComponent);

    if (product == null)
    {
      product = new Product();
      product.setName(uasComponent.getName());
      product.setPublished(false);
    }

    product.setLastUpdateDate(new Date());
    product.apply(uasComponent);

    return product;
  }

  public static Product find(UasComponentIF component)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "'))\n");
    statement.append("FROM :rid \n");

    final GraphQuery<Product> query = new GraphQuery<Product>(statement.toString());
    query.setParameter("rid", ( (UasComponent) component ).getRID());

    return query.getSingleResult();
  }

  @Transaction
  public void clear()
  {
    Set<String> orphans = new HashSet<String>();

    final List<Document> rawImages = this.getParents(EdgeType.DOCUMENT_GENERATED_PRODUCT, Document.class);

    for (Document rawImage : rawImages)
    {
      this.removeParent(rawImage, EdgeType.DOCUMENT_GENERATED_PRODUCT);
    }

    final List<Document> orthoArtifacts = this.getChildren(EdgeType.PRODUCT_HAS_DOCUMENT, Document.class);

    for (Document orthoArtifact : orthoArtifacts)
    {
      orphans.add(orthoArtifact.getOid());
      this.removeChild(orthoArtifact, EdgeType.PRODUCT_HAS_DOCUMENT);
    }

    for (String orphan : orphans)
    {
      Document.get(orphan).delete(false);
    }
  }

  /**
   * This method calculates a 4326 CRS bounding box for a given raster layer
   * with the specified mapKey. The Cog must exist on S3 before calling this method.
   * If the bounding box cannot be calculated, for whatever reason, this method will return null.
   */
  public BBoxView calculateBoundingBox()
  {
    DocumentIF mappable = null;
    
    List<DocumentIF> mappables = this.getMappableDocuments();
    
    Optional<DocumentIF> ortho = mappables.stream().filter(doc -> doc.getS3location().endsWith(ImageryComponent.ORTHO + "/odm_orthophoto" + CogTifProcessor.COG_EXTENSION)).findFirst();
    Optional<DocumentIF> hillshade = mappables.stream().filter(doc -> doc.getS3location().endsWith(ODMZipPostProcessor.DEM_GDAL + "/dsm" + CogTifProcessor.COG_EXTENSION)).findFirst();
    
    if (ortho.isPresent())
    {
      mappable = ortho.get();
    }
    else if (hillshade.isPresent())
    {
      mappable = hillshade.get();
    }
    else
    {
      return null;
    }

    try
    {
//      Map<String, CRSEnvelope> bboxes = layer.getBoundingBoxes();
//
//      for (Entry<String, CRSEnvelope> entry : bboxes.entrySet())
//      {
//        String code = entry.getKey();
//        CRSEnvelope envelope = entry.getValue();
//
//        try
//        {
//          // Mapbox's docs say that it's in 3857 but it's bounding box method
//          // expects 4326.
//          CoordinateReferenceSystem sourceCRS = CRS.decode(code);
//          CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
//          MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
//
//          com.vividsolutions.jts.geom.Envelope jtsEnvelope = new com.vividsolutions.jts.geom.Envelope();
//          jtsEnvelope.init(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY());
//
//          com.vividsolutions.jts.geom.Envelope env3857 = JTS.transform(jtsEnvelope, transform);
//
//          JSONArray json = new JSONArray();
//          json.put(env3857.getMinX());
//          json.put(env3857.getMaxX());
//          json.put(env3857.getMinY());
//          json.put(env3857.getMaxY());
//
//          return json.toString();
//        }
//        catch (Throwable t)
//        {
//          // Perhaps there is another bounding box we can try?
//        }
//      }
      
      return new CloudOptimizedGeoTiff(this, mappable).getBoundingBox();
    }
    catch (Throwable t)
    {
      logger.error("Error when getting bounding box from mappable [" + mappable.getS3location() + "] for product [" + this.getName() + "].", t);
      return null;
    }
  }

  public void updateBoundingBox()
  {
    UasComponent component = this.getComponent();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);

    components.add(component);

    if (this.getImageKey() == null)
    {
      this.calculateKeys(components);
    }

    BBoxView bbox = this.calculateBoundingBox();

    if (bbox != null)
    {
      this.setBoundingBox(bbox.toJSON().toString());
      this.apply();
    }
  }

  public String getImageKey()
  {
    return this.imageKey;
  }

  @Override
  public boolean isPublished()
  {
    return this.getPublished() != null && this.getPublished();
  }

  /**
   * Refreshes S3 and the database with contents from the all zip for all
   * products in the system.
   * 
   * @param sessionId
   * @param productId
   * @throws InterruptedException
   */
  public static void refreshAllDocuments() throws InterruptedException
  {
    final MdVertexDAOIF mdProduct = MdVertexDAO.getMdVertexDAO(Product.CLASS);

    StringBuilder sb = new StringBuilder();

    sb.append("SELECT FROM " + mdProduct.getDBClassName());

    GraphQuery<Product> gq = new GraphQuery<Product>(sb.toString());

    List<Product> results = gq.getResults();

    for (Product product : results)
    {
      logger.info("Refreshing documents for product [" + product.getName() + " : " + product.getOid() + "].");
      product.refreshDocuments();
    }
  }

  /**
   * Downloads the product's ODM all.zip and refreshes S3 and database documents
   * with the data contained.
   * 
   * @param sessionId
   * @param productId
   * @throws InterruptedException
   */
  public void refreshDocuments() throws InterruptedException
  {
    final CollectionIF collection = (CollectionIF) this.getComponent();

    boolean allZipExists = this.getAllZip() != null;

    if (allZipExists)
    {
      ODMZipPostProcessor uploader = new ODMZipPostProcessor(collection, null, this);

      uploader.processAllZip();
    }
  }

  public SiteObject getAllZip()
  {
    final UasComponentIF component = this.getComponent();

    // This commented out code was for fetching the all zips from the
    // ProductHasDocument relationship. Unfortunately
    // we can't do this because Documents don't have a lastUpdateDate field so
    // we wouldn't know how to order them.
    // I'm leaving this code here because it's technically a "better" solution
    // if we ever have lastUpdate on the graph.
    // List<DocumentIF> docs = product.getDocuments();
    //
    // Iterator<DocumentIF> it = docs.iterator();
    // while (it.hasNext())
    // {
    // DocumentIF doc = it.next();
    //
    // if (! (doc.getS3location().contains("/" + Product.ODM_ALL_DIR + "/") &&
    // doc.getS3location().endsWith(".zip")))
    // {
    // it.remove();
    // }
    // }
    //
    // DocumentIF lastDoc = null;
    // for (DocumentIF doc : docs)
    // {
    // if (lastDoc == null ||
    // doc.getLastModified().after(lastDoc.getLastModified()))
    // {
    // lastDoc = doc;
    // }
    // }
    //
    // if (lastDoc != null)
    // {
    // return component.download(lastDoc.getS3location());
    // }
    // else
    // {
    List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, Product.ODM_ALL_DIR, new LinkedList<SiteObject>(), null, null).getObjects();

    SiteObject last = null;

    String path = component.getS3location().replaceAll("\\/", "\\\\/");

    if (!path.endsWith("/"))
    {
      path = path + "\\\\/";
    }

    Pattern pattern = Pattern.compile("^" + path + "odm_all\\/all.+\\.zip$", Pattern.CASE_INSENSITIVE);

    for (SiteObject item : items)
    {
      if (last == null || item.getLastModified().after(last.getLastModified()))
      {
        Matcher matcher = pattern.matcher(item.getKey());

        if (matcher.find())
        {
          last = item;
        }
      }
    }

    if (last != null)
    {
      return last;
    }
    else
    {
      return null;
    }
    // }
  }

  public RemoteFileObject downloadAllZip()
  {
    final UasComponentIF component = this.getComponent();

    SiteObject all = this.getAllZip();

    if (all != null)
    {
      return component.download(all.getKey());
    }
    else
    {
      throw new ProgrammingErrorException("All zip does not exist");
    }
  }

  @Override
  @Transaction
  public void togglePublished()
  {
    this.setPublished(!this.isPublished());
    this.apply();

    List<DocumentIF> mappables = this.getMappableDocuments();

    for (DocumentIF mappable : mappables)
    {
      if (this.isPublished())
      {
        // Add to public S3 bucket
        RemoteFileFacade.copyObject(mappable.getS3location(), AppProperties.getBucketName(), mappable.getS3location(), AppProperties.getPublicBucketName());
      }
      else
      {
        // Remove from public S3 bucket
        RemoteFileFacade.deleteObject(mappable.getS3location(), AppProperties.getPublicBucketName());
      }
    }
  }

  @Override
  public void calculateKeys(List<UasComponentIF> components)
  {
    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      if (document.getName().endsWith(".png"))
      {
        this.imageKey = document.getS3location();
      }
    }
  }

  public List<DocumentIF> getMappableDocuments()
  {
    List<DocumentIF> mappableDocs = new ArrayList<DocumentIF>();
    List<DocumentIF> documents = this.getDocuments();
    
    for (DocumentIF document : documents)
    {
      if (document.isMappable())
      {
        mappableDocs.add(document);
      }
    }

    return mappableDocs;
  }

  public static List<ProductIF> getProducts()
  {
    final MdGraphClassDAOIF mdEdge = MdGraphClassDAO.getMdGraphClassDAO(Product.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdEdge.getDBClassName() + " \n");

    final GraphQuery<ProductIF> query = new GraphQuery<ProductIF>(statement.toString());

    return query.getResults();
  }
}
