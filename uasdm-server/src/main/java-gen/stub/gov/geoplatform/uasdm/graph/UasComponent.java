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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEdge;

import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.CollectionStatusQuery;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.bus.DuplicateComponentException;
import gov.geoplatform.uasdm.bus.InvalidUasComponentNameException;
import gov.geoplatform.uasdm.bus.UasComponentDeleteException;
import gov.geoplatform.uasdm.command.IndexDeleteDocumentCommand;
import gov.geoplatform.uasdm.command.IndexDeleteDocumentsCommand;
import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.model.CompositeDeleteException;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.AdminCondition;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import net.geoprism.GeoprismUser;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.rbac.RoleConstants;

public abstract class UasComponent extends UasComponentBase implements UasComponentIF
{
  private static class Artifact
  {
    private List<SiteObject> objects;

    private boolean          report;

    private String           folder;

    private String[]         extensions;

    public Artifact(String folder, String... extensions)
    {
      this.folder = folder;
      this.extensions = extensions;
      this.report = false;
      this.objects = new LinkedList<SiteObject>();
    }

    public void process(SiteObject object)
    {
      if (object.getKey().contains("/" + this.folder + "/"))
      {
        for (String extension : extensions)
        {
          if (object.getKey().toUpperCase().endsWith(extension))
          {
            this.objects.add(object);

            break;
          }
        }

        if (object.getKey().toUpperCase().endsWith("REPORT.PDF"))
        {
          this.report = true;
        }
      }

    }

    public JSONObject toJSON()
    {
      JSONArray items = new JSONArray();

      this.objects.forEach(object -> items.put(object.toJSON()));

      JSONObject object = new JSONObject();
      object.put("report", this.report);
      object.put("items", items);
      return object;
    }
  }

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

  /**
   * @return The name of the solr field for the components id.
   */
  public abstract String getSolrIdField();

  /**
   * @return The name of the solr field for the components name.
   */
  public abstract String getSolrNameField();

