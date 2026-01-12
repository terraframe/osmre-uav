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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.Pair;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.metadata.MdBusiness;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.CollectionStatusQuery;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.DuplicateComponentException;
import gov.geoplatform.uasdm.bus.InvalidUasComponentNameException;
import gov.geoplatform.uasdm.bus.UasComponentDeleteException;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.command.IndexCreateDocumentCommand;
import gov.geoplatform.uasdm.command.IndexDeleteDocumentCommand;
import gov.geoplatform.uasdm.command.IndexDeleteDocumentsCommand;
import gov.geoplatform.uasdm.command.IndexUpdateDocumentCommand;
import gov.geoplatform.uasdm.command.ReIndexStacItemCommand;
import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentRawSet;
import gov.geoplatform.uasdm.model.ComponentWithAttributes;
import gov.geoplatform.uasdm.model.CompositeDeleteException;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.raw.FileUploadProcessor;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.AdminCondition;
import gov.geoplatform.uasdm.view.Artifact;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.ComponentProductDTO;
import gov.geoplatform.uasdm.view.CreateRawSetView;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import net.geoprism.GeoprismUser;
import net.geoprism.rbac.RoleConstants;

public abstract class UasComponent extends UasComponentBase implements UasComponentIF
{
  private static final long serialVersionUID = -1526604195;

  private Logger            log              = LoggerFactory.getLogger(UasComponent.class);

  public UasComponent()
  {
    super();
  }

  /**
   * There will be a default child type for each node.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public abstract UasComponent createDefaultChild();

  protected abstract MdEdgeDAOIF getParentMdEdge();

  protected abstract MdEdgeDAOIF getChildMdEdge();

  /**
   * Create the child of the given type.
   * 
   * @param return
   *          the child of the given type. It assumes the type is valid. It is
   *          the type name of the Runway {@link MdBusiness}.
   * 
   * @return a new {@link UasComponent} of the correct type.
   */
  public UasComponent createChild(String typeName)
  {
    return this.createDefaultChild();
  }

  @Override
  public void regenerateMetadata()
  {
    List<ComponentProductDTO> derivedProducts = this.getDerivedProducts(null, null);

    derivedProducts.forEach(view -> {
      view.getProducts().forEach(product -> {
        new GenerateMetadataCommand(view.getComponent(), (Product) product, product.getMetadata().orElseThrow()).doIt();
      });
    });
  }

  /**
   * @return The name of the solr field for the components id.
   */
  public abstract String getSolrIdField();

  /**
   * @return The name of the solr field for the components name.
   */
  public abstract String getSolrNameField();

  protected abstract List<String> buildProductExpandClause();

