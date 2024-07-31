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
package gov.geoplatform.uasdm.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.WorkflowTaskStatus;
import gov.geoplatform.uasdm.bus.ImageryWorkflowTask;
import gov.geoplatform.uasdm.bus.UasComponentCompositeDeleteException;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.cog.CogPreviewParams;
import gov.geoplatform.uasdm.cog.TiTillerProxy;
import gov.geoplatform.uasdm.graph.ArtifactQuery;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.graph.UserAccessEntity;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.CompositeComponent;
import gov.geoplatform.uasdm.model.CompositeDeleteException;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.Quality;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.ProcessingInProgressException;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.FlightMetadata;
import gov.geoplatform.uasdm.view.ODMRunView;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.QuerySiteResult;
import gov.geoplatform.uasdm.view.RequestParserIF;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import gov.geoplatform.uasdm.view.TreeComponent;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.UserNotificationMessage;
import net.geoprism.GeoprismUser;
import net.geoprism.localization.LocalizationService;

public class ProjectManagementService
{

  private class RerunOrthoThread extends Thread
  {
    private ODMProcessingTask task;

    private CollectionIF      collection;

    private Set<String>       excludes;

    public RerunOrthoThread(ODMProcessingTask task, CollectionIF collection, Set<String> excludes)
    {
      super("Rerun ortho thread for collection [" + collection.getName() + "]");

      this.task = task;
      this.collection = collection;
      this.excludes = excludes;
    }

    @Override
    @Request
    public void run()
    {
      try
      {
        /*
         * Predicate for filtering out files from the zip file to send to ODM
         */
        Predicate<SiteObject> predicate = ( excludes == null || excludes.size() == 0 ) ? null : new ExcludeSiteObjectPredicate(this.excludes);

        List<String> filenames = new LinkedList<String>();

        CloseableFile zip;
        try
        {
          logger.info("Initiating download from S3 of all raw data for collection [" + collection.getName() + "].");

          zip = new CloseableFile(File.createTempFile("raw-" + this.collection.getOid(), ".zip"));

          try (OutputStream ostream = new BufferedOutputStream(new FileOutputStream(zip)))
          {
            List<String> files = downloadAll(this.collection, ImageryComponent.RAW, ostream, predicate, false);

            filenames.addAll(files);
          }
        }
        catch (IOException e)
        {
          throw new ProgrammingErrorException(e);
        }

        JSONArray array = new JSONArray();

        for (String filename : filenames)
        {
          array.put(filename);
        }

        task.appLock();
        task.setProcessFilenameArray(array.toString());
        task.apply();

        task.initiate(new FileResource(zip), collection.isMultiSpectral());

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
      }
      catch (Throwable t)
      {
        logger.error("Exception while re-running ortho", t);

        task.appLock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        task.setMessage(RunwayException.localizeThrowable(t, CommonProperties.getDefaultLocale()));
        task.apply();

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

        throw t;
      }
    }
  }

  final Logger logger = LoggerFactory.getLogger(ProjectManagementService.class);

  @Request(RequestType.SESSION)
  public List<TreeComponent> getChildren(String sessionId, String parentid)
  {
    LinkedList<TreeComponent> children = new LinkedList<TreeComponent>();

    UasComponentIF uasComponent = ComponentFacade.getComponent(parentid);

    final List<UasComponentIF> i = uasComponent.getChildren();
    i.forEach(c -> children.add(Converter.toSiteItem(c, false)));

    return children;
  }

  public List<TreeComponent> getComponentChildren(String parentid, String conditions)
  {
    LinkedList<TreeComponent> children = new LinkedList<TreeComponent>();

    UasComponentIF uasComponent = ComponentFacade.getComponent(parentid);

    final List<UasComponentIF> i = uasComponent.getChildrenWithConditions(conditions);
    i.forEach(c -> children.add(Converter.toSiteItem(c, false)));

    return children;
  }

  @Request(RequestType.SESSION)
  public UasComponent getComponent(String sessionId, String componentId)
  {
    return UasComponent.get(componentId);
  }

  @Request(RequestType.SESSION)
  public JSONObject view(String sessionId, String id)
  {
    LinkedList<TreeComponent> ancestors = new LinkedList<TreeComponent>();

    UasComponentIF uasComponent = ComponentFacade.getComponent(id);

    final List<UasComponentIF> i = uasComponent.getAncestors();
    i.forEach(c -> ancestors.add(Converter.toSiteItem(c, false)));

    if (uasComponent instanceof CollectionIF || uasComponent instanceof ImageryIF)
    {
      final UasComponentIF parent = i.get(0);
      final TreeComponent item = ancestors.get(0);

      final List<UasComponentIF> children = parent.getChildren();

      for (UasComponentIF child : children)
      {
        item.addChild(Converter.toSiteItem(child, false));
      }
    }

    Collections.reverse(ancestors);

    JSONObject response = new JSONObject();
    response.put("breadcrumbs", SiteItem.serialize(ancestors));
    response.put("item", Converter.toSiteItem(uasComponent, false).toJSON());

    return response;
  }

