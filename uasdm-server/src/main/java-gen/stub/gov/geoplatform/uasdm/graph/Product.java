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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
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
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.SSLLocalhostTrustConfiguration;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.command.GeoserverRemoveCoverageCommand;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader.BasicODMFile;
import gov.geoplatform.uasdm.odm.AllZipS3Uploader.SpecialException;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class Product extends ProductBase implements ProductIF
{
  public static final String ODM_ALL_DIR = "odm_all";
  
  private static final Logger logger           = LoggerFactory.getLogger(Product.class);

  private static final long   serialVersionUID = -1476643617;

  private String              imageKey         = null;

  private String              mapKey           = null;
  
  private String              demKey           = null;

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
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    // Delete all of the documents
    UasComponent component = this.getComponent();
    String workspace = this.getWorkspace();

    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      document.delete(removeFromS3);

      if (document.getName().endsWith(".tif"))
      {
        String storeName = component.getStoreName(document.getS3location());

        if (GeoserverFacade.layerExists(workspace, storeName))
        {
          new GeoserverRemoveCoverageCommand(workspace, storeName).doIt();
        }
      }
    }

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
    final List<Document> documents = this.getParents(EdgeType.DOCUMENT_GENERATED_PRODUCT, Document.class);

    for (Document document : documents)
    {
      this.removeParent(document, EdgeType.DOCUMENT_GENERATED_PRODUCT);
    }
  }

  static
  {
    SSLLocalhostTrustConfiguration.trustLocalhost();
  }

  /**
   * This method calculates a 4326 CRS bounding box for a given raster layer
   * with the specified mapKey. This layer must exist on Geoserver before
   * calling this method. If the bounding box cannot be calculated, for whatever
   * reason, this method will return null.
   * 
   * @return A JSON array where [x1, x2, y1, y2]
   */
  public String calculateBoundingBox(String mapKey)
  {
    try
    {
      WMSCapabilities capabilities = GeoserverFacade.getCapabilities(this.getWorkspace(), mapKey);

      List<Layer> layers = capabilities.getLayerList();

      Layer layer = null;

      if (layers.size() == 0)
      {
        logger.error("Unable to calculate bounding box for product [" + this.getName() + "]. Geoserver did not return any layers.");
        return null;
      }
      else if (layers.size() == 1)
      {
        layer = layers.get(0);
      }
      else
      {
        for (Layer potential : layers)
        {
          if (potential.getName() != null && potential.getName().equals(mapKey))
          {
            layer = potential;
            break;
          }
        }

        if (layer == null)
        {
          logger.error("Unable to calculate bounding box for product [" + this.getName() + "]. Geoserver returned more than one layer and none of the layers matched what we were looking for.");
          return null;
        }
      }

      Map<String, CRSEnvelope> bboxes = layer.getBoundingBoxes();

      for (Entry<String, CRSEnvelope> entry : bboxes.entrySet())
      {
        String code = entry.getKey();
        CRSEnvelope envelope = entry.getValue();

        try
        {
          // Mapbox's docs say that it's in 3857 but it's bounding box method
          // expects 4326.
          CoordinateReferenceSystem sourceCRS = CRS.decode(code);
          CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
          MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

          com.vividsolutions.jts.geom.Envelope jtsEnvelope = new com.vividsolutions.jts.geom.Envelope();
          jtsEnvelope.init(envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY());

          com.vividsolutions.jts.geom.Envelope env3857 = JTS.transform(jtsEnvelope, transform);

          JSONArray json = new JSONArray();
          json.put(env3857.getMinX());
          json.put(env3857.getMaxX());
          json.put(env3857.getMinY());
          json.put(env3857.getMaxY());

          return json.toString();
        }
        catch (Throwable t)
        {
          // Perhaps there is another bounding box we can try?
        }
      }
    }
    catch (Throwable t)
    {
      logger.error("Error when getting bounding box from layer [" + mapKey + "] for product [" + this.getName() + "].", t);
      return null;
    }

    logger.error("Error when getting bounding box from layer [" + mapKey + "] for product [" + this.getName() + "].");
    return null;
  }

  public void updateBoundingBox()
  {
    UasComponent component = this.getComponent();

    List<UasComponentIF> components = component.getAncestors();
    Collections.reverse(components);

    components.add(component);

    if (this.getImageKey() == null || this.getMapKey() == null)
    {
      this.calculateKeys(components);
    }

    if (this.getMapKey() != null && this.getMapKey().length() > 0)
    {
      String bbox = this.calculateBoundingBox(this.getMapKey());

      if (bbox != null)
      {
        this.setBoundingBox(bbox);
        this.apply();
      }
    }
  }

  public String getImageKey()
  {
    return this.imageKey;
  }

  public String getMapKey()
  {
    return this.mapKey;
  }
  
  public String getDemKey()
  {
    return demKey;
  }

  public void setDemKey(String demKey)
  {
    this.demKey = demKey;
  }

  public String getWorkspace()
  {
    if (isPublished())
    {
      return AppProperties.getPublicWorkspace();
    }

    return GeoserverProperties.getWorkspace();
  }

  @Override
  public boolean isPublished()
  {
    return this.getPublished() != null && this.getPublished();
  }

  @Override
  public void createImageService()
  {
    Util.createImageServices(this.getWorkspace(), this.getComponent());
  }
  
  /**
   * Refreshes S3 and the database with contents from the all zip for all products in the system.
   * 
   * @param sessionId
   * @param productId
   * @throws InterruptedException
   */
  public static void refreshAllDocuments(List<BasicODMFile> config) throws InterruptedException
  {
    final MdVertexDAOIF mdProduct = MdVertexDAO.getMdVertexDAO(Product.CLASS);
    
    StringBuilder sb = new StringBuilder();
    
    sb.append("SELECT FROM " + mdProduct.getDBClassName());
    
    GraphQuery<Product> gq = new GraphQuery<Product>(sb.toString());
    
    List<Product> results = gq.getResults();
    
    for (Product product : results)
    {
      product.refreshDocuments(config);
    }
  }
  
  /**
   * Downloads the product's ODM all.zip and refreshes S3 and database documents with the data contained.
   * 
   * @param sessionId
   * @param productId
   * @throws InterruptedException
   */
  public void refreshDocuments(List<BasicODMFile> config) throws InterruptedException
  {
    final UasComponentIF component = this.getComponent();
    
    boolean allZipExists = this.getAllZip() != null;
    
    if (allZipExists) 
    {
      AllZipS3Uploader uploader = new AllZipS3Uploader(config, component, null, this);
      
      try
      {
        uploader.processAllZip();
      }
      catch (SpecialException e)
      {
        throw new RuntimeException(e);
      }
    }
  }
  
  public SiteObject getAllZip()
  {
    final UasComponentIF component = this.getComponent();

    // This commented out code was for fetching the all zips from the ProductHasDocument relationship. Unfortunately
    // we can't do this because Documents don't have a lastUpdateDate field so we wouldn't know how to order them.
    // I'm leaving this code here because it's technically a "better" solution if we ever have lastUpdate on the graph.
//    List<DocumentIF> docs = product.getDocuments();
//    
//    Iterator<DocumentIF> it = docs.iterator();
//    while (it.hasNext())
//    {
//      DocumentIF doc = it.next();
//      
//      if (! (doc.getS3location().contains("/" + Product.ODM_ALL_DIR + "/") && doc.getS3location().endsWith(".zip")))
//      {
//        it.remove();
//      }
//    }
//    
//    DocumentIF lastDoc = null;
//    for (DocumentIF doc : docs)
//    {
//      if (lastDoc == null || doc.getLastModified().after(lastDoc.getLastModified()))
//      {
//        lastDoc = doc;
//      }
//    }
//    
//    if (lastDoc != null)
//    {
//      return component.download(lastDoc.getS3location());
//    }
//    else
//    {
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
//    }
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
    String existing = this.getWorkspace();

    this.setPublished(!this.isPublished());
    this.apply();

    UasComponent component = this.getComponent();

    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      if (document.getName().endsWith(".tif"))
      {
        String storeName = component.getStoreName(document.getS3location());

        if (GeoserverFacade.layerExists(existing, storeName))
        {
          Util.removeCoverageStore(existing, storeName);
        }
      }
    }

    Util.createImageServices(this.getWorkspace(), component);
  }

  @Override
  public void calculateKeys(List<UasComponentIF> components)
  {
    List<DocumentIF> documents = this.getDocuments();
    String workspace = this.getWorkspace();

    logger.trace("Calculating image keys for product [" + this.getOid() + "] with [" + documents.size() + "]");

    for (DocumentIF document : documents)
    {
      logger.trace("Checking document [" + document.getName() + "]");

      if (document.getName().endsWith(".png"))
      {
        this.imageKey = document.getS3location();

        logger.trace("Setting image key for product [" + this.getOid() + "] to [" + this.imageKey + "]");
      }
      else if (document.getName().endsWith(".tif"))
      {
        String storeName = components.get(components.size() - 1).getStoreName(document.getS3location());

        if (GeoserverFacade.layerExists(workspace, storeName))
        {
          if (document.getS3location().contains(ImageryComponent.ORTHO + "/"))
          {
            this.mapKey = storeName;
  
            logger.trace("Setting map key for product [" + this.getOid() + "] to [" + this.mapKey + "]");
          }
          else if (document.getS3location().contains(ImageryComponent.DEM + "/") && document.getName().equals("dsm.tif"))
          {
            this.demKey = storeName;
  
            logger.trace("Setting dem key for product [" + this.getOid() + "] to [" + this.demKey + "]");
          }
        }
      }
    }
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
