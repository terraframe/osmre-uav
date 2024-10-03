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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdGraphClassDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.metadata.MdEdge;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.SSLLocalhostTrustConfiguration;
import gov.geoplatform.uasdm.bus.InvalidUasComponentNameException;
import gov.geoplatform.uasdm.bus.UasComponentDeleteException;
import gov.geoplatform.uasdm.cog.TiTillerProxy.BBoxView;
import gov.geoplatform.uasdm.command.IndexDeleteStacCommand;
import gov.geoplatform.uasdm.command.ReIndexStacItemCommand;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.StacItem.Properties;
import gov.geoplatform.uasdm.model.StacLink;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.s3.S3RemoteFileService;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.ComponentProductDTO;
import gov.geoplatform.uasdm.view.ProductCriteria;
import gov.geoplatform.uasdm.view.SiteObject;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.spring.ApplicationContextHolder;

public class Product extends ProductBase implements ProductIF
{
  static
  {
    SSLLocalhostTrustConfiguration.trustLocalhost();
  }

  public static final String  ODM_ALL_DIR               = "odm_all";

  public static final String  MAPPABLE_ORTHO_REGEX      = ".*\\/" + ImageryComponent.ORTHO + "\\/[^\\/]+" + CogTifProcessor.COG_EXTENSION.replaceAll("\\.", "\\\\.");

  public static final String  ORTHO_PNG_REGEX           = ".*\\/" + ImageryComponent.ORTHO + "\\/[^\\/]+" + ".png".replaceAll("\\.", "\\\\.");

  public static final String  THUMBNAIL_ORTHO_PNG_REGEX = ".*\\/" + ImageryComponent.ORTHO + "\\/" + "thumbnails" + "\\/[^\\/]+orthophoto[^\\\\/]*" + ".png".replaceAll("\\.", "\\\\.");

  public static final String  MAPPABLE_DEM_REGEX        = ".*\\/" + ODMZipPostProcessor.DEM_GDAL + "\\/[^\\/]+" + CogTifProcessor.COG_EXTENSION.replaceAll("\\.", "\\\\.");

  public static final String  GEO_LOCATION_FILE         = "geo.txt";

  private static final Logger logger                    = LoggerFactory.getLogger(Product.class);

  private static final long   serialVersionUID          = -1476643617;

  private String              imageKey                  = null;

  public Product()
  {
    super();
  }

  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();

