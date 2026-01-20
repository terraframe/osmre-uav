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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.Pair;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.CannotDeleteProcessingCollection;
import gov.geoplatform.uasdm.CollectionStatus;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.bus.CollectionReportQuery;
import gov.geoplatform.uasdm.bus.CollectionUploadEvent;
import gov.geoplatform.uasdm.bus.CollectionUploadEventQuery;
import gov.geoplatform.uasdm.bus.MissingMetadataMessage;
import gov.geoplatform.uasdm.bus.MissingUploadMessage;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.cog.CogPreviewParams;
import gov.geoplatform.uasdm.cog.TiTillerProxy;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.graph.Sensor.CollectionFormat;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentWithAttributes;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.BasicFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.remote.s3.InputStreamObjectWrapper;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import net.geoprism.GeoprismUser;

public class Collection extends CollectionBase implements ImageryComponent, CollectionIF
{
  public static final long    serialVersionUID = 166854005;

  private static final String PARENT_EDGE      = "gov.geoplatform.uasdm.graph.MissionHasCollection";

  public static final String  SENSOR           = "sensor";

  public static final String  POINT_OF_CONTACT = "pointOfContact";

  public static final String  NAME             = "name";

  public static final String  EMAIL            = "email";

  public static final String  FORMAT           = "format";

  final Logger                log              = LoggerFactory.getLogger(Collection.class);

  public Collection()
  {
    super();
  }

  @Override
  public void regenerateMetadata()
  {
    new GenerateMetadataCommand(this, null, ( (CollectionIF) this ).getMetadata().orElseThrow()).doIt();
  }

  @Override
  public void apply(boolean regenerateMetadata)
  {
    boolean hasPIIConcern = this.isModified(UasComponent.HASPIICONCERN) && this.hasPIIConcern();

    if (hasPIIConcern)
    {
      this.setIsPrivate(true);
    }

    if (this.isNew())
    {
      this.setMetadataUploaded(false);
    }
    else if (this.isModified(UasComponent.ISPRIVATE) && this.isPrivate())
    {
      boolean isPublished = this.getProducts().stream().anyMatch(p -> p.isPublished());

      if (isPublished)
      {
        GenericException ex = new GenericException();
        ex.setUserMessage("A collection can not be made private if it has published products");
        throw ex;
      }
    }

    if (this.getCollectionEndDate() == null)
    {
      this.setCollectionEndDate(this.getCollectionDate());
    }

    super.apply(regenerateMetadata);

    if (this.hasPIIConcern())
    {
      this.getProducts().stream().filter(p -> !p.isLocked()).forEach(p -> {
        p.toggleLock();
      });
    }

  }

  @Override
  public void apply()
  {
    this.apply(true);
  }

  @Override
  public void appLock()
  {
    // Balk
  }

  /**
   * @return All raw "input" documents which may be used for processing, which
   *         may in practice be imagery, pointclouds, etc.
   */
  public List<DocumentIF> getRaw()
  {
    return getDocuments().stream()//
        .filter(doc -> {
          return doc.getS3location().contains("/raw/");
        }).filter(doc -> {
          // Exclude the metadata xml file
          return !doc.getS3location().endsWith(".xml");
        }).collect(Collectors.toList());
  }

  public CollectionFormat getFormat()
  {
    if (StringUtils.isBlank(this.getSCollectionFormat()))
      return null;

    return CollectionFormat.valueOf(this.getSCollectionFormat());
  }

  public void setFormat(CollectionFormat format)
  {
    this.setSCollectionFormat(format == null ? null : format.name());
  }

  public void setFormat(String format)
  {
    if (format != null)
      CollectionFormat.valueOf(format); // validate

    this.setSCollectionFormat(format);
  }