  @Request(RequestType.SESSION)
  public List<TreeComponent> getRoots(String sessionId, String id, String conditions, String sort)
  {
    LinkedList<TreeComponent> roots = new LinkedList<TreeComponent>();

    List<SiteIF> sites = ComponentFacade.getSites(conditions, sort);

    for (SiteIF s : sites)
    {
      roots.add(Converter.toSiteItem(s, false));
    }

    // TODO : This code is never run. The front-end always specifys a null id.
    // And why would we want to fetch all imagery ? That could be really
    // expensive.
    if (id != null)
    {
      // UasComponent component = ComponentFactory.getComponent(id);
      //
      // TreeComponent child = Converter.toSiteItem(component, false, true);
      //
      // List<UasComponentIF> ancestors = component.getAncestors();
      //
      // for (int j = 0; j < ancestors.size(); j++)
      // {
      // TreeComponent parent = null;
      //
      // if (j == ( ancestors.size() - 1 ))
      // {
      // /*
      // * The last ancestor in the list should be the root tree node, which
      // * should already be in the roots list. As such use the root list node
      // * instead and add children to it.
      // */
      // UasComponent root = ancestors.get(ancestors.size() - 1);
      //
      // for (TreeComponent r : roots)
      // {
      // if (r.getId().equals(root.getOid()))
      // {
      // parent = r;
      // }
      // }
      // }
      // else
      // {
      // parent = Converter.toSiteItem(ancestors.get(j), false);
      // }
      //
      // if (parent instanceof SiteItem)
      // {
      // /*
      // * For each ancestor get all of its children TreeComponents
      // */
      // List<TreeComponent> children = this.items(parent.getId(), null);
      //
      // for (TreeComponent chi : children)
      // {
      // if (!chi.getId().equals(child.getId()))
      // {
      // parent.addChild(chi);
      // }
      // else
      // {
      // parent.addChild(child);
      // }
      // }
      //
      // child = parent;
      // }
      // }
    }

    return roots;
  }

  /**
   * Should this method return null if the given parent has no children?
   * 
   * @param sessionId
   * @param parentId
   * @return
   */
  @Request(RequestType.SESSION)
  public SiteItem newDefaultChild(String sessionId, String parentId)
  {
    if (parentId != null)
    {
      UasComponentIF uasComponent = ComponentFacade.getComponent(parentId);

      UasComponentIF childUasComponent = uasComponent.createDefaultChild();

      if (childUasComponent != null)
      {
        return Converter.toSiteItem(childUasComponent, true);
      }
      else
      {
        return null;
      }
    }
    else
    {
      return Converter.toSiteItem(ComponentFacade.newRoot(), true);
    }
  }

  /**
   * Should this method return null if the given parent has no children?
   * 
   * @param sessionId
   * @param parentId
   * @return
   */
  @Request(RequestType.SESSION)
  public SiteItem newChild(String sessionId, String parentId, String childType)
  {
    if (parentId != null)
    {
      UasComponentIF uasComponent = ComponentFacade.getComponent(parentId);

      UasComponentIF childUasComponent = uasComponent.createChild(childType);

      if (childUasComponent != null)
      {
        return Converter.toSiteItem(childUasComponent, true);
      }
      else
      {
        return null;
      }
    }
    else
    {
      return Converter.toSiteItem(ComponentFacade.newRoot(), true);
    }
  }

  /**
   * Returns null if the given parent type has no child type.
   * 
   * @param parent
   * @param siteItem
   * @return
   */
  @Request(RequestType.SESSION)
  public SiteItem applyWithParent(String sessionId, SiteItem siteItem, String parentId)
  {
    UasComponentIF parent = parentId != null ? ComponentFacade.getComponent(parentId) : null;

    CompositeComponent<UasComponentIF> child = Converter.toNewUasComponent(parent, siteItem);

    if (child != null)
    {
      // child.setS3location(child.buildS3Key(parent));

      child.applyWithParent(parent);

      // parent.addComponent(child).apply();

      return Converter.toSiteItem(child.getComponent(), false);
    }
    else
    {
      return null;
    }
    // TODO Do domain stuff here
    // throw new ProgrammingErrorException("Unable to create item");
  }

  @Request(RequestType.SESSION)
  public SiteItem edit(String sessionId, String id)
  {
    UasComponentIF uasComponent = ComponentFacade.getComponent(id);

    return Converter.toSiteItem(uasComponent, true);
  }

