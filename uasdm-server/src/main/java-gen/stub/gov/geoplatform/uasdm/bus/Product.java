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
package gov.geoplatform.uasdm.bus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Product extends ProductBase implements ProductIF
{
  private static final long   serialVersionUID = 1797567850;

  private static final Logger logger           = LoggerFactory.getLogger(Product.class);

  private String              imageKey         = null;

  private String              mapKey           = null;

  public Product()
  {
    super();
  }

  public String getImageKey()
  {
    return this.imageKey;
  }

  public String getMapKey()
  {
    return this.mapKey;
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    List<Document> documents = new LinkedList<Document>();

    try (OIterator<? extends Document> it = this.getAllDocuments())
    {
      documents.addAll(it.getAll());
    }

    for (Document document : documents)
    {
      document.delete(removeFromS3, true);
    }

    super.delete();
  }

  public void addDocuments(List<DocumentIF> documents)
  {
    for (DocumentIF doc : documents)
    {
      Document document = (Document) doc;

      ProductHasDocument pd = getProductHasDocument(document);

      if (pd == null)
      {
        this.addDocuments(document).apply();
      }
    }
  }

  public ProductHasDocument getProductHasDocument(Document document)
  {
    ProductHasDocumentQuery query = new ProductHasDocumentQuery(new QueryFactory());
    query.WHERE(query.getParent().EQ(this));
    query.AND(query.getChild().EQ(document));

    try (OIterator<? extends ProductHasDocument> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  @Override
  public List<DocumentIF> getGeneratedFromDocuments()
  {
    List<DocumentIF> generated = new LinkedList<DocumentIF>();

    try (OIterator<? extends Document> it = this.getAllGeneratedDocuments())
    {
      generated.addAll(it.getAll());
    }

    return generated;
  }

  @Override
  public Page<DocumentIF> getGeneratedFromDocuments(Integer pageNumber, Integer pageSize)
  {
    final List<DocumentIF> documents = this.getGeneratedFromDocuments();

    return new Page<DocumentIF>(1, 1, 1, documents);
  }

  public static Product createIfNotExist(UasComponentIF uasComponent)
  {
    Product product = find(uasComponent);

    if (product == null)
    {
      product = new Product();
      product.setComponent((UasComponent) uasComponent);
      product.setName(uasComponent.getName());
      product.setPublished(false);
      product.apply();
    }

    return product;
  }

  public static Product find(UasComponentIF uasComponent)
  {
    ProductQuery query = new ProductQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ((UasComponent) uasComponent));

    try (OIterator<? extends Product> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  @Transaction
  public void clear()
  {
    try (OIterator<? extends DocumentGeneratedProduct> relationships = this.getAllGeneratedDocumentsRel())
    {
      List<? extends DocumentGeneratedProduct> list = relationships.getAll();

      for (DocumentGeneratedProduct relationship : list)
      {
        relationship.delete();
      }
    }
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
//    try
//    {
//      WMSCapabilities capabilities = GeoserverFacade.getCapabilities(this.getWorkspace(), mapKey);
//
//      List<Layer> layers = capabilities.getLayerList();
//
//      Layer layer = null;
//
//      if (layers.size() == 0)
//      {
//        logger.error("Unable to calculate bounding box for product [" + this.getName() + "]. Geoserver did not return any layers.");
//        return null;
//      }
//      else if (layers.size() == 1)
//      {
//        layer = layers.get(0);
//      }
//      else
//      {
//        for (Layer potential : layers)
//        {
//          if (potential.getName() != null && potential.getName().equals(mapKey))
//          {
//            layer = potential;
//            break;
//          }
//        }
//
//        if (layer == null)
//        {
//          logger.error("Unable to calculate bounding box for product [" + this.getName() + "]. Geoserver returned more than one layer and none of the layers matched what we were looking for.");
//          return null;
//        }
//      }
//
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
//
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
//    }
//    catch (Throwable t)
//    {
//      logger.error("Error when getting bounding box from layer [" + mapKey + "] for product [" + this.getName() + "].", t);
//      return null;
//    }

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
        this.lock();
        this.setBoundingBox(bbox);
        this.apply();
      }
    }
  }

  @Override
  public boolean isPublished()
  {
    return this.getPublished() != null && this.getPublished();
  }

//  @Override
//  public String getWorkspace()
//  {
//    if (isPublished())
//    {
//      return AppProperties.getPublicWorkspace();
//    }
//
//    return GeoserverProperties.getWorkspace();
//  }

  @Override
  public void createImageService(boolean refreshMosaic)
  {
//    GeoserverPublisher.createImageServices(this.isPublished(), this.getComponent(), refreshMosaic);
  }

  @Override
  @Transaction
  public void togglePublished()
  {
//    String existing = this.getWorkspace();
//
//    this.setPublished(!this.isPublished());
//    this.apply();
//
//    UasComponent component = this.getComponent();
//
//    GeoserverPublisher.removeImageServices(existing, component, false);
//    
//    GeoserverPublisher.createImageServices(this.getWorkspace(), component, true);
  }

  public void calculateKeys(List<UasComponentIF> components)
  {
//    List<Document> documents = new LinkedList<Document>();
//    String workspace = this.getWorkspace();
//
//    try (OIterator<? extends Document> it = this.getAllDocuments())
//    {
//      documents.addAll(it.getAll());
//    }
//
//    for (Document document : documents)
//    {
//      if (document.getName().endsWith(".png"))
//      {
//        this.imageKey = document.getS3location();
//      }
//      else if (document.getName().endsWith(".tif"))
//      {
//        String storeName = components.get(components.size() - 1).getStoreName(document.getS3location());
//
//        if (GeoserverFacade.layerExists(workspace, storeName))
//        {
//          this.mapKey = storeName;
//        }
//      }
//    }
  }

  public static List<ProductIF> getProduct()
  {
    ProductQuery query = new ProductQuery(new QueryFactory());

    try (OIterator<? extends Product> it = query.getIterator())
    {
      return new LinkedList<ProductIF>(it.getAll());
    }
  }

  @Override
  public List<DocumentIF> getDocuments()
  {
    throw new UnsupportedOperationException();
  }
}