  @Override
  public UasComponent createDefaultChild()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<CollectionMetadata> getMetadata()
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COLLECTION_HAS_METADATA);

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

  @Override
  public List<Pair<ComponentWithAttributes, List<AttributeType>>> getCompositeAttributes()
  {
    List<Pair<ComponentWithAttributes, List<AttributeType>>> list = super.getCompositeAttributes();

    this.getMetadata().ifPresent(metadata -> {
      List<AttributeType> attributes = new LinkedList<>();
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.EXIFINCLUDED)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.NORTHBOUND)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.SOUTHBOUND)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.EASTBOUND)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.WESTBOUND)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.ACQUISITIONDATESTART)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.ACQUISITIONDATEEND)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.FLYINGHEIGHT)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.NUMBEROFFLIGHTS)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.PERCENTENDLAP)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.PERCENTSIDELAP)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.AREACOVERED)));
      attributes.add(AttributeType.create(metadata.getMdAttributeDAO(Collection.WEATHERCONDITIONS)));

      list.add(new Pair<ComponentWithAttributes, List<AttributeType>>(metadata, attributes));
    });

    return list;
  }

  @Override
  public String getSolrIdField()
  {
    return "collectionId";
  }

  @Override
  public String getSolrNameField()
  {
    return "collectionName";
  }

  @Override
  protected MdEdgeDAOIF getParentMdEdge()
  {
    return MdEdgeDAO.getMdEdgeDAO(PARENT_EDGE);
  }

  @Override
  protected MdEdgeDAOIF getChildMdEdge()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<UasComponentIF> getChildren()
  {
    return new LinkedList<UasComponentIF>();
  }

  @Override
  protected List<String> buildProductExpandClause()
  {
    return Collection.expandClause();
  }

  @Override
  public List<CollectionIF> getDerivedCollections()
  {
    return Arrays.asList(this);
  }

  public JSONObject toMetadataMessage()
  {
    final List<UasComponentIF> ancestors = this.getAncestors();
    Collections.reverse(ancestors);

    final JSONArray parents = new JSONArray();

    for (UasComponentIF ancestor : ancestors)
    {
      parents.put(ancestor.getName());
    }

    JSONObject object = new JSONObject();
    object.put("collectionName", this.getName());
    object.put("collectionId", this.getOid());
    object.put("ancestors", parents);
    object.put("message", "Metadata missing for collection [" + this.getName() + "]");

    if (this.getImageHeight() != null)
    {
      object.put("imageHeight", this.getImageHeight());
    }
    if (this.getImageWidth() != null)
    {
      object.put("imageWidth", this.getImageWidth());
    }

    return object;
  }

  @Override
  public RemoteFileObject download(String key)
  {
    CollectionReportFacade.updateDownloadCount(this).doIt();

    return super.download(key);
  }

  public RemoteFileObject download(String key, boolean incrementDownloadCount)
  {
    if (incrementDownloadCount)
    {
      CollectionReportFacade.updateDownloadCount(this).doIt();
    }

    // TODO : Due to a bug in ODM png generation with multispectral we're
    // hacking around it here
    // @see https://github.com/OpenDroneMap/ODM/issues/1658
    if (this.isMultiSpectral() && key.matches(Product.THUMBNAIL_ORTHO_PNG_REGEX))
    {
      Product product = this.getProducts().get(0); // TODO : This might be the
                                                   // wrong product...
      InputStream is = new TiTillerProxy().getCogPreview(product, product.getMappableOrtho().get(), new CogPreviewParams(250, 250));

      try
      {
        byte[] bytes = IOUtils.toByteArray(is);

        return new InputStreamObjectWrapper(key, new ByteArrayInputStream(bytes), new BasicFileMetadata(ContentType.IMAGE_PNG.toString(), bytes.length));
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    return super.download(key);
  }

  @Override
  public RemoteFileObject download(String key, String range)
  {
    List<Range> ranges = Range.decodeRange(range);

    long isStart = ranges.stream().filter(r -> r.getStart().equals(0L)).count();

    if (isStart > 0)
    {
      CollectionReportFacade.updateDownloadCount(this).doIt();
    }

    return super.download(key, range);
  }

  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  @Override
  public void applyWithParent(UasComponentIF parent)
  {
    super.applyWithParent(parent);

    if (this.isNew())
    {
      this.createS3Folder(this.buildRawKey());

      this.createS3Folder(this.buildPointCloudKey());

      this.createS3Folder(this.buildDemKey());

      this.createS3Folder(this.buildOrthoKey());

      CollectionReport.create(this);
    }
  }

  private boolean calculateHasAllZip()
  {
    Optional<ODMRun> odmRun = ODMRun.getByComponentOrdered(this.getOid()).stream().findFirst();

    if (odmRun.isPresent())
    {
      return odmRun.get().getODMRunOutputChildDocuments().stream().filter(doc -> doc.getS3location().matches(".*\\/odm_all\\/all.*\\.zip")).findAny().isPresent();
    }
    else
    {
      return getAllZip() != null;
    }
  }

  @Override
  public Boolean getHasAllZip()
  {
    Boolean hasAllZip = super.getHasAllZip();

    if (hasAllZip == null)
    {
      hasAllZip = calculateHasAllZip();

      this.appLock();
      this.setHasAllZip(hasAllZip);
      this.apply(false);
    }

    return hasAllZip;
  }

  /**
   * TODO: This is an order of magnitude slower than querying the database
   * because it does an s3 list objects request. But, since we have a corruption
   * in our database with the all zip document relationship with the product it
   * is sometimes necessary.
   */
  public SiteObject getAllZip()
  {
    List<SiteObject> items = RemoteFileFacade.getSiteObjects(this, Product.ODM_ALL_DIR, new LinkedList<SiteObject>(), null, null).getObjects();

    SiteObject last = null;

    String path = this.getS3location().replaceAll("\\/", "\\\\/");

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
  }

  public String getStatus()
  {
    List<? extends WorkflowTask> tasks = WorkflowTask.getTasksForComponent(this.getOid());

    Map<String, LinkedList<WorkflowTask>> taskGroups = CollectionStatus.createTaskGroups(tasks);

    String status = CollectionStatus.mergeTaskGroupStatuses(taskGroups);

    return status;
  }

  @Transaction
  public void delete()
  {
    String status = this.getStatus();
    if (status.equals("Processing"))
    {
      CannotDeleteProcessingCollection ex = new CannotDeleteProcessingCollection();
      ex.setCollectionName(this.getName());
      throw ex;
    }

    List<AbstractWorkflowTask> tasks = this.getTasks();

    for (AbstractWorkflowTask task : tasks)
    {
      task.delete();
    }

    List<CollectionUploadEvent> events = this.getEvents();

    for (CollectionUploadEvent event : events)
    {
      event.delete();
    }

    CollectionReportFacade.handleDelete(this).doIt();

    MissingUploadMessage.remove(this);

    MissingMetadataMessage.remove(this);

    super.delete();

    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildRawKey(), RAW);

      this.deleteS3Folder(this.buildPointCloudKey(), PTCLOUD);

      this.deleteS3Folder(this.buildDemKey(), DEM);

      this.deleteS3Folder(this.buildOrthoKey(), ORTHO);

      this.deleteS3Folder(this.buildVideoKey(), VIDEO);
    }
  }

  protected void deleteS3Object(String key)
  {
    Util.deleteS3Object(key, this);
  }

  public List<AbstractWorkflowTask> getTasks()
  {
    WorkflowTaskQuery query = new WorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this.getOid()));

    try (OIterator<? extends WorkflowTask> iterator = query.getIterator())
    {
      return new LinkedList<AbstractWorkflowTask>(iterator.getAll());
    }
  }

  public List<CollectionUploadEvent> getEvents()
  {
    CollectionUploadEventQuery query = new CollectionUploadEventQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this.getOid()));

    try (OIterator<? extends CollectionUploadEvent> iterator = query.getIterator())
    {
      return new LinkedList<CollectionUploadEvent>(iterator.getAll());
    }
  }

  public List<CollectionReport> getReports()
  {
    CollectionReportQuery query = new CollectionReportQuery(new QueryFactory());
    query.WHERE(query.getCollection().EQ(this.getOid()));

    try (OIterator<? extends CollectionReport> iterator = query.getIterator())
    {
      return new LinkedList<CollectionReport>(iterator.getAll());
    }
  }

  @Override
  public SiteObjectsResultSet getSiteObjects(String folder, Long pageNumber, Long pageSize)
  {
    List<SiteObject> objects = new LinkedList<SiteObject>();

    if (folder == null)
    {
      SiteObject raw = new SiteObject();
      raw.setId(this.getOid() + "-" + RAW);
      raw.setName(RAW);
      raw.setComponentId(this.getOid());
      raw.setKey(this.buildRawKey());
      raw.setType(SiteObject.FOLDER);

      SiteObject ptCloud = new SiteObject();
      ptCloud.setId(this.getOid() + "-" + PTCLOUD);
      ptCloud.setName(PTCLOUD);
      ptCloud.setComponentId(this.getOid());
      ptCloud.setKey(this.buildPointCloudKey());
      ptCloud.setType(SiteObject.FOLDER);

      SiteObject dem = new SiteObject();
      dem.setId(this.getOid() + "-" + DEM);
      dem.setName(DEM);
      dem.setComponentId(this.getOid());
      dem.setKey(this.buildDemKey());
      dem.setType(SiteObject.FOLDER);

      SiteObject ortho = new SiteObject();
      ortho.setId(this.getOid() + "-" + ORTHO);
      ortho.setName(ORTHO);
      ortho.setComponentId(this.getOid());
      ortho.setKey(this.buildOrthoKey());
      ortho.setType(SiteObject.FOLDER);

      SiteObject video = new SiteObject();
      video.setId(this.getOid() + "-" + VIDEO);
      video.setName(VIDEO);
      video.setComponentId(this.getOid());
      video.setKey(this.buildVideoKey());
      video.setType(SiteObject.FOLDER);

      objects.add(raw);
      objects.add(ptCloud);
      objects.add(dem);
      objects.add(ortho);
      objects.add(video);
    }
    else
    {
      return this.getSiteObjects(folder, objects, pageNumber, pageSize);
    }

    return new SiteObjectsResultSet(Long.valueOf(objects.size()), pageNumber, pageSize, objects, folder);
  }

  @Override
  protected SiteObjectsResultSet getSiteObjects(String folder, List<SiteObject> objects, Long pageNumber, Long pageSize)
  {
    if (! ( folder.equals(RAW) || folder.equals("image") ) && ( pageNumber != null || pageSize != null ))
    {
      throw new ProgrammingErrorException(new UnsupportedOperationException("Pagination only supported for raw right now."));
    }

    SiteObjectsResultSet rs = super.getSiteObjects(folder, objects, pageNumber, pageSize);

    return rs;
  }

  @Override
  public Long getNumberOfChildren()
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT out('" + mdEdge.getDBClassName() + "').size() FROM :rid");

    GraphQuery<Integer> query = new GraphQuery<Integer>(statement.toString());
    query.setParameter("rid", this.getRID());

    return query.getSingleResult().longValue();
  }

  @Override
  public Set<String> getExcludes()
  {
    TreeSet<String> filenames = new TreeSet<String>();

    List<DocumentIF> documents = this.getDocuments();

    for (DocumentIF document : documents)
    {
      if (document.getExclude() != null && document.getExclude())
      {
        filenames.add(document.getName());
      }
    }

    return filenames;
  }

  @Override
  public UAV getUav()
  {
    String oid = this.getObjectValue(UAV);

    if (oid != null && oid.length() > 0)
    {
      return ( gov.geoplatform.uasdm.graph.UAV.get(oid) );
    }

    return null;
  }

  // @Override
  public Sensor getSensor()
  {
    String oid = this.getObjectValue(COLLECTIONSENSOR);

    if (oid != null && oid.length() > 0)
    {
      return ( gov.geoplatform.uasdm.graph.Sensor.get(oid) );
    }

    return null;
  }
  //
  // @Override
  // public void setSensor(Sensor sensor)
  // {
  // this.setCollectionSensor(sensor);
  // }

  @Override
  public boolean isMultiSpectral()
  {
    var format = this.getFormat();
    if (format != null)
      return format.isMultispectral();

    return this.getMetadata().map(metadata -> {
      Sensor sensor = metadata.getSensor();
      if (sensor == null)
        return false;

      var formats = sensor.getCollectionFormats();
      return formats.contains(CollectionFormat.STILL_MULTISPECTRAL) || formats.contains(CollectionFormat.VIDEO_MULTISPECTRAL);
    }).orElse(false);
  }

  /**
   * A thermal sensor (such as Workswell) can capture data as either radiometric
   * or RGB encoded thermal (designed for viewing with the human eye).
   * Radiometric data is used for scientific purposes, and each pixel does NOT
   * encode an "RGB" value. It encodes a real measured "radiance" values as
   * recorded by the sensor. The images also: MUST be tif, and MUST only have a
   * single band.
   * 
   * @return Whether or not the collection includes "radiometric" data used for
   *         scientific purposes, where the actual pixel values encode
   *         "radiance" values, NOT RGB data.
   */
  @Override
  public boolean isRadiometric()
  {
    var format = this.getFormat();
    if (format != null)
      return format.isRadiometric();

    return false;
  }

  @Override
  public boolean isLidar()
  {
    var format = this.getFormat();
    if (format != null)
      return format.isLidar();

    // Maintain legacy behaviour (before collection format existed)
    return this.getMetadata().map(metadata -> {
      Sensor sensor = metadata.getSensor();

      if (sensor != null)
      {
        SensorType type = sensor.getSensorType();

        return type.isLidar();
      }

      return false;
    }).orElse(false);
  }

  @Override
  public DocumentIF createDocumentIfNotExist(String key, String name, DocumentIF.Metadata metadata)
  {
    DocumentIF document = super.createDocumentIfNotExist(key, name, metadata);

    CollectionReportFacade.update(this, document).doIt();

    return document;
  }

  @Override
  public ProductIF createProductIfNotExist(String productName)
  {
    final Product product = (Product) super.createProductIfNotExist(productName);

    if (product.isNew())
    {
      this.getMetadata().ifPresent(metadata -> {
        product.addChild(metadata, EdgeType.PRODUCT_HAS_METADATA).apply();
      });
    }

    return product;
  }

  public static java.util.Collection<CollectionIF> getMissingMetadata(Integer pageNumber, Integer pageSize)
  {
    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
      final MdVertexDAOIF mdCollection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdCollection.getDBClassName());
      builder.append(" WHERE " + METADATAUPLOADED + " = :metadataUploaded");
      builder.append(" AND " + OWNER + " = :owner");
      builder.append(" AND outE('" + mdEdge.getDBClassName() + "').size() > 0");
      builder.append(" ORDER BY name");
      builder.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

      final GraphQuery<CollectionIF> query = new GraphQuery<CollectionIF>(builder.toString());
      query.setParameter("metadataUploaded", false);
      query.setParameter("owner", singleActor.getOid());

      return query.getResults();
    }

    return new LinkedHashSet<CollectionIF>();
  }

  public static long getMissingMetadataCount()
  {
    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_DOCUMENT);
      final MdVertexDAOIF mdCollection = MdVertexDAO.getMdVertexDAO(Collection.CLASS);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT COUNT(*) FROM " + mdCollection.getDBClassName());
      builder.append(" WHERE " + METADATAUPLOADED + " = :metadataUploaded");
      builder.append(" AND " + OWNER + " = :owner");
      builder.append(" AND outE('" + mdEdge.getDBClassName() + "').size() > 0");

      final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());
      query.setParameter("metadataUploaded", false);
      query.setParameter("owner", singleActor.getOid());

      return query.getSingleResult();
    }

    return 0L;
  }

  public static JSONArray toMetadataMessage(java.util.Collection<CollectionIF> collections)
  {
    JSONArray messages = new JSONArray();

    for (CollectionIF collection : collections)
    {
      messages.put(collection.toMetadataMessage());
    }

    return messages;
  }

  public static List<String> expandClause()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_PRODUCT);

    return new ArrayList<String>(Arrays.asList("OUT('" + mdEdge.getDBClassName() + "')"));
  }

  public static boolean isUAVReferenced(UAV uav)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Collection.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(Collection.UAV);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :uav");

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("uav", uav.getRID());

    Long result = query.getSingleResult();

    return ( result != null && result > 0 );
  }

  @Transaction
  public static String createCollection(JSONArray selections)
  {
    UasComponentIF component = ImageryWorkflowTaskIF.createUasComponent(selections);

    return component.getOid();
  }

}
