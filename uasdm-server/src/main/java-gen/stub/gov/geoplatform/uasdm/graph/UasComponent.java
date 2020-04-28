package gov.geoplatform.uasdm.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.metadata.MdBusiness;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.DuplicateComponentException;
import gov.geoplatform.uasdm.bus.InvalidUasComponentNameException;
import gov.geoplatform.uasdm.bus.UasComponentDeleteException;
import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.command.SolrDeleteDocumentsCommand;
import gov.geoplatform.uasdm.model.CompositeDeleteException;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.SolrService;
import gov.geoplatform.uasdm.view.AdminCondition;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import net.geoprism.DefaultConfiguration;
import net.geoprism.GeoprismUser;
import net.geoprism.JSONStringImpl;

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
      this.setFolderName(UUID.randomUUID().toString().replaceAll("-", ""));
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
      SolrService.createDocument(this.getAncestors(), this);
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
        SolrService.updateName(this);
      }

      if (needsUpdate)
      {
        SolrService.updateComponent(this);
      }
    }
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

    if (session != null && ! ( session.userHasRole(DefaultConfiguration.ADMIN) || this.getOwnerOid().equals(session.getUser().getOid()) ))
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

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.getS3location(), null);
    }

    final SolrDeleteDocumentsCommand command = new SolrDeleteDocumentsCommand(this.getSolrIdField(), this.getOid());
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
    new RemoteFileDeleteCommand(key).doIt();
  }

  public SiteObjectsResultSet getSiteObjects(String folder, Integer pageNumber, Integer pageSize)
  {
    return new SiteObjectsResultSet(0, pageNumber, pageSize, new LinkedList<SiteObject>(), folder);
  }

  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Integer pageNumber, Integer pageSize)
  {
    return RemoteFileFacade.getSiteObjects(this, folder, objects, pageNumber, pageSize);
  }

  @Transaction
  public void deleteObject(String key)
  {
    final Document document = Document.find(key);
    document.delete();

//    SolrService.deleteDocument(this, key);
  }

  public RemoteFileObject download(String key)
  {
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
    GeometryJSON gjson = new GeometryJSON();

    StringWriter geomWriter = new StringWriter();

    gjson.write(this.getGeoPoint(), geomWriter);

    writer.object();

    writer.key("type");
    writer.value("Feature");

    writer.key("properties");
    writer.value(this.getProperties());

    writer.key("id");
    writer.value(this.getOid());

    writer.key("geometry");
    writer.value(new JSONStringImpl(geomWriter.toString()));

    writer.endObject();
  }

  public JSONObject getProperties()
  {
    JSONObject properties = new JSONObject();
    properties.put("oid", this.getOid());
    properties.put("name", this.getName());

    return properties;
  }

  public static JSONObject features() throws IOException
  {
    StringWriter sWriter = new StringWriter();
    JSONWriter writer = new JSONWriter(sWriter);

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Site.CLASS);

    final GraphQuery<Site> query = new GraphQuery<Site>("SELECT FROM " + mdVertex.getDBClassName());
    final List<Site> sites = query.getResults();

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
  public AbstractWorkflowTask createWorkflowTask(String uploadId)
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

  public List<String> uploadZipArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget)
  {
    throw new UnsupportedOperationException();
  }

  public Integer getNumberOfChildren()
  {
    return this.getChildren().size();
  }

  public String getStoreName(String key)
  {
    String baseName = FilenameUtils.getBaseName(key);

    return this.getOid() + "-" + baseName;
  }

  public List<DocumentIF> getDocuments()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_DOCUMENT, DocumentIF.class);
  }

  public List<Product> getProducts()
  {
    return this.getChildren(EdgeType.COMPONENT_HAS_PRODUCT, Product.class);
  }

  public List<ProductIF> getDerivedProducts()
  {
    String expand = this.buildProductExpandClause();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(" + expand + ")");
    statement.append(" FROM :rid");

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
//      final MdVertexDAOIF mdVertex = mdEdge.getParentMdVertex();

      UasComponent component = (UasComponent) parent;

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "'))\n");
      statement.append("FROM :rid \n");
      statement.append("WHERE out.folderName = :folderName" + "\n");

      final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(statement.toString());
      query.setParameter("folderName", folderName);
      query.setParameter("rid", component.getRID());

      final List<UasComponent> list = query.getResults();

      if (list.size() > 0)
      {
        return true;
      }

      return false;
    }

    return false;
  }

  @Override
  public DocumentIF createDocumentIfNotExist(String key, String name)
  {
    return Document.createIfNotExist(this, key, name);
  }

  @Override
  public ProductIF createProductIfNotExist()
  {
    return Product.createIfNotExist(this);
  }

}
