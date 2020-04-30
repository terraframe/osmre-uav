package gov.geoplatform.uasdm.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.Platform;
import gov.geoplatform.uasdm.bus.Sensor;
import gov.geoplatform.uasdm.bus.UasComponentCompositeDeleteException;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.CompositeDeleteException;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.Range;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import gov.geoplatform.uasdm.view.TreeComponent;
import net.geoprism.GeoprismUser;

public class ProjectManagementService
{

  final Logger logger = LoggerFactory.getLogger(ProjectManagementService.class);

  @Request(RequestType.SESSION)
  public List<TreeComponent> getChildren(String sessionId, String parentid)
  {
    return this.getChildren(parentid);
  }

  public List<TreeComponent> getChildren(String parentid)
  {
    LinkedList<TreeComponent> children = new LinkedList<TreeComponent>();

    UasComponentIF uasComponent = ComponentFacade.getComponent(parentid);

    final List<UasComponentIF> i = uasComponent.getChildren();
    i.forEach(c -> children.add(Converter.toSiteItem(c, false)));

    return children;
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
  public List<TreeComponent> getRoots(String sessionId, String id, String bounds)
  {
    LinkedList<TreeComponent> roots = new LinkedList<TreeComponent>();

    List<SiteIF> sites = ComponentFacade.getSites(bounds);

    for (SiteIF s : sites)
    {
      roots.add(Converter.toSiteItem(s, false));
    }

    // TODO : This code is never run. The front-end always specifys a null id.
    // And why would we want to fetch all imagery ? That could be really
    // expensive.
    if (id != null)
    {
//      UasComponent component = ComponentFactory.getComponent(id);
//
//      TreeComponent child = Converter.toSiteItem(component, false, true);
//
//      List<UasComponentIF> ancestors = component.getAncestors();
//
//      for (int j = 0; j < ancestors.size(); j++)
//      {
//        TreeComponent parent = null;
//
//        if (j == ( ancestors.size() - 1 ))
//        {
//          /*
//           * The last ancestor in the list should be the root tree node, which
//           * should already be in the roots list. As such use the root list node
//           * instead and add children to it.
//           */
//          UasComponent root = ancestors.get(ancestors.size() - 1);
//
//          for (TreeComponent r : roots)
//          {
//            if (r.getId().equals(root.getOid()))
//            {
//              parent = r;
//            }
//          }
//        }
//        else
//        {
//          parent = Converter.toSiteItem(ancestors.get(j), false);
//        }
//
//        if (parent instanceof SiteItem)
//        {
//          /*
//           * For each ancestor get all of its children TreeComponents
//           */
//          List<TreeComponent> children = this.items(parent.getId(), null);
//
//          for (TreeComponent chi : children)
//          {
//            if (!chi.getId().equals(child.getId()))
//            {
//              parent.addChild(chi);
//            }
//            else
//            {
//              parent.addChild(child);
//            }
//          }
//
//          child = parent;
//        }
//      }
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

    UasComponentIF child = Converter.toNewUasComponent(parent, siteItem);

    if (child != null)
    {
      // child.setS3location(child.buildS3Key(parent));

      child.applyWithParent(parent);

      // parent.addComponent(child).apply();

      return Converter.toSiteItem(child, false);
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
  public void runOrtho(String sessionId, String id, String excludes)
  {
    CollectionIF collection = ComponentFacade.getCollection(id);

    /*
     * Predicate for filtering out files from the zip file to send to ODM
     */
    Predicate<SiteObject> predicate = ( excludes == null || excludes.length() == 0 ) ? null : new ExcludeSiteObjectPredicate(new JSONArray(excludes));

    List<String> filenames = new LinkedList<String>();

    File zip;
    try
    {
      logger.info("Initiating download from S3 of all raw data for collection [" + collection.getName() + "].");

      zip = File.createTempFile("raw-" + id, ".zip");

      try (OutputStream ostream = new BufferedOutputStream(new FileOutputStream(zip)))
      {
        List<String> files = downloadAll(sessionId, id, ImageryComponent.RAW, ostream, predicate);

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

    ODMProcessingTask task = new ODMProcessingTask();
    task.setUploadId(id);
    task.setComponent(collection.getOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("Orthorectification Processing (ODM) [" + collection.getName() + "]");
    task.setMessage("The images uploaded to ['" + collection.getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
    task.setFilenames(array.toString());
    task.apply();

    task.initiate(new FileResource(zip), collection.getSensor().isMultiSpectral());
  }

  @Request(RequestType.SESSION)
  public void downloadAll(String sessionId, String id, String key, OutputStream out)
  {
    this.downloadAll(sessionId, id, key, out, null);
  }

  private List<String> downloadAll(String sessionId, String id, String key, OutputStream out, Predicate<SiteObject> predicate)
  {
    List<SiteObject> items = getObjects(id, key, null, null).getObjects();

    List<String> filenames = new LinkedList<String>();

    if (predicate != null)
    {
      items = items.stream().filter(predicate).collect(Collectors.toList());
    }

    try (ZipOutputStream zos = new ZipOutputStream(out))
    {
      for (SiteObject item : items)
      {
        try (RemoteFileObject remoteFile = download(sessionId, id, item.getKey()))
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
    UasComponentIF uasComponent = ComponentFacade.getComponent(siteItem.getId());

//    uasComponent.lock();

    uasComponent = Converter.toExistingUasComponent(siteItem);

    uasComponent.apply();

//    uasComponent.unlock();

    SiteItem updatedSiteItem = Converter.toSiteItem(uasComponent, false);

    return updatedSiteItem;
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    UasComponentIF uasComponent = ComponentFacade.getComponent(id);

    try
    {
      uasComponent.delete();
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
  public void removeTask(String sessionId, String uploadId)
  {
    AbstractUploadTask wfTask = AbstractUploadTask.getTaskByUploadId(uploadId);

    if (wfTask != null)
    {
      wfTask.delete();
    }
  }

  @Request(RequestType.SESSION)
  public void handleUploadFinish(String sessionId, RequestParser parser, File infile)
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
  public void validate(String sessionId, RequestParser parser)
  {
//    Map<String, String> params = parser.getCustomParams();
//    Boolean createCollection = new Boolean(params.get("create"));
//
//    if (createCollection)
//    {
//      String missionId = params.get("mission");
//      String folderName = params.get("folderName");
//
//      UasComponent.validateFolderName(missionId, folderName);
//    }
  }

  @Request(RequestType.SESSION)
  public void submitMetadata(String sessionId, String json)
  {
    new MetadataXMLGenerator(json).generateAndUpload();
  }

  @Request(RequestType.SESSION)
  public String getObjects(String sessionId, String id, String key, Integer pageNumber, Integer pageSize)
  {
    return this.getObjects(id, key, pageNumber, pageSize).toJSON().toString();
  }

  public SiteObjectsResultSet getObjects(String id, String key, Integer pageNumber, Integer pageSize)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return component.getSiteObjects(key, pageNumber, pageSize);
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject download(String sessionId, String id, String key)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return component.download(key);
  }

  @Request(RequestType.SESSION)
  public RemoteFileObject downloadLast(String sessionId, String id, String key)
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
      return this.download(sessionId, id, last.getKey());
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
    List<QueryResult> results = SolrService.query(term);

    return results;
  }

  @Request(RequestType.SESSION)
  public JSONObject features(String sessionId) throws IOException
  {
    return ComponentFacade.features();
  }

  @Request(RequestType.SESSION)
  public List<TreeComponent> items(String sessionId, String id, String key)
  {
    return this.items(id, key);
  }

  public List<TreeComponent> items(String id, String key)
  {
    List<TreeComponent> items = new LinkedList<TreeComponent>();

    if (key == null || key.length() == 0)
    {
      items.addAll(this.getChildren(id));
      items.addAll(this.getObjects(id, null, null, null).getObjects());
    }
    else
    {
      items.addAll(this.getObjects(id, key, null, null).getObjects());
    }

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
    response.put("sensors", Sensor.getAll());
    response.put("platforms", Platform.getAll());
    response.put("name", user.getValue(GeoprismUser.FIRSTNAME) + " " + user.getValue(GeoprismUser.LASTNAME));
    response.put("email", user.getValue(GeoprismUser.EMAIL));

    if (id != null && id.length() > 0)
    {
      UasComponentIF component = ComponentFacade.getComponent(id);

      if (component instanceof CollectionIF)
      {
        response.put("platform", (String) component.getObjectValue(Collection.PLATFORM));
        response.put("sensor", (String) component.getObjectValue(Collection.SENSOR));
      }
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public SiteItem get(String sessionId, String id)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);

    return Converter.toSiteItem(component, false);
  }

  @Request(RequestType.SESSION)
  public JSONObject putFile(String sessionId, String id, String folder, String fileName, RemoteFileMetadata metadata, InputStream stream)
  {
    UasComponentIF component = ComponentFacade.getComponent(id);
    DocumentIF doc = component.putFile(folder, fileName, metadata, stream);

    return doc.toJSON();
  }

//  public void logLoginAttempt(String sessionId, String username)
//  {
//    if (sessionId != null)
//    {
//      logSuccessfulLogin(sessionId);
//    }
//    else
//    {
//      // log invalid attempt??
//    }
//  }
//
//  @Request(RequestType.SESSION)
//  public void logSuccessfulLogin(String sessionId)
//  {
//    SessionEvent.createLoginEvent();
//  }
}