  @Request(RequestType.SESSION)
  public SiteObject setExclude(String sessionId, String id, Boolean exclude)
  {
    DocumentIF document = ComponentFacade.getDocument(id);
    document.setExclude(exclude);
    document.apply();

    UasComponentIF component = document.getComponent();

    return SiteObject.create(component, document);
  }

  @Request(RequestType.SESSION)
  public void runOrtho(String sessionId, String id, Boolean processPtcloud, Boolean processDem, Boolean processOrtho, String configuration)
  {
    CollectionIF collection = ComponentFacade.getCollection(id);

    if (collection instanceof gov.geoplatform.uasdm.graph.Collection && ( (gov.geoplatform.uasdm.graph.Collection) collection ).getStatus().equals("Processing"))
    {
      ProcessingInProgressException ex = new ProcessingInProgressException();
      throw ex;
    }

    // TODO: No longer valid with multiple product approach?
    // Product product = Product.find(collection, productName);
    //
    // if(product != null && product.isLocked()) {
    // GenericException exception = new GenericException();
    // exception.setUserMessage("The collection can not be processed because its
    // product is locked.");
    // throw exception;
    // }

    ODMProcessingTask task = new ODMProcessingTask();
    task.setUploadId(id);
    task.setComponent(collection.getOid());
    task.setGeoprismUser(GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setProcessDem(processDem);
    task.setProcessOrtho(processOrtho);
    task.setProcessPtcloud(processOrtho);
    task.setTaskLabel("Orthorectification Processing (ODM) [" + collection.getName() + "]");
    task.setMessage("The images uploaded to ['" + collection.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setConfiguration(ODMProcessConfiguration.parse(configuration));
    task.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    // Get the exlcudes
    Set<String> excludes = collection.getExcludes();

    RerunOrthoThread t = new RerunOrthoThread(task, collection, excludes);
    t.setDaemon(true);
    t.start();
  }

  @Request(RequestType.SESSION)
  public void downloadAll(String sessionId, String id, String key, OutputStream out, boolean incrementDownloadCount)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    this.downloadAll(component, key, out, null, incrementDownloadCount);
  }

  private List<String> downloadAll(UasComponentIF component, String key, OutputStream out, Predicate<SiteObject> predicate, boolean incrementDownloadCount)
  {
    List<SiteObject> items = component.getSiteObjects(key, null, null).getObjects();

    List<String> filenames = new LinkedList<String>();

    if (predicate != null)
    {
      items = items.stream().filter(predicate).collect(Collectors.toList());
    }

    try (ZipOutputStream zos = new ZipOutputStream(out))
    {
      for (SiteObject item : items)
      {
        try (RemoteFileObject remoteFile = download(component, item.getKey(), incrementDownloadCount))
        {
          try (InputStream istream = remoteFile.getObjectContent())
          {
            zos.putNextEntry(new ZipEntry(item.getName()));

            IOUtils.copy(istream, zos);

            zos.closeEntry();
          }

          filenames.add(item.getName());
        }
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return filenames;
  }

  @Request(RequestType.SESSION)
  public SiteItem update(String sessionId, SiteItem siteItem)
  {

    CompositeComponent<UasComponentIF> uasComponent = Converter.toExistingUasComponent(siteItem);

    uasComponent.apply();

    // uasComponent.unlock();

    SiteItem updatedSiteItem = Converter.toSiteItem(uasComponent.getComponent(), false);

    return updatedSiteItem;
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    UasComponentIF uasComponent = ComponentFacade.getComponent(id);

    try
    {
      if (uasComponent != null)
      {
        uasComponent.delete();
      }
    }
    catch (ProgrammingErrorException e)
    {
      if (e.getCause() instanceof CompositeDeleteException)
      {
        final CompositeDeleteException cause = (CompositeDeleteException) e.getCause();

        final UasComponentCompositeDeleteException ex = new UasComponentCompositeDeleteException();
        ex.setTypeLabel(uasComponent.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
        ex.setComponents(cause.toLabel());
        throw ex;
      }

      throw e;
    }
  }

  @Request(RequestType.SESSION)
  public void removeObject(String sessionId, String id, String key)
  {
    UasComponentIF uasComponent = ComponentFacade.getComponent(id);

    uasComponent.deleteObject(key);
  }

  @Request(RequestType.SESSION)
  public void removeUploadTask(String sessionId, String uploadId)
  {
    try
    {
      AbstractUploadTask wfTask = AbstractUploadTask.getTaskByUploadId(uploadId);

      if (wfTask != null)
      {
        if (! ( wfTask.getStatus().equals(WorkflowTaskStatus.STARTED.toString()) || wfTask.getStatus().equals(WorkflowTaskStatus.UPLOADING.toString()) ))
        {
          ProcessingInProgressException ex = new ProcessingInProgressException();
          throw ex;
        }

        wfTask.delete();
      }
    }
    catch (ProcessingInProgressException ex)
    {
      // At the end of the day, the front-end is just trying to cancel out their
      // fine-uploader status. Let them cancel their upload since it's corrupt
      // anyway.
    }
  }

  @Request(RequestType.SESSION)
  public void removeTask(String sessionId, String taskId)
  {
    WorkflowTask task = WorkflowTask.get(taskId);

    if (task != null)
    {
      if (task.getNormalizedStatus().equals(WorkflowTaskStatus.PROCESSING.toString()))
      {
        ProcessingInProgressException ex = new ProcessingInProgressException();
        throw ex;
      }

      task.delete();
    }
  }

  @Request(RequestType.SESSION)
  public void handleUploadFinish(String sessionId, RequestParserIF parser, File infile)
  {
    try
    {
      ImageryProcessingJob.processFiles(parser, infile);
    }
    catch (Throwable t)
    {
      logger.error("Error occurred in 'handleUploadFinish'.", t);
    }
    finally
    {
      FileUtils.deleteQuietly(infile);
    }
  }

  @Request(RequestType.SESSION)
  public void handleUploadMergeError(String sessionId, RequestParserIF parser, Throwable t)
  {
    final AbstractUploadTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());
    final String msg = "An error occurred while merging upload chunks. " + RunwayException.localizeThrowable(t, Session.getCurrentLocale());

    task.lock();
    task.setStatus(WorkflowTaskStatus.ERROR.toString());
    task.setMessage(msg);
    task.apply();

    logger.error(msg, t);

    if (Session.getCurrentSession() != null)
    {
      NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
    }
  }

  @Request(RequestType.SESSION)
  public void handleUploadMergeStart(String sessionId, RequestParserIF parser)
  {
    final AbstractUploadTask task = ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());
    final String msg = "Processing uploaded files...";

    task.lock();
    task.setStatus(WorkflowTaskStatus.PROCESSING.toString());
    task.setMessage(msg);
    task.apply();

    if (Session.getCurrentSession() != null)
    {
      NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.UPLOAD_JOB_CHANGE, task.toJSON()));
    }
  }