  protected abstract String buildProductExpandClause();

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
      IndexService.createDocument(this.getAncestors(), this);
    }
  }

  @Override
  public void apply()
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

    super.apply();

    if (!isNew)
    {
      if (isNameModified)
      {
        IndexService.updateName(this);
      }

      if (needsUpdate)
      {
        IndexService.updateComponent(this);
      }

      CollectionReport.update(this);
    }
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
    return this.isModified(UasComponent.DESCRIPTION);
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
  public JSONObject getArtifacts()
  {
    List<SiteObject> objects = new ArtifactQuery(this).getSiteObjects();

    Artifact[] artifacts = new Artifact[] { 
        new Artifact(ImageryComponent.DEM, ".TIF"), 
        new Artifact(ImageryComponent.ORTHO, ".TIF"), 
        new Artifact(ImageryComponent.PTCLOUD, ".LAZ", ".LAS")
    };

    for (SiteObject object : objects)
    {
      for (Artifact artifact : artifacts)
      {
        artifact.process(object);
      }
    }

    JSONObject response = new JSONObject();

    for (Artifact artifact : artifacts)
    {
      response.put(artifact.folder, artifact.toJSON());
    }

    return response;
  }

  @Override
  @Transaction
  public void removeArtifacts(String folder)
  {
    if (folder.equalsIgnoreCase(ImageryComponent.RAW) || folder.equalsIgnoreCase(ImageryComponent.VIDEO))
    {
      return;
    }

    List<Document> documents = new SiteObjectDocumentQuery(this, folder).getDocuments();

    if (folder.equals(ImageryComponent.PTCLOUD))
    {
      documents.addAll(new SiteObjectDocumentQuery(this, ODMZipPostProcessor.POTREE).getDocuments());
    }
    else if (folder.equals(ImageryComponent.DEM))
    {
      documents.addAll(new SiteObjectDocumentQuery(this, ODMZipPostProcessor.DEM_GDAL).getDocuments());
    }

    for (Document document : documents)
    {
      document.delete(true);
    }

    if (new ArtifactQuery(this).getDocuments().size() == 0)
    {
      this.getProducts().forEach(product -> {
        product.delete();
      });
    }
    else
    {
      // Re-index the products
      this.getProducts().forEach(product -> {
        IndexService.createStacItems(product);
      });
    }
  }

  public SiteObjectsResultSet getSiteObjects(String folder, Long pageNumber, Long pageSize)
  {
    return new SiteObjectsResultSet(0L, pageNumber, pageSize, new LinkedList<SiteObject>(), folder);
  }

  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    SiteObjectDocumentQueryIF query = folder.equals("artifacts") ? new ArtifactQuery(this) : new SiteObjectDocumentQuery(this, folder);

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
  public RemoteFileObject download(String key, List<Range> ranges)
  {
    return RemoteFileFacade.download(key, ranges);
  }

  public int getItemCount(String key)
  {
    return RemoteFileFacade.getItemCount(key);
  }

  public List<UasComponentIF> getAncestors()
  {
    List<UasComponentIF> ancestors = new LinkedList<UasComponentIF>();

    List<UasComponentIF> parents = getParents();

    ancestors.addAll(parents);

    for (UasComponentIF parent : parents)
    {
      ancestors.addAll(parent.getAncestors());
    }

    return ancestors;
  }

  public List<AttributeType> attributes()
  {
    List<AttributeType> list = new LinkedList<AttributeType>();
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.NAME)));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.FOLDERNAME), true, new AdminCondition()));
    list.add(AttributeType.create(this.getMdAttributeDAO(UasComponent.DESCRIPTION)));

    return list;
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
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);
    final TreeMap<String, Object> parameters = new TreeMap<String, Object>();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    if (conditions != null && conditions.length() > 0)
    {
      JSONObject cObject = new JSONObject(conditions);
      
      boolean isFirst = true;

      /*
       * select from ( traverse out('g_0__operational', 'ha_0__graph_271118')
       * from #474:1 ) where @class = 'site0'
       */
      if (cObject.has("hierarchy") && !cObject.isNull("hierarchy"))
      {
        JSONObject hierarchy = cObject.getJSONObject("hierarchy");
        String oid = hierarchy.getString(LabeledPropertyGraphSynchronization.OID);
        String uid = hierarchy.getString("uid");

        LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(oid);
        LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
        HierarchyTypeSnapshot hierarchyType = version.getHierarchies().get(0);

        SynchronizationEdge synchronizationEdge = SynchronizationEdge.get(version);
        MdEdge siteEdge = synchronizationEdge.getGraphEdge();

        VertexObject object = version.getObject(uid);

        statement = new StringBuilder();
        statement.append("SELECT FROM (");
        statement.append(" TRAVERSE OUT('" + hierarchyType.getGraphMdEdge().getDbClassName() + "', '" + siteEdge.getDbClassName() + "') FROM :rid");
        statement.append(") WHERE @class = 'site0'");

        parameters.put("rid", object.getRID());
        
        isFirst = false;
      }
      
      JSONArray array = cObject.getJSONArray("array");

      for (int i = 0; i < array.length(); i++)
      {
        JSONObject condition = array.getJSONObject(i);

        String field = condition.getString("field");

        if (field.equalsIgnoreCase("bounds"))
        {
          // {"_sw":{"lng":-90.55128715174949,"lat":20.209904454730363},"_ne":{"lng":-32.30032930862288,"lat":42.133128793454745}}
          JSONObject object = condition.getJSONObject("value");

          JSONObject sw = object.getJSONObject("_sw");
          JSONObject ne = object.getJSONObject("_ne");

          double x1 = sw.getDouble("lng");
          double x2 = ne.getDouble("lng");
          double y1 = sw.getDouble("lat");
          double y2 = ne.getDouble("lat");

          Envelope envelope = new Envelope(x1, x2, y1, y2);
          WKTWriter wktwriter = new WKTWriter();
          GeometryFactory factory = new GeometryFactory();
          Geometry geometry = factory.toGeometry(envelope);

          statement.append(isFirst ? " WHERE" : " AND");
          statement.append(" ST_WITHIN(geoPoint, ST_GeomFromText(:wkt)) = true");

          parameters.put("wkt", wktwriter.write(geometry));
        }
        else if (field.equalsIgnoreCase(Site.BUREAU))
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(field.equalsIgnoreCase(Site.BUREAU) ? Site.CLASS : UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            String value = condition.getString("value");

            statement.append(isFirst ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName());

            parameters.put(mdAttribute.getColumnName(), value);
          }

        }
        else
        {
          MdVertexDAOIF mdClass = MdVertexDAO.getMdVertexDAO(UasComponent.CLASS);

          MdAttributeDAOIF mdAttribute = mdClass.definesAttribute(field);

          if (mdAttribute != null)
          {
            String value = condition.getString("value");

            statement.append(isFirst ? " WHERE" : " AND");
            statement.append(" " + mdAttribute.getColumnName() + " = :" + mdAttribute.getColumnName());

            parameters.put(mdAttribute.getColumnName(), value);
          }
        }
        
        isFirst = false;
      }
    }

    final GraphQuery<Site> query = new GraphQuery<Site>(statement.toString(), parameters);
    final List<Site> sites = query.getResults();

    StringWriter sWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(sWriter);

    writer.object();

    writer.key("type");
    writer.value("FeatureCollection");
    writer.key("features");
    writer.array();

    for (Site site : sites)
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
    throw new UnsupportedOperationException();
  }

  @Override
  public DocumentIF putFile(String folder, String fileName, RemoteFileMetadata metadata, InputStream stream)
  {
    return Util.putFile(this, folder, fileName, metadata, stream);
  }

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    throw new UnsupportedOperationException();
  }

  public Integer getNumberOfChildren()
  {
    return this.getChildren().size();
  }

  public List<DocumentIF> getDocuments()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_DOCUMENT, DocumentIF.class);
  }

  public List<Product> getProducts()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_PRODUCT, Product.class);
  }

  @Override
  public List<ProductIF> getDerivedProducts(String sortField, String sortOrder)
  {    
    sortField = sortField != null ? sortField : "name";
    sortOrder = sortOrder != null ? sortOrder : "DESC";

    String expand = this.buildProductExpandClause();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( " + Collection.expandClause() + ") FROM (");
    statement.append("  SELECT FROM (");
    statement.append("    SELECT EXPAND(" + expand + ")");
    statement.append("    FROM :rid ");
    statement.append("  )");

    if (sortField.equals("name"))
    {
      statement.append("  ORDER BY " + sortField + " " + sortOrder);
    }
    else if (sortField.equals("sensor"))
    {
      statement.append("  ORDER BY collectionSensor.name " + sortOrder);
    }
    else if (sortField.equals("serialNumber"))
    {
      statement.append("  ORDER BY uav.serialNumber " + sortOrder);
    }
    else if (sortField.equals("faaNumber"))
    {
      statement.append("  ORDER BY uav.faaNumber " + sortOrder);
    }

    statement.append(")");

    if (sortField.equals(Product.LASTUPDATEDATE))
    {
      statement.append("  ORDER BY " + sortField + " " + sortOrder);
    }

    final GraphQuery<ProductIF> query = new GraphQuery<ProductIF>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getResults();
  }

  public void addComponent(UasComponentIF parent)
  {
    final MdEdgeDAOIF mdEdge = this.getParentMdEdge();

    this.addParent((UasComponent) parent, mdEdge).apply();
  }

  public List<UasComponentIF> getParents()
  {
    return this.getParents(this.getParentMdEdge(), UasComponentIF.class);
  }

  public List<UasComponentIF> getChildren()
  {
    return this.getChildren(this.getChildMdEdge(), UasComponentIF.class);
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
  public DocumentIF createDocumentIfNotExist(String key, String name, String description, String tool)
  {
    return Document.createIfNotExist(this, key, name, description, tool);
  }

  @Override
  public ProductIF createProductIfNotExist()
  {
    return Product.createIfNotExist(this);
  }

}
