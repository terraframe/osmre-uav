package gov.geoplatform.uasdm.graph;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONArray;
import org.locationtech.jts.geom.Coordinate;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.metadata.MdEdge;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.bus.InvalidUasComponentNameException;
import gov.geoplatform.uasdm.model.ComponentRawSet;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.SystemProcessExecutor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.business.IDMHierarchyTypeSnapshotBusinessService;
import gov.geoplatform.uasdm.view.CollectionCriteria;
import gov.geoplatform.uasdm.view.CreateRawSetView;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.spring.core.ApplicationContextHolder;

public class RawSet extends RawSetBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1976921284;

  public RawSet()
  {
    super();
  }

  public boolean isPublished()
  {
    return this.getPublished() != null && this.getPublished();
  }

  public boolean isLocked()
  {
    return this.getLocked() != null && this.getLocked();
  }

  public UasComponent getComponent()
  {
    final List<UasComponent> parents = this.getParents(EdgeType.COMPONENT_HAS_RAW_SET, UasComponent.class);

    return parents.get(0);
  }

  public List<DocumentIF> getDocuments()
  {
    return this.getChildren(EdgeType.RAW_SET_HAS_DOCUMENT, DocumentIF.class);
  }

  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();

    if (!UasComponentIF.isValidName(this.getName()))
    {
      MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(RawSet.CLASS);
      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(RawSet.NAME);

      InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The product name [" + this.getName() + "] has an invalid character. Disallowed characters are " + UasComponentIF.DISALLOWED_FILENAME_REGEX);
      ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
      throw ex;
    }

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_RAW_SET).apply();
    }
  }

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

          // Copy the thumbnail over to the public bucket
          String ext = FilenameUtils.getExtension(document.getName());

          if (ext.toUpperCase().equals("PNG"))
          {
            String rootPath = FilenameUtils.getPath(document.getS3location());
            String baseName = FilenameUtils.getBaseName(document.getName());

            RemoteFileFacade.copyObject(rootPath + "thumbnails/" + baseName + ".png", AppProperties.getBucketName(), rootPath + "thumbnails/" + baseName + ".png", AppProperties.getPublicBucketName());
          }

        }
        else
        {
          // Remove from public S3 bucket
          RemoteFileFacade.deleteObject(document.getS3location(), AppProperties.getPublicBucketName());

          String ext = FilenameUtils.getExtension(document.getName());

          if (ext.toUpperCase().equals("PNG"))
          {
            String rootPath = FilenameUtils.getPath(document.getS3location());
            String baseName = FilenameUtils.getBaseName(document.getName());

            RemoteFileFacade.deleteObject(rootPath + "thumbnails/" + baseName + ".png", AppProperties.getPublicBucketName());
          }
        }
      }
    }
    finally
    {
    }
  }

  /**
   * WARNING This method does not create an associated metadata with the created
   * product. All products must have metadata associated with them. You probably
   * want to use component.createProductIfNotExist instead.
   * 
   * @param uasComponent
   * @param productName
   * @return
   */
  public static RawSet createIfNotExist(UasComponentIF uasComponent, CreateRawSetView view)
  {
    // Get the documents
    List<Document> documents = view.getFiles().stream().map(id -> Document.get(id)).collect(Collectors.toList());

    // Ensure that each document has coordinates
    documents.stream() //
        .filter(document -> document.getLongitude() == null && document.getLatitude() == null) //
        .forEach(document -> updateCoordinates(uasComponent, document));

    // Calculate bounding box
    final ReferencedEnvelope envelope = new ReferencedEnvelope();

    documents.stream() //
        .map(document -> new Coordinate(document.getLongitude(), document.getLatitude())) //
        .filter(coord -> coord != null && !Double.isNaN(coord.x) && !Double.isNaN(coord.y)) //
        .forEach(coord -> envelope.expandToInclude(coord.x, coord.y));

    JSONArray array = new JSONArray();
    array.put(envelope.getMinX());
    array.put(envelope.getMinY());
    array.put(envelope.getMaxX());
    array.put(envelope.getMaxY());

    // Get or create the raw set
    RawSet set = find(uasComponent, view.getName()).orElseGet(() -> {
      RawSet newInstance = new RawSet();
      newInstance.setName(view.getName());
      newInstance.setPublished(false);

      return newInstance;
    });

    // Update values
    set.setBoundingBox(array.toString());
    set.setLastUpdateDate(new Date());
    set.apply(uasComponent);

    return set;
  }

  public static Optional<RawSet> find(UasComponentIF component, String name)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_RAW_SET);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "')[name = :name])\n");
    statement.append("FROM :rid \n");

    final GraphQuery<RawSet> query = new GraphQuery<RawSet>(statement.toString());
    query.setParameter("rid", ( (UasComponent) component ).getRID());
    query.setParameter("name", name);

    return Optional.ofNullable(query.getSingleResult());
  }

  public static List<ComponentRawSet> getAll(CollectionCriteria criteria)
  {
    LabeledPropertyGraphTypeVersionBusinessServiceIF service = ApplicationContextHolder.getBean(LabeledPropertyGraphTypeVersionBusinessServiceIF.class);
    IDMHierarchyTypeSnapshotBusinessService hService = ApplicationContextHolder.getBean(IDMHierarchyTypeSnapshotBusinessService.class);

    LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(criteria.getHierarchy());
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();
    HierarchyTypeSnapshot hierarchyType = hService.get(version).get(0);

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

    return ComponentRawSet.process(query.getResults());
  }

  private static void updateCoordinates(UasComponentIF uasComponent, Document document)
  {
    // GPS Longitude : 111.124293944 W
    try (RemoteFileObject remoteFile = uasComponent.download(document.getKey()))
    {
      File tempFile = File.createTempFile(remoteFile.getName(), remoteFile.getNameExtension());

      try
      {
        try (InputStream istream = remoteFile.getObjectContent())
        {
          FileUtils.copyToFile(istream, tempFile);

          SystemProcessExecutor executor = new SystemProcessExecutor();
          executor.execute(new String[] { "exiftool", "-GPSPosition", "-c", "%.9f", tempFile.getAbsolutePath() });

          String output = executor.getStdOut();

          if (output.contains("GPS") && output.contains(":"))
          {
            String[] tokens = output.split(":");

            String token = tokens[1].trim();

            String[] coordinates = token.split(",");

            String latitude = coordinates[0].trim();
            String longitutde = coordinates[1].trim();

            if (latitude.endsWith("S"))
            {
              latitude = "-" + latitude;
            }

            if (longitutde.endsWith("W"))
            {
              longitutde = "-" + longitutde;
            }

            latitude = latitude.replaceAll("S", "").replaceAll("N", "");
            longitutde = longitutde.replaceAll("E", "").replaceAll("W", "");

            document.setLongitude(Double.valueOf(longitutde));
            document.setLatitude(Double.valueOf(latitude));
            document.apply();
          }
        }
      }
      finally
      {
        FileUtils.deleteQuietly(tempFile);
      }
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