  @Override
  public String getS3location(ProductIF product, String folderOrFilename)
  {
    if (StringUtils.isBlank(folderOrFilename))
      folderOrFilename = ImageryComponent.RAW;

    String ending = "";
    if (!folderOrFilename.contains("."))
      ending = "/";

    if (product == null)
    {
      return this.getS3location() + folderOrFilename + ending;
    }

    return this.getS3location() + product.getS3location() + folderOrFilename + ending;
  }

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  public void applyWithParent(UasComponentIF parent)
  {
    boolean isNew = this.isNew();

    if (isNew && this.getFolderName() == null)
    {
      String folderName = this.generateFolderName(parent);

      this.setFolderName(folderName);
    }

    if (isNew || this.isModified(UasComponent.FOLDERNAME))
    {
      String name = this.getFolderName();

      if (!UasComponentIF.isValidName(name))
      {
        MdVertexDAOIF mdBusiness = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);
        MdAttributeDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.FOLDERNAME);

        InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The folder name field has an invalid character");
        ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
        throw ex;
      }
    }

    if (isNew)
    {
      if (parent != null)
      {
        boolean isDuplicate = this.isDuplicateFolderName(parent, this.getFolderName());

        if (isDuplicate)
        {
          DuplicateComponentException e = new DuplicateComponentException();
          e.setParentName(parent.getName());
          e.setChildComponentLabel(this.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
          e.setChildName(this.getFolderName());

          throw e;
        }
      }

      String key;

      if (parent != null)
      {
        key = this.buildS3Key(parent);
      }
      else
      {
        key = this.buildS3Key(null);
      }

      this.setS3location(key);

      this.createS3Folder(key);
    }

    this.apply();

    if (parent != null)
    {
      this.addComponent(parent);
    }

    if (isNew)
    {
      new IndexCreateDocumentCommand(this.getAncestors(), this).doIt();
    }
  }

  @Transaction
  public void apply(boolean regenerateMetadata)
  {
    boolean isNameModified = this.isModified(UasComponent.NAME);
    boolean needsUpdate = this.needsUpdate();
    boolean isNew = this.isNew();

    if (isNew && this.getOwnerOid() == null)
    {
      final SingleActor user = GeoprismUser.getCurrentUser();

      if (user != null)
      {
        this.setOwner(user);
      }
    }

    if (!isNew && this.isModified(UasComponent.ISPRIVATE))
    {
      SessionIF session = Session.getCurrentSession();

      // Session will be null during a patch
      if (session != null && ! ( session.userHasRole(RoleConstants.ADMIN) || this.getOwnerOid().equals(session.getUser().getOid()) ))
      {
        GenericException exception = new GenericException();
        exception.setUserMessage("Only the owner may change component visibility");
        throw exception;
      }
    }

    super.apply();

    if (!isNew)
    {
      if (needsUpdate || isNameModified)
      {
        new IndexUpdateDocumentCommand(this, isNameModified).doIt();

        List<ComponentProductDTO> derivedProducts = this.getDerivedProducts(null, null);

        // Re-index all of the derived products below this component
        derivedProducts.forEach(view -> {
          view.getProducts().forEach(prod -> new ReIndexStacItemCommand(view.getPrimaryOrAny()).doIt());
        });
      }

      // Site data is not included in the XML metadata spec and as
      // such we do not need to update when there is a change.
      if (regenerateMetadata)
      {
        this.regenerateMetadata();
      }

      CollectionReportFacade.update(this).doIt();
    }
  }

  @Override
  @Transaction
  public void apply()
  {
    this.apply(true);
  }

  public String generateFolderName(UasComponentIF parent)
  {
    String original = this.createSafeFolderName(this.getName()).trim();
    String folderName = new String(original);

    int count = 0;

    while (this.isDuplicateFolderName(parent, folderName))
    {
      folderName = original + count;

      count++;
    }

    return folderName;
  }

  private String createSafeFolderName(String name)
  {
    final String lower = name.toLowerCase().replaceAll("\\s", "");
    StringBuilder folderName = new StringBuilder();

    for (int i = 0; i < lower.length(); i++)
    {
      final char c = lower.charAt(i);

      if (UasComponentIF.isValid(c))
      {
        folderName.append(c);
      }
    }

    return folderName.toString();
  }

  protected boolean needsUpdate()
  {
    return this.isModified(UasComponent.DESCRIPTION) || this.isModified(UasComponent.ISPRIVATE);
  }

  @Transaction
  public void delete()
  {
    log.info("Deleting component [" + this.getName() + "]");

    // Ensure that a component can only be deleted by an admin or the owner of
    // the component
    final SessionIF session = Session.getCurrentSession();

    if (session != null && ! ( session.userHasRole(RoleConstants.ADMIN) || this.getOwnerOid().equals(session.getUser().getOid()) ))
    {
      final UasComponentDeleteException ex = new UasComponentDeleteException();
      ex.setTypeLabel(this.getClassDisplayLabel());
      ex.setComponentName(this.getName());

      throw ex;
    }

    CompositeDeleteException exception = new CompositeDeleteException();

    // Delete all of the child components
    List<UasComponentIF> children = this.getChildren();

    for (UasComponentIF child : children)
    {
      try
      {
        child.delete();
      }
      catch (UasComponentDeleteException e)
      {
        exception.add(e);
      }
      catch (CompositeDeleteException e)
      {
        exception.addAll(e.getExceptions());
      }
    }

    if (exception.hasExceptions())
    {
      throw exception;
    }

    List<AbstractWorkflowTask> tasks = this.getTasks();

    for (AbstractWorkflowTask task : tasks)
    {
      task.delete();
    }

    // Delete all of the products
    List<Product> products = this.getProducts();

    for (Product product : products)
    {
      product.delete();
    }

    // Delete all of the documents
    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      document.delete();
    }

    CollectionStatusQuery query = new CollectionStatusQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this.getOid()));
    try (OIterator<? extends CollectionStatus> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        iterator.next().delete();
      }
    }

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.getS3location(), null);
    }

    final IndexDeleteDocumentsCommand command = new IndexDeleteDocumentsCommand(this.getSolrIdField(), this.getOid());
    command.doIt();
  }

  /**
   * Builds a key for S3 that conforms to the directory structure requirements.
   * If the parent is null, then
   * 
   * @param uasComponent
   *          null if no parent
   * @return a key for S3 that conforms to the directory structure requirements.
   */
  public String buildS3Key(UasComponentIF parent)
  {
    String key = new String();

    if (parent != null)
    {
      key += parent.getS3location();
    }

    key += this.getFolderName() + "/";

    return key;
  }

  protected void createS3Folder(String key)
  {
    RemoteFileFacade.createFolder(key);
  }

  protected void deleteS3Folder(String key, String folderName)
  {
    new RemoteFileDeleteCommand(key, this).doIt();
  }

  @Override
  public boolean isPrivate()
  {
    return this.getIsPrivate() != null && this.getIsPrivate();
  }

  @Override
  public JSONArray getArtifacts()
  {
    JSONArray response = new JSONArray();

    List<Product> products = this.getProducts();

    for (Product product : products)
    {
      Artifact[] artifacts = getArtifactObjects(product);

      JSONObject map = new JSONObject();
      map.put("productName", product.getProductName() == null ? "" : product.getProductName());
      map.put("primary", product.isPrimary());

      for (Artifact artifact : artifacts)
      {
        map.put(artifact.getFolder(), artifact.toJSON());
      }

      response.put(map);
    }

    return response;
  }

  public Artifact[] getArtifactObjects(ProductIF product)
  {
    Artifact[] artifacts = new Artifact[] { new Artifact(ImageryComponent.DEM, ".TIF", ".TIFF"), new Artifact(ImageryComponent.ORTHO, ".TIF", ".TIFF"), new Artifact(ImageryComponent.PTCLOUD, ".LAZ", ".LAS") };

    List<SiteObject> objects = new ArtifactQuery(this, product).getSiteObjects();

    for (SiteObject object : objects)
    {
      for (Artifact artifact : artifacts)
      {
        artifact.process(object);
      }
    }

    return artifacts;
  }

  @Override
  @Transaction
  public void removeArtifacts(ProductIF product, String folder, boolean updateMetadata)
  {
    if (folder.equalsIgnoreCase(ImageryComponent.RAW) || folder.equalsIgnoreCase(ImageryComponent.VIDEO))
    {
      return;
    }

    List<Document> documents = new SiteObjectDocumentQuery(this, product, folder).getDocuments();

    if (folder.equals(ImageryComponent.PTCLOUD))
    {
      documents.addAll(new SiteObjectDocumentQuery(this, product, ODMZipPostProcessor.POTREE).getDocuments());
    }
    else if (folder.equals(ImageryComponent.DEM))
    {
      documents.addAll(new SiteObjectDocumentQuery(this, product, ODMZipPostProcessor.DEM_GDAL).getDocuments());
    }

    for (Document document : documents)
    {
      document.delete(true);
    }

    // TODO: How to handle this with versions
    // if (new ArtifactQuery(this, product).getDocuments().size() == 0)
    // {
    // // product.delete();
    // }

    // Re-index the products
    new ReIndexStacItemCommand(product).doIt();

    if (updateMetadata)
    {
      new GenerateMetadataCommand(product.getComponent(), (Product) product, product.getMetadata().orElseThrow()).doIt();
    }
  }

  public SiteObjectsResultSet getSiteObjects(String folder, Long pageNumber, Long pageSize)
  {
    return new SiteObjectsResultSet(0L, pageNumber, pageSize, new LinkedList<SiteObject>(), folder);
  }

  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    // TODO: Handle different products
    SiteObjectDocumentQueryIF query = folder.equals("artifacts") ? new ArtifactQuery(this, this.getPrimaryProduct()) : new SiteObjectDocumentQuery(this, null, folder);

    if (pageNumber != null && pageSize != null)
    {
      query.setSkip( ( ( pageNumber - 1 ) * pageSize ));
      query.setLimit(pageSize);
    }

    Long count = query.getCount();
    List<SiteObject> items = query.getSiteObjects();

    return new SiteObjectsResultSet(count, pageNumber, pageSize, items, folder);

    // return RemoteFileFacade.getSiteObjects(this, folder, objects, pageNumber,
    // pageSize);
  }

  @Transaction
  public void deleteObject(String key)
  {
    final Document document = Document.find(key);
    document.delete();

    new IndexDeleteDocumentCommand(this, key).doIt();
  }

  public RemoteFileObject download(String key)
  {
    if (!key.startsWith(this.getS3location()))
    {
      return RemoteFileFacade.download(this.getS3location() + key);
    }

    return RemoteFileFacade.download(key);
  }

  @Override
  public RemoteFileObject download(String key, String range)
  {
    if (!key.startsWith(this.getS3location()))
    {
      return RemoteFileFacade.download(this.getS3location() + key, range);
    }

    return RemoteFileFacade.download(key, range);
  }

  public RemoteFileObject downloadReport(String productName, String folder)
  {
    ProductIF product = this.getProduct(productName).orElseThrow(() -> {
      GenericException ex = new GenericException();
      ex.setUserMessage("A product with the name [" + productName + "] does not exist");
      throw ex;
    });

    return this.download(this.getS3location(product, folder) + "report.pdf");
  }

  public List<UasComponentIF> getAncestors()
  {
    return getAncestors(true);
  }

  public List<UasComponentIF> getAncestors(boolean filterByPermissions)
  {
    List<UasComponentIF> ancestors = new LinkedList<UasComponentIF>();

    List<UasComponentIF> parents = getParents(filterByPermissions);

    ancestors.addAll(parents);

    for (UasComponentIF parent : parents)
    {
      ancestors.addAll(parent.getAncestors(filterByPermissions));
    }

    return ancestors;
  }

  public List<AttributeType> attributes()
  {
    List<AttributeType> list = new LinkedList<AttributeType>();
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.NAME)));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.FOLDERNAME), true, new AdminCondition()));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.DESCRIPTION)));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.ISPRIVATE)));

    return list;
  }

  @Override
  public List<Pair<ComponentWithAttributes, List<AttributeType>>> getCompositeAttributes()
  {
    return new LinkedList<>();
  }

  public void writeFeature(JSONWriter writer) throws IOException
  {
    writer.object();

    writer.key("type");
    writer.value("Feature");

    writer.key("properties");
    writer.value(this.getProperties());

    writer.key("id");
    writer.value(this.getOid());

    writer.key("geometry");
    writer.value(new JSONObject(new GeoJsonWriter().write(this.getGeoPoint())));

    writer.endObject();
  }

  public JSONObject getProperties()
  {
    JSONObject properties = new JSONObject();
    properties.put("oid", this.getOid());
    properties.put("name", this.getName());

    return properties;
  }

  public static JSONObject features(String conditions) throws IOException
  {
    SiteQuery query = new SiteQuery(conditions);

    final List<UasComponentIF> sites = query.getResults();

    StringWriter sWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(sWriter);

    writer.object();

    writer.key("type");
    writer.value("FeatureCollection");
    writer.key("features");
    writer.array();

    for (UasComponentIF site : sites)
    {
      if (site.getGeoPoint() != null)
      {
        site.writeFeature(writer);
      }
    }

    writer.endArray();

    writer.key("totalFeatures");
    writer.value(sites.size());

    writer.key("crs");
    writer.value(new JSONObject("{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:EPSG::4326\"}}"));

    writer.endObject();

    return new JSONObject(sWriter.toString());
  }

  public static JSONArray bbox()
  {
    try
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(UasComponent.CLASS);
      MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(UasComponent.GEOPOINT);

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ST_AsText(ST_Extent(" + mdAttribute.getColumnName() + ")) AS bbox");
      sql.append(" FROM " + mdBusiness.getTableName());
      sql.append(" WHERE " + mdAttribute.getColumnName() + " IS NOT NULL");

      try (ResultSet resultSet = Database.query(sql.toString()))
      {
        if (resultSet.next())
        {
          String bbox = resultSet.getString("bbox");

          if (bbox != null)
          {
            Pattern p = Pattern.compile("POLYGON\\(\\((.*)\\)\\)");
            Matcher m = p.matcher(bbox);

            if (m.matches())
            {
              String coordinates = m.group(1);
              List<Coordinate> coords = new LinkedList<Coordinate>();

              for (String c : coordinates.split(","))
              {
                String[] xAndY = c.split(" ");
                double x = Double.valueOf(xAndY[0]);
                double y = Double.valueOf(xAndY[1]);

                coords.add(new Coordinate(x, y));
              }

              Envelope e = new Envelope(coords.get(0), coords.get(2));

              JSONArray bboxArr = new JSONArray();
              bboxArr.put(e.getMinX());
              bboxArr.put(e.getMinY());
              bboxArr.put(e.getMaxX());
              bboxArr.put(e.getMaxY());

              return bboxArr;
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      // Ignore the error and just return the default bounding box
    }

    // Extent of the continental United States
    JSONArray bboxArr = new JSONArray();
    bboxArr.put(-125.0011);
    bboxArr.put(24.9493);
    bboxArr.put(-66.9326);
    bboxArr.put(49.5904);

    return bboxArr;
  }

  /**
   * Returns tasks associated with this item.
   * 
   * @return tasks associated with this item.
   */
  public abstract List<AbstractWorkflowTask> getTasks();

  @Override
  public AbstractWorkflowTask createWorkflowTask(String uploadId, String uploadTarget)
  {
    return this.createWorkflowTask(GeoprismUser.getCurrentUser().getOid(), uploadId, uploadTarget);
  }

  @Override
  public AbstractWorkflowTask createWorkflowTask(String userOid, String uploadId, String uploadTarget)
  {
    WorkflowTask workflowTask = new WorkflowTask();
    workflowTask.setUploadId(uploadId);
    workflowTask.setUploadTarget(uploadTarget);
    workflowTask.setComponent(this.getOid());
    workflowTask.setGeoprismUserId(userOid);
    workflowTask.setTaskLabel("UAV data upload for " + this.getClass().getSimpleName().toLowerCase() + " [" + this.getName() + "]");

    return workflowTask;
  }

  @Override
  public DocumentIF putFile(String folder, String fileName, ProductIF product, RemoteFileMetadata metadata, InputStream stream)
  {
    return Util.putFile(this, folder, product, fileName, metadata, stream);
  }

  @Override
  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationFileResource file, String uploadTarget, ProductIF product)
  {
    if (this instanceof ImageryComponent)
    {
      return new FileUploadProcessor().process(task, file, (ImageryComponent) this, uploadTarget, product);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }

  public Long getNumberOfChildren()
  {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("rid", this.getRID());

    final MdEdgeDAOIF mdEdge = this.getChildMdEdge();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM (");
    statement.append("  SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "'))\n");
    statement.append("  FROM :rid \n");
    statement.append(")");

    addAccessFilter(parameters, statement);

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString(), parameters);

    return query.getSingleResult();
    //
    // StringBuilder statement = new StringBuilder();
    // statement.append("SELECT out('" + this.getChildMdEdge().getDBClassName()
    // + "').size() FROM :rid");
    //
    // GraphQuery<Integer> query = new
    // GraphQuery<Integer>(statement.toString());
    // query.setParameter("rid", this.getRID());
    //
    // return query.getSingleResult();
  }

  public List<DocumentIF> getDocuments()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_DOCUMENT, DocumentIF.class);
  }

  public List<Product> getProducts()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_PRODUCT, Product.class);
  }

  public List<RawSet> getRawSets()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_RAW_SET, RawSet.class);
  }

  public Integer getNumberOfProducts()
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT OUT('" + mdEdge.getDBClassName() + "').size()");
    statement.append(" FROM :rid ");

    final GraphQuery<Integer> query = new GraphQuery<Integer>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getSingleResult();
  }

  @Override
  public Optional<ProductIF> getPrimaryProduct()
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( ");
    statement.append(" OUT('" + mdEdge.getDBClassName() + "')[primary = :primary]");
    statement.append(")");
    statement.append(" FROM :rid ");

    final GraphQuery<ProductIF> query = new GraphQuery<ProductIF>(statement.toString());
    query.setParameter("rid", this.getRID());
    query.setParameter("primary", true);

    return Optional.ofNullable(query.getSingleResult());
  }

  @Override
  @Transaction
  public void setPrimaryProduct(ProductIF product)
  {
    this.getPrimaryProduct().ifPresent(current -> {
      ( (Product) current ).setPrimary(false);
      ( (Product) current ).apply();
    });

    ( (Product) product ).setPrimary(true);
    ( (Product) product ).apply();
  }

  @Override
  public Optional<ProductIF> getProduct(String productName)
  {
    if (!StringUtils.isBlank(productName))
    {
      MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT EXPAND( ");
      statement.append(" OUT('" + mdEdge.getDBClassName() + "')[productName = :productName]");
      statement.append(")");
      statement.append(" FROM :rid ");

      final GraphQuery<ProductIF> query = new GraphQuery<ProductIF>(statement.toString());
      query.setParameter("rid", this.getRID());
      query.setParameter("productName", productName);

      return Optional.ofNullable(query.getSingleResult());
    }

    return Optional.ofNullable(null);
  }

  @Override
  @Transaction
  public void removeProduct(String productName)
  {
    this.getProduct(productName).ifPresent(product -> {
      if (product.isPrimary())
      {
        GenericException ex = new GenericException();
        ex.setUserMessage("Cannot delete the primary product.");
        throw ex;
      }

      product.delete();
    });
  }

  public List<CollectionIF> getDerivedCollections()
  {
    String expand = String.join(".", this.buildProductExpandClause());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(" + expand + ")");
    statement.append("FROM :rid ");

    final GraphQuery<CollectionIF> query = new GraphQuery<CollectionIF>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }

  // Dumb first attempt

  // SELECT EXPAND( $c )
  // LET $a = (
  // TRAVERSE OUT('component_has_product')
  // FROM (
  // SELECT FROM (
  // SELECT *
  // FROM #34:0
  // )
  // ORDER BY name ASC
  // )
  // ),
  // $b = (
  // TRAVERSE OUT ('component_has_product')
  // FROM (
  // SELECT FROM (
  // SELECT EXPAND(OUT('project_has_mission0').OUT('mission_has_collection0'))
  // FROM #34:0
  // )
  // ORDER BY name ASC
  // )
  // ),
  // $c = UNIONALL( $a, $b )

  // Getting a little smarter...

  // TRAVERSE OUT ('component_has_product')
  // FROM (
  // SELECT EXPAND(*) FROM (
  // SELECT unionall(*, OUT('project_has_mission0'),
  // OUT('project_has_mission0').OUT('mission_has_collection0'))
  // FROM #34:0
  // )
  // ORDER BY name ASC
  // )

  @Override
  public List<ComponentProductDTO> getDerivedProducts(String sortField, String sortOrder)
  {
    sortField = sortField != null ? sortField : "name";
    sortOrder = sortOrder != null ? sortOrder : "DESC";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("rid", this.getRID());

    // String expand = "unionall(*, " + String.join(", ",
    // this.buildProductExpandClause()) + ")";

    List<String> descends = new ArrayList<String>();
    List<String> clause = this.buildProductExpandClause();
    for (int i = 0; i < clause.size(); ++i)
    {
      List<String> d2 = new ArrayList<String>();
      for (int j = 0; j < clause.size() - i; ++j)
      {
        d2.add(clause.get(j));
      }
      descends.add(String.join(".", d2));
    }
    String expand = "unionall(*, " + String.join(",", descends) + ")";

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

    statement.append("  SELECT FROM (");
    statement.append("    SELECT EXPAND(" + expand + ")");
    statement.append("    FROM :rid ");
    statement.append("  )");

    // Add the access criteria
    addAccessFilter(parameters, statement);

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
      statement.append(")\n");
    }

    statement.append(")");

    final GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString(), parameters);

    return ComponentProductDTO.process(query.getResults());
  }

  public void addComponent(UasComponentIF parent)
  {
    final MdEdgeDAOIF mdEdge = this.getParentMdEdge();

    this.addParent((UasComponent) parent, mdEdge).apply();
  }

  public List<UasComponentIF> getParents()
  {
    return getParents(true);
  }

  public List<UasComponentIF> getParents(boolean filterByPermissions)
  {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", this.getOid());
    // parameters.put("rid", this.getRID());

    MdVertexDAOIF mdVertex = (MdVertexDAOIF) this.getMdClass();

    final MdEdgeDAOIF mdEdge = this.getParentMdEdge();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM ( \n");
    statement.append("  SELECT EXPAND( IN('" + mdEdge.getDBClassName() + "'))\n");
    statement.append("  FROM ( \n");
    statement.append("    SELECT FROM " + mdVertex.getDBClassName() + " WHERE oid = :oid \n");
    statement.append("  )\n");
    statement.append(")\n");

    if (filterByPermissions)
      addAccessFilter(parameters, statement);

    final GraphQuery<UasComponentIF> query = new GraphQuery<UasComponentIF>(statement.toString(), parameters);

    return query.getResults();
  }

  public List<UasComponentIF> getChildren()
  {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("rid", this.getRID());

    final MdEdgeDAOIF mdEdge = this.getChildMdEdge();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM (");
    statement.append("  SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "'))\n");
    statement.append("  FROM :rid \n");
    statement.append(")");

    addAccessFilter(parameters, statement);

    final GraphQuery<UasComponentIF> query = new GraphQuery<UasComponentIF>(statement.toString(), parameters);

    return query.getResults();
  }

  @Override
  public List<UasComponentIF> getChildrenWithConditions(String conditions)
  {
    return new ChildrenQuery(this, conditions).getResults();
  }

  public UasComponentIF getChild(String name)
  {
    final MdEdgeDAOIF mdEdge = this.getChildMdEdge();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "')[name=:name])\n");
    statement.append("FROM :rid \n");

    final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(statement.toString());
    query.setParameter("name", name);
    query.setParameter("rid", this.getRID());

    return query.getSingleResult();
  }

  public boolean isDuplicateFolderName(UasComponentIF parent, String folderName)
  {
    if (parent != null)
    {
      final MdEdgeDAOIF mdEdge = this.getParentMdEdge();

      UasComponent component = (UasComponent) parent;

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "')");
      statement.append(" [ folderName = :folderName AND oid != :oid ])\n");
      statement.append("FROM :rid \n");
      // statement.append("WHERE out.folderName = :folderName" + "\n");
      // statement.append("AND out.oid != :oid" + "\n");

      final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(statement.toString());
      query.setParameter("folderName", folderName);
      query.setParameter("rid", component.getRID());
      query.setParameter("oid", this.getOid());

      final List<UasComponent> list = query.getResults();

      if (list.size() > 0)
      {
        return true;
      }

      return false;
    }

    final MdGraphClassDAOIF mdGraphClass = (MdGraphClassDAOIF) this.getMdClass();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdGraphClass.getDBClassName() + " \n");
    statement.append("WHERE folderName = :folderName" + "\n");
    statement.append("AND oid != :oid" + "\n");

    final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(statement.toString());
    query.setParameter("folderName", folderName);
    query.setParameter("oid", this.getOid());

    List<UasComponent> list = query.getResults();

    if (list.size() > 0)
    {
      return true;
    }

    return false;
  }

  @Override
  public DocumentIF createDocumentIfNotExist(String key, String name, DocumentIF.Metadata metadata)
  {
    return Document.createIfNotExist(this, key, name, metadata);
  }

  @Override
  public ProductIF createProductIfNotExist(String productName)
  {
    return Product.createIfNotExist(this, productName);
  }

  @Override
  public RawSet createRawSetIfNotExist(CreateRawSetView view)
  {
    return RawSet.createIfNotExist(this, view);
  }

  @Override
  public List<ComponentRawSet> getDerivedRawSets(String sortField, String sortOrder)
  {
    sortField = sortField != null ? sortField : "name";
    sortOrder = sortOrder != null ? sortOrder : "DESC";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("rid", this.getRID());

    // String expand = "unionall(*, " + String.join(", ",
    // this.buildProductExpandClause()) + ")";

    List<String> descends = new ArrayList<String>();
    List<String> clause = this.buildProductExpandClause();
    for (int i = 0; i < clause.size(); ++i)
    {
      List<String> d2 = new ArrayList<String>();
      for (int j = 0; j < clause.size() - i; ++j)
      {
        d2.add(clause.get(j));
      }
      descends.add(String.join(".", d2));
    }
    String expand = "unionall(*, " + String.join(",", descends) + ")";

    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_RAW_SET);

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

    statement.append("  SELECT FROM (");
    statement.append("    SELECT EXPAND(" + expand + ")");
    statement.append("    FROM :rid ");
    statement.append("  )");

    // Add the access criteria
    addAccessFilter(parameters, statement);

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
      statement.append(")\n");
    }

    statement.append(")");

    final GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString(), parameters);

    return ComponentRawSet.process(query.getResults());
  }

  public static <T extends UasComponent> Optional<T> getWithAccessControl(String oid)
  {
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", oid);

    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM ( \n");
    statement.append("  SELECT FROM " + mdVertex.getDBClassName() + " WHERE oid = :oid \n");
    statement.append(")\n");

    addAccessFilter(parameters, statement);

    final GraphQuery<T> query = new GraphQuery<T>(statement.toString(), parameters);

    return Optional.ofNullable(query.getSingleResult());
  }

  public static void addAccessFilter(HashMap<String, Object> parameters, StringBuilder statement)
  {
    // Add the filter for permissions
    MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);
    MdEdgeDAOIF accessEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.USER_HAS_ACCESS);

    MdAttributeDAOIF privateAttribute = mdClass.definesAttribute(UasComponent.ISPRIVATE);
    MdAttributeDAOIF ownerAttribute = mdClass.definesAttribute(UasComponent.OWNER);

    SessionIF session = Session.getCurrentSession();

    if (session != null && session.userHasRole(RoleConstants.ADMIN))
      return;

    statement.append(" WHERE " + privateAttribute.getColumnName() + " = :isPrivate");
    statement.append(" OR " + privateAttribute.getColumnName() + " IS NULL");

    if (session != null)
    {
      statement.append(" OR " + ownerAttribute.getColumnName() + " = :owner");
      statement.append(" OR in('" + accessEdge.getDBClassName() + "')[user = :owner].size() > 0");
    }

    parameters.put("isPrivate", false);

    if (session != null)
    {
      parameters.put("owner", session.getUser().getOid());
    }
  }

  public String buildRawKey()
  {
    return this.getS3location() + ImageryComponent.RAW + "/";
  }

  public String buildVideoKey()
  {
    return this.getS3location() + ImageryComponent.VIDEO + "/";
  }

  public String buildPointCloudKey()
  {
    return this.getS3location() + ImageryComponent.PTCLOUD + "/";
  }

  public String buildDemKey()
  {
    return this.getS3location() + ImageryComponent.DEM + "/";
  }

  public String buildOrthoKey()
  {
    return this.getS3location() + ImageryComponent.ORTHO + "/";
  }

  public UasComponentIF getUasComponent()
  {
    return this;
  }

}