  @Request(RequestType.SESSION)
  public void validate(String sessionId, RequestParserIF parser)
  {
    // Map<String, String> params = parser.getCustomParams();
    // Boolean createCollection = Boolean.valueOf(params.get("create"));
    //
    // if (createCollection)
    // {
    // String missionId = params.get("mission");
    // String folderName = params.get("folderName");
    //
    // UasComponent.validateFolderName(missionId, folderName);
    // }
  }

  @Request(RequestType.SESSION)
  public void applyMetadata(String sessionId, String json)
  {
    applyMetadata(new JSONObject(json));
  }

  @Transaction
  public void applyMetadata(JSONObject selection)
  {
    String collectionId = selection.getString("value");
    String uavId = selection.getString("uav");
    String sensorId = selection.getString("sensor");

    CollectionIF collection = ComponentFacade.getCollection(collectionId);

    if (selection.has(Collection.POINT_OF_CONTACT))
    {
      JSONObject poc = selection.getJSONObject(Collection.POINT_OF_CONTACT);

      if (poc.has(Collection.NAME))
      {
        collection.setValue(Collection.POCNAME, poc.getString(Collection.NAME));
      }

      if (poc.has(Collection.EMAIL))
      {
        collection.setValue(Collection.POCEMAIL, poc.getString(Collection.EMAIL));
      }
    }

    if (selection.has("artifacts"))
    {
      JSONArray array = selection.getJSONArray("artifacts");

      for (int i = 0; i < array.length(); i++)
      {
        JSONObject object = array.getJSONObject(i);

        if (object.getString("folder").equals(ImageryComponent.PTCLOUD))
        {
          Integer ptEpsg = object.has(Document.PTEPSG) ? object.getInt(Document.PTEPSG) : null;
          String projectName = object.has(Document.PROJECTIONNAME) ? object.getString(Document.PROJECTIONNAME) : null;

          collection.getPrimaryProduct().ifPresent(product -> {

            new ArtifactQuery(collection, product).getDocuments().stream().filter(document -> {
              return document.getS3location().contains("/" + ImageryComponent.PTCLOUD + "/");
            }).forEach(document -> {
              String name = document.getName().toUpperCase();

              if (name.endsWith(".LAZ") || name.endsWith(".LAS"))
              {
                document.setPtEpsg(ptEpsg);
                document.setProjectionName(projectName);
                document.apply();
              }
            });
          });
        }

        if (object.getString("folder").equals(ImageryComponent.ORTHO))
        {
          String orthoCorrectionModel = object.has(Document.ORTHOCORRECTIONMODEL) ? object.getString(Document.ORTHOCORRECTIONMODEL) : null;

          collection.getPrimaryProduct().ifPresent(product -> {

            new ArtifactQuery(collection, product).getDocuments().stream().filter(document -> {
              return document.getS3location().contains("/" + ImageryComponent.ORTHO + "/");
            }).forEach(document -> {
              String name = document.getName().toUpperCase();

              if (name.endsWith(".TIF"))
              {
                document.setOrthoCorrectionModel(orthoCorrectionModel);
                document.apply();
              }
            });
          });
        }
      }
    }

    collection.apply();

    collection.getMetadata().ifPresentOrElse(metadata -> {

      UAV uav = ( uavId != null && uavId.length() > 0 ) ? UAV.get(uavId) : null;
      Sensor sensor = ( sensorId != null && sensorId.length() > 0 ) ? Sensor.get(sensorId) : null;

      metadata.setUav(uav);
      metadata.setSensor(sensor);

      ImageryWorkflowTaskIF.setBooleanValue(selection, collection, Collection.EXIFINCLUDED);
      ImageryWorkflowTaskIF.setDecimalValue(selection, collection, Collection.NORTHBOUND);
      ImageryWorkflowTaskIF.setDecimalValue(selection, collection, Collection.SOUTHBOUND);
      ImageryWorkflowTaskIF.setDecimalValue(selection, collection, Collection.EASTBOUND);
      ImageryWorkflowTaskIF.setDecimalValue(selection, collection, Collection.WESTBOUND);
      ImageryWorkflowTaskIF.setDateValue(selection, collection, Collection.ACQUISITIONDATESTART);
      ImageryWorkflowTaskIF.setDateValue(selection, collection, Collection.ACQUISITIONDATEEND);
      ImageryWorkflowTaskIF.setDateValue(selection, collection, Collection.COLLECTIONDATE);
      ImageryWorkflowTaskIF.setDateValue(selection, collection, Collection.COLLECTIONENDDATE);
      ImageryWorkflowTaskIF.setIntegerValue(selection, collection, Collection.FLYINGHEIGHT);
      ImageryWorkflowTaskIF.setIntegerValue(selection, collection, Collection.NUMBEROFFLIGHTS);
      ImageryWorkflowTaskIF.setIntegerValue(selection, collection, Collection.PERCENTENDLAP);
      ImageryWorkflowTaskIF.setIntegerValue(selection, collection, Collection.PERCENTSIDELAP);
      ImageryWorkflowTaskIF.setDecimalValue(selection, collection, Collection.AREACOVERED);
      ImageryWorkflowTaskIF.setStringValue(selection, collection, Collection.WEATHERCONDITIONS);
      
      metadata.apply();
    }, () -> {
      throw new ProgrammingErrorException("Unabled to find metadata object for collection [" + collection.getOid() + "]");
    });

  }

