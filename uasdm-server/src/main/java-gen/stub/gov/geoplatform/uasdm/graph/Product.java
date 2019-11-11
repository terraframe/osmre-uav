package gov.geoplatform.uasdm.graph;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.runwaysdk.business.graph.VertexQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.gis.geoserver.GeoserverFacade;

public class Product extends ProductBase implements ProductIF
{
  private static final Logger logger           = LoggerFactory.getLogger(Product.class);

  private static final long   serialVersionUID = -1476643617;

  private String              imageKey         = null;

  private String              mapKey           = null;

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
    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      document.delete(removeFromS3);
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

    final VertexQuery<Product> query = new VertexQuery<Product>(statement.toString());
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
      WMSCapabilities capabilities = GeoserverFacade.getCapabilities(mapKey);

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
      else if (document.getName().endsWith(".tif"))
      {
        String storeName = components.get(components.size() - 1).getStoreName(document.getS3location());

        if (GeoserverFacade.layerExists(storeName))
        {
          this.mapKey = storeName;
        }
      }
    }
  }
}