    if (!UasComponentIF.isValidName(this.getProductName()))
    {
      MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Product.CLASS);
      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Product.PRODUCTNAME);

      InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The product name [" + this.getProductName() + "] has an invalid character. Disallowed characters are " + UasComponentIF.DISALLOWED_FILENAME_REGEX);
      ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
      throw ex;
    }

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_PRODUCT).apply();
    }

    CollectionReportFacade.update(this).doIt();
  }

  @Override
  public boolean isPrimary()
  {
    return Boolean.TRUE.equals(this.getPrimary());
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    if (this.isLocked())
    {
      GenericException exception = new GenericException();
      exception.setUserMessage("Product can not be deleted because it is locked.");
      throw exception;
    }

    final SessionIF session = Session.getCurrentSession();

    if (session != null && ! ( session.userHasRole(RoleConstants.ADMIN) || this.getComponent().getOwnerOid().equals(session.getUser().getOid()) ))
    {
      final UasComponentDeleteException ex = new UasComponentDeleteException();
      ex.setTypeLabel(this.getClassDisplayLabel());
      ex.setComponentName(this.getName());

      throw ex;
    }

    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      document.delete(removeFromS3);
    }

    UasComponent component = this.getComponent();

    CollectionReportFacade.handleDeleteProduct(component).doIt();

    new IndexDeleteStacCommand(this).doIt();

    if (! ( component instanceof CollectionIF ))
    {
      CollectionStatus.deleteForProduct(this);

      this.getMetadata().ifPresent(meta -> {
        meta.delete();
      });
    }
    else if (this.isPrimary() && component instanceof CollectionIF)
    {
      CollectionIF col = ( (CollectionIF) component );

      List<ProductIF> prods = col.getProducts();
      if (prods.size() > 0)
      {
        Product prod = (Product) prods.get(0);
        prod.setPrimary(true);
        prod.apply();
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

  public static Product createIfNotExist(UasComponentIF uasComponent, String productName)
  {
    Product product = find(uasComponent, productName);

    if (product == null)
    {
      product = new Product();
      product.setName(uasComponent.getName());
      product.setProductName(productName);
      product.setPublished(false);

      if (! ( uasComponent instanceof CollectionIF ))
      {
        product.setPrimary(false);
      }
    }

    product.setLastUpdateDate(new Date());
    product.apply(uasComponent);

    return product;
  }

  public static Product createIfNotExistOrThrow(UasComponentIF uasComponent, String productName)
  {
    Product product = find(uasComponent, productName);

    if (product == null)
    {
      product = new Product();
      product.setName(uasComponent.getName());
      product.setProductName(productName);
      product.setPublished(false);

      if (! ( uasComponent instanceof CollectionIF ))
      {
        product.setPrimary(false);
      }
    }
    else
    {
      throw new DuplicateDataException("Product with name [" + productName + "] is already associated with the provided component. Choose a different product name, or a different associated component.", MdClassDAO.getMdClassDAO(Product.CLASS), Arrays.asList(Product.getProductNameMd()), Arrays.asList(productName));
    }

    product.setLastUpdateDate(new Date());
    product.apply(uasComponent);

    return product;
  }

  public static Product find(UasComponentIF component, String productName)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "')[productName = :productName])\n");
    statement.append("FROM :rid \n");

    final GraphQuery<Product> query = new GraphQuery<Product>(statement.toString());
    query.setParameter("rid", ( (UasComponent) component ).getRID());
    query.setParameter("productName", productName);

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
   * with the specified mapKey. The Cog must exist on S3 before calling this
   * method. If the bounding box cannot be calculated, for whatever reason, this
   * method will return null.
   */
  public BBoxView calculateBoundingBox()
  {
    DocumentIF mappable = null;

    Optional<DocumentIF> ortho = this.getMappableOrtho();
    Optional<DocumentIF> hillshade = this.getMappableDEM();

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
      // Map<String, CRSEnvelope> bboxes = layer.getBoundingBoxes();
      //
      // for (Entry<String, CRSEnvelope> entry : bboxes.entrySet())
      // {
      // String code = entry.getKey();
      // CRSEnvelope envelope = entry.getValue();
      //
      // try
      // {
      // // Mapbox's docs say that it's in 3857 but it's bounding box method
      // // expects 4326.
      // CoordinateReferenceSystem sourceCRS = CRS.decode(code);
      // CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
      // MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
      //
      // org.locationtech.jts.geom.Envelope jtsEnvelope = new
      // org.locationtech.jts.geom.Envelope();
      // jtsEnvelope.init(envelope.getMinX(), envelope.getMaxX(),
      // envelope.getMinY(), envelope.getMaxY());
      //
      // org.locationtech.jts.geom.Envelope env3857 =
      // JTS.transform(jtsEnvelope, transform);
      //
      // JSONArray json = new JSONArray();
      // json.put(env3857.getMinX());
      // json.put(env3857.getMaxX());
      // json.put(env3857.getMinY());
      // json.put(env3857.getMaxY());
      //
      // return json.toString();
      // }
      // catch (Throwable t)
      // {
      // // Perhaps there is another bounding box we can try?
      // }
      // }
      return RemoteFileFacade.getBoundingBox(this, mappable);
    }
    catch (Throwable t)
    {
      logger.error("Error when getting bounding box from mappable [" + mappable.getS3location() + "] for product [" + this.getName() + "].", t);
      return null;
    }
  }

  @Override
  public void updateBoundingBox(boolean newProduct)
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

      // if (component instanceof Collection)
      // {
      // Collection collection = (Collection) component;
      //
      // collection.setValue(Collection.NORTHBOUND, new
      // BigDecimal(bbox.getMaxLat()));
      // collection.setValue(Collection.SOUTHBOUND, new
      // BigDecimal(bbox.getMinLat()));
      // collection.setValue(Collection.EASTBOUND, new
      // BigDecimal(bbox.getMaxLong()));
      // collection.setValue(Collection.WESTBOUND, new
      // BigDecimal(bbox.getMinLong()));
      //
      // collection.apply();
      // }

      this.getMetadata().ifPresent(metadata -> {
        metadata.setNorthBound(new BigDecimal(bbox.getMaxLat()));
        metadata.setSouthBound(new BigDecimal(bbox.getMinLat()));
        metadata.setEastBound(new BigDecimal(bbox.getMaxLong()));
        metadata.setWestBound(new BigDecimal(bbox.getMinLong()));
        metadata.apply();
      });
    }
    else
    {
      int failCount = newProduct ? 0 : getBoundingBoxFailureCount();

      JSONObject jo = new JSONObject();
      jo.put("FailureCount", failCount + 1);
      this.setBoundingBox(jo.toString());
      this.apply();
    }
  }

  /**
   * @return -1 if the bounding box is valid. Zero if it has never been
   *         calculated. A number less than 4 or equal to 4 if it's been
   *         attempted and failed before.
   */
  public int getBoundingBoxFailureCount()
  {
    try
    {
      String bbox = super.getBoundingBox();
      if (bbox == null || bbox.length() == 0)
      {
        return 0;
      }

      if (bbox.startsWith("["))
      {
        JSONArray ja = new JSONArray(bbox);
        if (ja.length() == 4)
        {
          return -1;
        } // A valid bounding box
      }

      JSONObject jo = new JSONObject(bbox);
      if (!jo.has("FailureCount"))
      {
        return 0;
      }

      return jo.getInt("FailureCount");
    }
    catch (Throwable t)
    {
      return 0;
    }
  }

  public String getBoundingBoxInternal()
  {
    return super.getBoundingBox();
  }

  @Override
  public String getBoundingBox()
  {
    int fc = getBoundingBoxFailureCount();

    if (fc >= 0 && fc < 4)
    {
      updateBoundingBox(false);
      fc = getBoundingBoxFailureCount();
    }

    if (fc != -1)
    {
      return null;
    }

    return super.getBoundingBox();
  }

  public Envelope getEnvelope()
  {
    String bbox = this.getBoundingBox();

    if (bbox != null && bbox.length() > 0)
    {
      JSONArray array = new JSONArray(bbox);

      return new Envelope(array.getDouble(0), array.getDouble(1), array.getDouble(2), array.getDouble(3));
    }

    return null;
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
   * Downloads the product's ODM all.zip and refreshes S3 and database documents
   * with the data contained.
   * 
   * @param sessionId
   * @param productId
   * @throws InterruptedException
   */
  public void refreshDocuments() throws InterruptedException
  {
    UasComponent c = this.getComponent();

    if (c != null && c instanceof CollectionIF)
    {
      final CollectionIF collection = (CollectionIF) this.getComponent();

      boolean allZipExists = this.hasAllZip();

      if (allZipExists)
      {
        ODMZipPostProcessor uploader = new ODMZipPostProcessor(collection, null, this, null);

        uploader.processAllZip();
      }
    }
  }

  public boolean hasAllZip()
  {
    // return this.getDocuments().stream().filter(doc ->
    // doc.getS3location().matches(".*\\/odm_all\\/all.*\\.zip")).findAny();

    UasComponent c = this.getComponent();

    if (! ( c instanceof Collection ))
      return false;

    return ( (Collection) c ).getHasAllZip();
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

    List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, this.getS3location() + Product.ODM_ALL_DIR, new LinkedList<SiteObject>(), null, null).getObjects();

    SiteObject last = null;

    for (SiteObject item : items)
    {
      if (last == null || item.getLastModified().after(last.getLastModified()))
      {
        String extension = FilenameUtils.getExtension(item.getKey());
        String baseName = FilenameUtils.getBaseName(item.getKey());
        String folder = FilenameUtils.getBaseName(FilenameUtils.getFullPathNoEndSeparator(item.getKey()));

        if (folder.equals(ODM_ALL_DIR) && baseName.startsWith("all") && extension.equals("zip"))
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
    SiteObject all = this.getAllZip();

    if (all != null)
    {
      return this.getComponent().download(all.getKey());
    }
    else
    {
      throw new ProgrammingErrorException("All zip does not exist");
    }
  }

  @Override
  public boolean isLocked()
  {
    return this.getLocked() != null && this.getLocked();
  }

  @Override
  @Transaction
  public void toggleLock()
  {
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      Map<String, String> roles = session.getUserRoles();

      SingleActorDAOIF user = session.getUser();

      String ownerOid = this.getComponent().getOwnerOid();

      if (user.getOid().equals(ownerOid) || roles.containsKey("geoprism.admin.Administrator"))
      {
        this.setLocked(!this.isLocked());
        this.setLockedById(user.getOid());
        this.apply();

        return;
      }
    }

    GenericException ex = new GenericException();
    ex.setUserMessage("Only the owner can lock a product");
    throw ex;
  }

  @Override
  @Transaction
  public void togglePublished()
  {
    if (this.isLocked())
    {
      GenericException ex = new GenericException();
      ex.setUserMessage("The product has been locked and can not be changed.");
      throw ex;
    }

    UasComponent component = this.getComponent();

    if (component.isPrivate() && !this.isPublished())
    {
      GenericException ex = new GenericException();
      ex.setUserMessage("Private collections can not be published");
      throw ex;
    }

    IndexService.removeStacItems(this);

    try
    {
      this.setPublished(!this.isPublished());
      this.apply();

      List<DocumentIF> documents = this.getDocuments();

      for (DocumentIF document : documents)
      {
        if (this.isPublished())
        {
          // Add to public S3 bucket
          RemoteFileFacade.copyObject(document.getS3location(), AppProperties.getBucketName(), document.getS3location(), AppProperties.getPublicBucketName());
        }
        else
        {
          // Remove from public S3 bucket
          RemoteFileFacade.deleteObject(document.getS3location(), AppProperties.getPublicBucketName());
        }
      }
    }
    finally
    {
      if (!component.isPrivate())
      {
        new ReIndexStacItemCommand(this).doIt();
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

  public Optional<DocumentIF> getMappableOrtho()
  {
    return this.getDocuments().stream().filter(doc -> doc.getS3location().matches(MAPPABLE_ORTHO_REGEX)).findAny();
  }

  public Optional<DocumentIF> getOrthoPng()
  {
    return this.getDocuments().stream().filter(doc -> doc.getS3location().matches(MAPPABLE_ORTHO_REGEX)).findAny();
  }

  public Optional<DocumentIF> getMappableDEM()
  {
    return this.getDocuments().stream().filter(doc -> doc.getS3location().matches(MAPPABLE_DEM_REGEX)).findAny();
  }

  public List<DocumentIF> getMappableDocuments()
  {
    return this.getDocuments().stream().filter(doc -> doc.isMappable()).collect(Collectors.toList());
  }

  @Override
  public StacItem toStacItem()
  {
    List<DocumentIF> documents = this.getDocuments();

    UasComponent component = this.getComponent();

    List<UasComponentIF> ancestors = component.getAncestors();

    Envelope envelope = this.getEnvelope();

    StacItem item = new StacItem();
    item.setId(this.getOid());
    item.setPublished(this.isPublished());
    if (envelope != null)
    {
      item.setBbox(envelope);
      item.setGeometry(new GeometryFactory(new PrecisionModel(), 4326).toGeometry(envelope));
    }

    Properties properties = new Properties();
    properties.setTitle(component.getName());
    properties.setCollection(component.getName());
    properties.setDescription(component.getDescription());
    properties.setUpdated(this.getLastUpdateDate());

    Date dateTime = new Date();

    Optional<CollectionMetadata> opMeta = getMetadata();
    if (opMeta.isPresent())
    {
      CollectionMetadata metadata = opMeta.get();

      Sensor sensor = metadata.getSensor();

      if (sensor != null)
      {
        properties.setSensor(sensor.getName());
      }

      UAV uav = metadata.getUav();

      if (uav != null)
      {
        properties.setFaaNumber(uav.getFaaNumber());
        properties.setSerialNumber(uav.getSerialNumber());

        Platform platform = uav.getPlatform();

        if (platform != null)
        {
          properties.setPlatform(platform.getName());
        }
      }

      dateTime = metadata.getCollectionDate();

      if (dateTime == null)
      {
        dateTime = metadata.getCollectionEndDate();
      }

      if (dateTime == null)
      {
        dateTime = this.getLastUpdateDate();
      }
    }

    properties.setDatetime(dateTime);

    item.setProperties(properties);

    for (UasComponentIF ancestor : ancestors)
    {
      properties.set(ancestor.getSolrNameField(), ancestor.getName());
    }

    for (DocumentIF document : documents)
    {
      String bucketName = this.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
      final String location = "s3://" + bucketName + "/" + document.getS3location();

      if ( ( location.contains("/" + ImageryComponent.DEM + "/") && location.toUpperCase().endsWith(".TIF") ) || ( location.contains("/" + ImageryComponent.ORTHO + "/") && location.toUpperCase().endsWith(".TIF") ) || ( location.contains("/" + ImageryComponent.ORTHO + "/") && location.toUpperCase().endsWith(".PNG") ))
      {
        String ext = FilenameUtils.getExtension(document.getName());

        if (ext.toUpperCase().equals("PNG"))
        {
          String title = "Thumbnail";
          String role = "visual";

          item.addAsset("thumbnail-hd", StacItem.buildAsset("image/png", title, location, role));

          // Private thumbnail
          String rootPath = FilenameUtils.getPath(document.getS3location());
          String baseName = FilenameUtils.getBaseName(document.getName());
          String thumbnail = "s3://" + AppProperties.getBucketName() + "/" + rootPath + "thumbnails/" + baseName + ".png";

          item.addAsset("thumbnail", StacItem.buildAsset("image/png", title, thumbnail, role));
        }
        else
        {
          String assetName = FilenameUtils.getBaseName(document.getName());

          String type = "image/tiff; application=geotiff;";

          if (location.toUpperCase().endsWith("COG.TIF"))
          {
            type = "image/tiff; application=geotiff; profile=cloud-optimized";
          }

          String title = "Visual";
          String role = "visual";

          item.addAsset(assetName, StacItem.buildAsset(type, title, location, role));
        }
      }
    }

    // Add the self link
    String bucket = item.isPublished() ? AppProperties.getPublicBucketName() : AppProperties.getBucketName();
    String url = "s3://" + bucket + "/" + S3RemoteFileService.STAC_BUCKET + "/" + item.getId() + ".json";

    item.addLink(StacLink.build(url, "self", "application/json"));

    return item;
  }

  public Optional<CollectionMetadata> getMetadata()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Product.CLASS);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PRODUCT_HAS_METADATA);

    // USING THE RID FAILS ON TRANSACTIONS WHICH HAVE NOT BEEN COMMITTED, WORK
    // AROUND BY DOING A SEARCH BASED ON THE OID
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( ");
    statement.append(" OUT('" + mdEdge.getDBClassName() + "')");
    statement.append(")");
    statement.append(" FROM ( ");
    statement.append("   SELECT FROM " + mdVertex.getDBClassName() + " WHERE oid = :oid");
    statement.append(" ) ");

    final GraphQuery<CollectionMetadata> query = new GraphQuery<CollectionMetadata>(statement.toString());
    query.setParameter("oid", this.getOid());

    return Optional.ofNullable(query.getSingleResult());
  }

  public static List<ProductIF> getProducts()
  {
    final MdGraphClassDAOIF mdEdge = MdGraphClassDAO.getMdGraphClassDAO(Product.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdEdge.getDBClassName() + " \n");

    final GraphQuery<ProductIF> query = new GraphQuery<ProductIF>(statement.toString());

    return query.getResults();
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

  /*
   * 
   * TRAVERSE OUT ('mission_has_collection0') FROM ( TRAVERSE OUT
   * ('project_has_mission0') FROM ( TRAVERSE OUT('site_has_project') FROM (
   * 
   * 
   * TRAVERSE OUT('component_has_product') FROM ( SELECT FROM ( SELECT FROM (
   * SELECT FROM ( TRAVERSE OUT ('mission_has_collection0') FROM ( TRAVERSE OUT
   * ('project_has_mission0') FROM ( TRAVERSE OUT('site_has_project') FROM (
   * SELECT FROM ( SELECT FROM ( TRAVERSE OUT('g_0__operational',
   * 'ha_0__graph_541834') FROM #274:1 ) WHERE @class = 'site0' AND
   * organization.code = '100013241' ) ) ) ) ) ) ) WHERE (isPrivate = false OR
   * isPrivate IS NULL OR owner = 'a2c721a5-a547-4627-bf3a-8f5ae000052a' OR
   * in('user_has_access')[user = 'a2c721a5-a547-4627-bf3a-8f5ae000052a'].size()
   * > 0 ) ORDER BY name ASC)
   */
  public static List<ComponentProductDTO> getProducts(ProductCriteria criteria)
  {
    LabeledPropertyGraphTypeVersionBusinessServiceIF service = ApplicationContextHolder.getBean(LabeledPropertyGraphTypeVersionBusinessServiceIF.class);

    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(criteria.getHierarchy());
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    HierarchyTypeSnapshot hierarchyType = service.getHierarchies(version).get(0);

    SynchronizationEdge synchronizationEdge = SynchronizationEdge.get(version);
    MdEdge siteEdge = synchronizationEdge.getGraphEdge();

    VertexObject object = service.getObject(version, criteria.getUid());

    String sortField = criteria.getSortField();
    String sortOrder = criteria.getSortOrder();

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("rid", object.getRID());

    criteria.getConditions().forEach(condition -> {
      parameters.put(condition.getField(), condition.getValue());
    });

    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    boolean hasMetadataSort = ( sortField.equals("sensor") || sortField.equals("serialNumber") || sortField.equals("faaNumber") );

    StringBuilder statement = new StringBuilder();
    statement.append("TRAVERSE OUT('" + mdEdge.getDBClassName() + "') FROM (");

    if (hasMetadataSort)
    {
      String sortAttribute = "sensor.name";

      if (sortField.equals("serialNumber"))
      {
        sortAttribute = "uav.serialNumber";
      }
      else if (sortField.equals("faaNumber"))
      {
        sortAttribute = "uav.faaNumber";
      }

      statement.append("  SELECT @rid FROM (\n");
      statement.append("    SELECT @rid, first(out('collection_has_metadata'))." + sortAttribute + " AS sortBy FROM (\n");
    }
    else
    {
      statement.append("  SELECT FROM (\n");
    }

    // statement.append(" SELECT EXPAND(OUT('site_has_project')");
    //
    // criteria.getConditions().stream().filter(condition ->
    // condition.isProject()).forEach(condition -> {
    // statement.append("[" + condition.getSQL() + " = :" + condition.getField()
    // + "]");
    // });
    //
    // statement.append(".OUT('project_has_mission0').OUT('mission_has_collection0'))
    // FROM (\n");

    statement.append("TRAVERSE OUT ('mission_has_collection0') FROM ( TRAVERSE OUT ('project_has_mission0') FROM ( TRAVERSE OUT('site_has_project') FROM (");

    statement.append("      SELECT FROM (\n");
    statement.append("        TRAVERSE OUT('" + hierarchyType.getGraphMdEdge().getDbClassName() + "', '" + siteEdge.getDbClassName() + "') FROM :rid");
    statement.append("      ) WHERE @class = 'site0' \n");

    criteria.getConditions().stream().filter(condition -> condition.isSite()).forEach(condition -> {
      statement.append("      AND " + condition.getSQL() + " = :" + condition.getField() + " \n");
    });

    statement.append("    )\n");
    statement.append("  )))\n");

    // Add the filter for permissions
    MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);
    MdEdgeDAOIF accessEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.USER_HAS_ACCESS);

    MdAttributeDAOIF privateAttribute = mdClass.definesAttribute(UasComponent.ISPRIVATE);
    MdAttributeDAOIF ownerAttribute = mdClass.definesAttribute(UasComponent.OWNER);

    SessionIF session = Session.getCurrentSession();

    statement.append(" WHERE (" + privateAttribute.getColumnName() + " = :isPrivate \n");
    statement.append("   OR " + privateAttribute.getColumnName() + " IS NULL \n");

    if (session != null)
    {
      statement.append("   OR " + ownerAttribute.getColumnName() + " = :owner \n");
      statement.append("   OR in('" + accessEdge.getDBClassName() + "')[user = :owner].size() > 0 \n");
    }
    statement.append(" ) \n");

    parameters.put("isPrivate", false);

    if (session != null)
    {
      parameters.put("owner", session.getUser().getOid());
    }

    criteria.getConditions().stream().filter(condition -> condition.isCollection()).forEach(condition -> {
      statement.append(" AND " + condition.getSQL() + " = :" + condition.getField() + " \n");
    });

    criteria.getConditions().stream().filter(condition -> condition.isMetadata()).forEach(condition -> {
      statement.append(" AND (first(out('collection_has_metadata'))." + condition.getSQL() + " = :" + condition.getField() + " OR OUT('component_has_product').out('product_has_metadata')." + condition.getSQL() + " = :" + condition.getField() + ") \n");
    });

    if (sortField.equals("name"))
    {
      statement.append("  ORDER BY name " + sortOrder);
    }
    else if (sortField.equals("collectionDate"))
    {
      statement.append("  ORDER BY collectionDate " + sortOrder);
    }
    else if (hasMetadataSort)
    {
      statement.append(")\n");
      statement.append("  ORDER BY sortBy " + sortOrder);
    }
    statement.append(")\n");

    // if (sortField.equals(Product.LASTUPDATEDATE))
    // {
    // statement.append(" ORDER BY " + sortField + " " + sortOrder);
    // }

    final GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString(), parameters);

    return ComponentProductDTO.process(query.getResults());
  }

  public String getS3location()
  {
    return ImageryComponent.PRODUCTS + "/" + this.getProductName() + "/";
  }
}
