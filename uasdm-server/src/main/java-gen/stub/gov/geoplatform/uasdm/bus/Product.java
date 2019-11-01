package gov.geoplatform.uasdm.bus;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.gis.geoserver.GeoserverFacade;

public class Product extends ProductBase
{
  private static final long serialVersionUID = 1797567850;
  
  private static final Logger logger = LoggerFactory.getLogger(Product.class);
  
  private String imageKey = null;
  
  private String mapKey = null;
  
  public Product()
  {
    super();
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
    List<Document> documents = new LinkedList<Document>();

    try (OIterator<? extends Document> it = this.getAllDocuments())
    {
      documents.addAll(it.getAll());
    }

    for (Document document : documents)
    {
      document.delete(removeFromS3);
    }

    super.delete();
  }

  public void addDocuments(List<Document> documents)
  {
    for (Document document : documents)
    {
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

  public static Product createIfNotExist(UasComponent uasComponent)
  {
    Product product = find(uasComponent);

    if (product == null)
    {
      product = new Product();
      product.setComponent(uasComponent);
      product.setName(uasComponent.getName());
      product.apply();
    }

    return product;
  }
  
  public static Product find(UasComponent uasComponent)
  {
    ProductQuery query = new ProductQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(uasComponent));

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
  
  // This code fixes java.security.cert.CertificateException: No subject alternative names present
  // When run in dev environments
  private static void disableSslVerification() {
      try
      {
          // Create a trust manager that does not validate certificate chains
          TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  return null;
              }
              public void checkClientTrusted(X509Certificate[] certs, String authType) {
              }
              public void checkServerTrusted(X509Certificate[] certs, String authType) {
              }
          }
          };
  
          // Install the all-trusting trust manager
          SSLContext sc = SSLContext.getInstance("SSL");
          sc.init(null, trustAllCerts, new java.security.SecureRandom());
          HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
  
          // Create all-trusting host name verifier
          HostnameVerifier allHostsValid = new HostnameVerifier() {
              public boolean verify(String hostname, SSLSession session) {
                  return true;
              }
          };
  
          // Install the all-trusting host verifier
          HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
      } catch (KeyManagementException e) {
          e.printStackTrace();
      }
  }
  
  static
  {
    if (Boolean.valueOf(System.getProperty("com.sun.jndi.ldap.object.disableEndpointIdentification")))
    {
      disableSslVerification();
    }
  }
  
  /**
   * This method calculates a 4326 CRS bounding box for a given raster layer with the specified mapKey. This layer
   * must exist on Geoserver before calling this method. If the bounding box cannot be calculated, for whatever
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
          CoordinateReferenceSystem sourceCRS = CRS.decode(code);
          CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326"); // Mapbox's docs say that it's in 3857 but its bounding box method expects 4326.
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

    List<UasComponent> components = component.getAncestors();
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
  
  public String getImageKey()
  {
    return this.imageKey;
  }
  
  public String getMapKey()
  {
    return this.mapKey;
  }
  
  public void calculateKeys(List<UasComponent> components)
  {
    List<Document> documents = new LinkedList<Document>();

    try (OIterator<? extends Document> it = this.getAllDocuments())
    {
      documents.addAll(it.getAll());
    }
    
    for (Document document : documents)
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