  @Request(RequestType.SESSION)
  public void submitMetadata(String sessionId, String collectionId, String json)
  {
    CollectionIF collection = ComponentFacade.getCollection(collectionId);

    FlightMetadata metadata = FlightMetadata.parse(collection, new JSONObject(json));

    new MetadataXMLGenerator().generateAndUpload(collection, metadata, collection.getMetadata().orElseThrow());
  }

  public String getObjectsPresigned(String sessionId, String id, String key, Long pageNumber, Long pageSize)
  {
    // Don't presign the urls inside of a request since we don't want to eat up
    // a db connection longer than we have to
    return getObjectsPresignedReq(sessionId, id, key, pageNumber, pageSize).toJSON(true).toString();
  }

  @Request(RequestType.SESSION)
  public SiteObjectsResultSet getObjectsPresignedReq(String sessionId, String id, String key, Long pageNumber, Long pageSize)
  {
    return this.getObjects(id, key, pageNumber, pageSize);
  }

  @Request(RequestType.SESSION)
  public String getObjects(String sessionId, String id, String key, Long pageNumber, Long pageSize)
  {
    return this.getObjects(id, key, pageNumber, pageSize).toJSON().toString();
  }

  public SiteObjectsResultSet getObjects(String id, String key, Long pageNumber, Long pageSize)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return component.getSiteObjects(key, pageNumber, pageSize);
  }

  @Request(RequestType.SESSION)
  public JSONArray getArtifacts(String sessionId, String id)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return component.getArtifacts();
  }

  @Request(RequestType.SESSION)
  public JSONArray removeArtifacts(String sessionId, String id, String productName, String folder)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    component.getProduct(productName).ifPresent(product -> component.removeArtifacts(product, folder, true));

    JSONObject data = new JSONObject();
    data.put("collection", id);

    NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.PRODUCT_GROUP_CHANGE, data));

    return component.getArtifacts();
  }

  @Request(RequestType.SESSION)
  public void removeProduct(String sessionId, String id, String productName)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    component.removeProduct(productName);

    JSONObject data = new JSONObject();
    data.put("collection", id);

    NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.PRODUCT_GROUP_CHANGE, data));
  }

  @Request(RequestType.SESSION)
  public void setPrimaryProduct(String sessionId, String id, String productName)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    component.getProduct(productName).ifPresent(product -> {
      component.setPrimaryProduct(product);

      JSONObject data = new JSONObject();
      data.put("collection", id);

      NotificationFacade.queue(new UserNotificationMessage(Session.getCurrentSession(), MessageType.PRODUCT_GROUP_CHANGE, data));
    });

  }

  public RemoteFileObject download(String sessionId, String id, String key, boolean incrementDownloadCount)
  {
    if (sessionId != null)
    {
      return this.downloadInReq(sessionId, id, key, incrementDownloadCount);
    }
    else
    {
      UasComponentIF component = ComponentFacade.getComponent(id);

      return download(component, key, incrementDownloadCount);
    }
  }

  @Request(RequestType.SESSION)
  public InputStream downloadProductPreview(String sessionId, String productId, String artifactName)
  {
    if (artifactName.equals("ortho"))
    {
      Product product = Product.get(productId);

      UasComponentIF component = product.getComponent();
      UserAccessEntity.validateAccess(component);

      Optional<DocumentIF> op = product.getMappableOrtho();

      if (op.isPresent())
      {
        Document document = (Document) op.get();

        InputStream cogImage = new TiTillerProxy().getCogPreview(product, document, new CogPreviewParams());

        if (cogImage != null)
        {
          return cogImage;
        }
      }

      Optional<DocumentIF> orthoPng = product.getOrthoPng();

      if (orthoPng.isPresent())
      {
        return orthoPng.get().download().getObjectContent();
      }
    }

    throw new UnsupportedOperationException();
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject proxyRemoteFile(String sessionId, String url)
  {
    return RemoteFileFacade.proxy(url);
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject downloadInReq(String sessionId, String id, String key, boolean incrementDownloadCount)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return download(component, key, incrementDownloadCount);
  }

  private RemoteFileObject download(UasComponentIF component, String key, boolean incrementDownloadCount)
  {
    if (component instanceof Collection)
    {
      return ( (Collection) component ).download(key, incrementDownloadCount);
    }
    else
    {
      return component.download(key);
    }
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject downloadLast(String sessionId, String id, String key, boolean incrementDownloadCount)
  {
    List<SiteObject> items = getObjects(id, key, null, null).getObjects();

    SiteObject last = null;

    for (SiteObject item : items)
    {
      if (last == null || item.getLastModified().after(last.getLastModified()))
      {
        last = item;
      }
    }

    if (last != null)
    {
      return this.download(sessionId, id, last.getKey(), incrementDownloadCount);
    }
    else
    {
      throw new ProgrammingErrorException("No files exist");
    }
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject download(String sessionId, String id, String key, List<Range> ranges)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return component.download(key, ranges);
  }

  @Request(RequestType.SESSION)
  public List<QueryResult> search(String sessionId, String term)
  {
    List<QueryResult> results = IndexService.query(term);

    // Filter private results
    results = results.stream().filter(result -> {
      if (result instanceof QuerySiteResult)
      {
        QuerySiteResult site = (QuerySiteResult) result;

        // if (site.getIsPrivate())
        {
          JSONArray hierarchy = site.getHierarchy();
          JSONObject object = hierarchy.getJSONObject(hierarchy.length() - 1);
          String id = object.getString("id");

          UasComponent component = UasComponent.get(id);

          return UserAccessEntity.hasAccess(component);
        }
      }

      return true;
    }).collect(Collectors.toList());

    return results;
  }

  @Request(RequestType.SESSION)
  public JSONArray getTotals(String sessionId, String text, String filtersStr)
  {
    JSONArray filters = ( filtersStr != null ) ? new JSONArray(filtersStr) : new JSONArray();

    return IndexService.getTotals(text, filters);
  }

  @Request(RequestType.SESSION)
  public Page<StacItem> getItems(String sessionId, String criteriaStr, Integer pageSize, Integer pageNumber)
  {
    JSONObject criteria = ( criteriaStr != null ) ? new JSONObject(criteriaStr) : new JSONObject();

    return IndexService.getItems(criteria, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JSONObject features(String sessionId, String conditions) throws IOException
  {
    return ComponentFacade.features(conditions);
  }

  @Request(RequestType.SESSION)
  public List<TreeComponent> items(String sessionId, String id, String key, String conditions)
  {
    return this.items(id, key, conditions);
  }

  public List<TreeComponent> items(String id, String key, String conditions)
  {
    List<TreeComponent> items = new LinkedList<TreeComponent>();

    if (key == null || key.length() == 0)
    {
      items.addAll(this.getComponentChildren(id, conditions));
      items.addAll(this.getObjects(id, null, null, null).getObjects());
    }
    else
    {
      items.addAll(this.getObjects(id, key, null, null).getObjects());
    }

    // Sort by name
    Collections.sort(items, (a, b) -> a.getName().compareTo(b.getName()));

    return items;
  }

  @Request(RequestType.SESSION)
  public JSONArray bbox(String sessionId)
  {
    return ComponentFacade.bbox();
  }

  @Request(RequestType.SESSION)
  public JSONObject getMetadataOptions(String sessionId, String id)
  {
    SingleActorDAOIF user = Session.getCurrentSession().getUser();

    JSONObject response = new JSONObject();
    // response.put("sensors", Sensor.getAll());
    // response.put("platforms", Platform.getAll());
    response.put("name", user.getValue(GeoprismUser.FIRSTNAME) + " " + user.getValue(GeoprismUser.LASTNAME));
    response.put("email", user.getValue(GeoprismUser.EMAIL));

    if (id != null && id.length() > 0)
    {
      UasComponentIF component = ComponentFacade.getComponent(id);

      if (component instanceof CollectionIF)
      {
        CollectionIF collection = (CollectionIF) component;

        response.put("name", collection.getPocName());
        response.put("email", collection.getPocEmail());

        collection.getMetadata().ifPresent(metadata -> {
          UAV uav = metadata.getUav();
          Sensor sensor = metadata.getSensor();

          if (uav != null)
          {
            response.put("uav", uav.toView());
          }

          if (sensor != null)
          {
            response.put("sensor", sensor.toView());
          }

          if (metadata.getExifIncluded() != null)
          {
            response.put("exifIncluded", metadata.getExifIncluded());
          }

          if (metadata.getNorthBound() != null)
          {
            response.put("northBound", metadata.getNorthBound().setScale(5, RoundingMode.HALF_UP));
          }

          if (metadata.getSouthBound() != null)
          {
            response.put("southBound", metadata.getSouthBound().setScale(5, RoundingMode.HALF_UP));
          }

          if (metadata.getEastBound() != null)
          {
            response.put("eastBound", metadata.getEastBound().setScale(5, RoundingMode.HALF_UP));
          }

          if (metadata.getWestBound() != null)
          {
            response.put("westBound", metadata.getWestBound().setScale(5, RoundingMode.HALF_UP));
          }

          if (metadata.getAcquisitionDateStart() != null)
          {
            response.put("acquisitionDateStart", Util.formatIso8601(metadata.getAcquisitionDateStart(), false));
          }

          if (metadata.getAcquisitionDateEnd() != null)
          {
            response.put("acquisitionDateEnd", Util.formatIso8601(metadata.getAcquisitionDateEnd(), false));
          }

          if (metadata.getFlyingHeight() != null)
          {
            response.put("flyingHeight", metadata.getFlyingHeight());
          }

          if (metadata.getNumberOfFlights() != null)
          {
            response.put("numberOfFlights", metadata.getNumberOfFlights());
          }

          if (metadata.getPercentEndLap() != null)
          {
            response.put("percentEndLap", metadata.getPercentEndLap());
          }

          if (metadata.getPercentSideLap() != null)
          {
            response.put("percentSideLap", metadata.getPercentSideLap());
          }

          if (metadata.getAreaCovered() != null)
          {
            response.put("areaCovered", metadata.getAreaCovered().setScale(5, RoundingMode.HALF_UP));
          }

          if (metadata.getWeatherConditions() != null)
          {
            response.put("weatherConditions", metadata.getWeatherConditions());
          }
        });

        JSONArray artifacts = collection.getPrimaryProduct().map(product -> {
          return Arrays.stream(collection.getArtifactObjects(product)).map(a -> a.toJSON(false)).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));
        }).orElseGet(() -> new JSONArray());

        response.put("artifacts", artifacts);
      }
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public JSONObject getUAVMetadata(String sessionId, String uavId, String sensorId)
  {
    UAV uav = UAV.get(uavId);
    Sensor sensor = Sensor.get(sensorId);

    JSONObject response = new JSONObject();
    response.put("uav", uav.toView());
    response.put("sensor", sensor.toView());

    return response;
  }

  @Request(RequestType.SESSION)
  public SiteItem get(String sessionId, String id)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return Converter.toSiteItem(component, false);
  }

  @Request(RequestType.SESSION)
  public JSONObject putFile(String sessionId, String id, String folder, String productName, String fileName, RemoteFileMetadata metadata, InputStream stream)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);
    ProductIF product = component.getProduct(productName).get();

    DocumentIF doc = component.putFile(folder, fileName, product, metadata, stream);

    return doc.toJSON();
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject downloadOdmAll(String sessionId, String colId)
  {
    Collection collection = (Collection) ComponentFacade.getCollection(colId);

    ProductIF product = collection.getPrimaryProduct().orElseThrow(() -> {
      GenericException ex = new GenericException();
      ex.setUserMessage("A product does not exist");
      throw ex;
    });

    return product.downloadAllZip();
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject downloadReport(String sessionId, String colId, String productName, String folder)
  {
    Collection collection = (Collection) ComponentFacade.getCollection(colId);

    return collection.downloadReport(productName, folder);
  }

  @Request(RequestType.SESSION)
  public String createCollection(String sessionId, String json)
  {
    JSONArray selections = new JSONArray(json);

    return Collection.createCollection(selections);
  }
  
  @Request(RequestType.SESSION)
  public String createStandaloneProductGroup(String sessionId, String sJson)
  {
    JSONObject json = new JSONObject(sJson);
    
    UasComponentIF component = ComponentFacade.getComponent(json.getString("component"));
    
    ProductIF product = component.createProductIfNotExist(json.getString("productGroupName"));
    ((Product)product).setPrimary(true);

    ImageryWorkflowTaskIF.createMetadata(json.getJSONObject("metadata"), component, (VertexObject) product, EdgeType.PRODUCT_HAS_METADATA);
    
    return product.getOid();
  }

  // @Request(RequestType.SESSION) // This was causing quite a nasty little bug
  // if the user has an expired session. Don't re-enable, since this endpoint is
  // public
  public JSONObject configuration(String sessionId, String contextPath)
  {
    JSONObject config = new JSONObject();
    config.put("contextPath", contextPath);
    config.put("uasdmKeycloakEnabled", AppProperties.isKeycloakEnabled());
    config.put("uasdmRequireKeycloakLogin", AppProperties.requireKeycloakLogin());
    config.put("uasAppDisclaimer", AppProperties.getAppDisclaimer());
    config.put("localization", new JSONObject(new LocalizationService().getAllView()));

    return config;
  }

  @Request(RequestType.SESSION)
  public String getDefaultODMRunConfig(String sessionId, String collectionId)
  {
    Collection collection = (Collection) ComponentFacade.getCollection(collectionId);

    List<ODMRun> runs = ODMRun.getByComponentOrdered(collectionId);

    if (runs.size() > 0)
    {
      ODMRun run = runs.get(0);

      ODMProcessConfiguration config = run.getConfiguration();

      if (config.isIncludeGeoLocationFile())
      {
        config.setGeoLocationFileName(Product.GEO_LOCATION_FILE);
      }

      return config.toJson().toString();
    }
    else
    {
      ODMProcessConfiguration config = new ODMProcessConfiguration();

      Document geoFile = Document.find(collection.buildRawKey() + Product.GEO_LOCATION_FILE);

      if (geoFile != null)
      {
        config.setIncludeGeoLocationFile(true);
        config.setGeoLocationFileName(Product.GEO_LOCATION_FILE);
        config.setGeoLocationFormat(FileFormat.RX1R2);
      }

      collection.getMetadata().ifPresent(metadata -> {

        if (Boolean.TRUE.equals(metadata.getSensor().getHighResolution()))
        {
          config.setResolution(new BigDecimal(2.0f));
          config.setPcQuality(Quality.HIGH);
        }
      });

      return config.toJson().toString();
    }
  }

  @Request(RequestType.SESSION)
  public ODMRunView getODMRunByTask(String sessionId, String taskId)
  {
    ODMRun run = ODMRun.getForTask(taskId);

    if (run == null)
    {
      throw new DataNotFoundException("Run does not exist", MdVertexDAO.getMdVertexDAO(ODMRun.CLASS));
    }

    return ODMRunView.fromODMRun(run);
  }

  // public void logLoginAttempt(String sessionId, String username)
  // {
  // if (sessionId != null)
  // {
  // logSuccessfulLogin(sessionId);
  // }
  // else
  // {
  // // log invalid attempt??
  // }
  // }
  //
  // @Request(RequestType.SESSION)
  // public void logSuccessfulLogin(String sessionId)
  // {
  // SessionEvent.createLoginEvent();
  // }
}
