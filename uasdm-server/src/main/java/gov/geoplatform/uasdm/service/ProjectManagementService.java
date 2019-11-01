package gov.geoplatform.uasdm.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.geoplatform.uasdm.ImageryProcessingJob;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.ImageryComponent;
import gov.geoplatform.uasdm.bus.Platform;
import gov.geoplatform.uasdm.bus.Sensor;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.SiteObjectsResultSet;
import gov.geoplatform.uasdm.bus.SiteQuery;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.postgis.ST_WITHIN;
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.view.SiteItem;
import gov.geoplatform.uasdm.view.SiteObject;
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

    UasComponent uasComponent = UasComponent.get(parentid);

    OIterator<? extends UasComponent> i = uasComponent.getAllComponents();

    try
    {
      i.forEach(c -> children.add(Converter.toSiteItem(c, false)));
    }
    finally
    {
      i.close();
    }
    return children;
  }

  @Request(RequestType.SESSION)
  public List<TreeComponent> getRoots(String sessionId, String id, String bounds)
  {
    LinkedList<TreeComponent> roots = new LinkedList<TreeComponent>();

    QueryFactory qf = new QueryFactory();
    SiteQuery q = new SiteQuery(qf);

    if (bounds != null && bounds.length() > 0)
    {
      // {"_sw":{"lng":-90.55128715174949,"lat":20.209904454730363},"_ne":{"lng":-32.30032930862288,"lat":42.133128793454745}}
      JSONObject object = new JSONObject(bounds);

      JSONObject sw = object.getJSONObject("_sw");
      JSONObject ne = object.getJSONObject("_ne");

      double x1 = sw.getDouble("lng");
      double x2 = ne.getDouble("lng");
      double y1 = sw.getDouble("lat");
      double y2 = ne.getDouble("lat");

      Envelope envelope = new Envelope(x1, x2, y1, y2);
      GeometryFactory factory = new GeometryFactory();
      Geometry geometry = factory.toGeometry(envelope);

      q.WHERE(new ST_WITHIN(q.getGeoPoint(), geometry));
    }

    q.ORDER_BY_ASC(q.getName());

    try (OIterator<? extends Site> i = q.getIterator())
    {
      i.forEach(s -> roots.add(Converter.toSiteItem(s, false)));
    }

    // TODO : This code is never run. The front-end always specifys a null id.
    //        And why would we want to fetch all imagery ? That could be really expensive.
    if (id != null)
    {
      UasComponent component = UasComponent.get(id);

      TreeComponent child = Converter.toSiteItem(component, false, true);

      List<UasComponent> ancestors = component.getAncestors();

      for (int j = 0; j < ancestors.size(); j++)
      {
        TreeComponent parent = null;

        if (j == ( ancestors.size() - 1 ))
        {
          /*
           * The last ancestor in the list should be the root tree node, which
           * should already be in the roots list. As such use the root list node
           * instead and add children to it.
           */
          UasComponent root = ancestors.get(ancestors.size() - 1);

          for (TreeComponent r : roots)
          {
            if (r.getId().equals(root.getOid()))
            {
              parent = r;
            }
          }
        }
        else
        {
          parent = Converter.toSiteItem(ancestors.get(j), false);
        }

        if (parent instanceof SiteItem)
        {
          /*
           * For each ancestor get all of its children TreeComponents
           */
          List<TreeComponent> children = this.items(parent.getId(), null);

          for (TreeComponent chi : children)
          {
            if (!chi.getId().equals(child.getId()))
            {
              parent.addChild(chi);
            }
            else
            {
              parent.addChild(child);
            }
          }

          child = parent;
        }
      }
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
      UasComponent uasComponent = UasComponent.get(parentId);

      UasComponent childUasComponent = uasComponent.createDefaultChild();

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
      return Converter.toSiteItem(new Site(), true);
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
      UasComponent uasComponent = UasComponent.get(parentId);

      UasComponent childUasComponent = uasComponent.createChild(childType);

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
      return Converter.toSiteItem(new Site(), true);
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
    UasComponent parent = parentId != null ? UasComponent.get(parentId) : null;

    UasComponent child = Converter.toNewUasComponent(parent, siteItem);

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
    UasComponent uasComponent = UasComponent.get(id);

    return Converter.toSiteItem(uasComponent, true);
  }

  @Request(RequestType.SESSION)
  public void runOrtho(String sessionId, String id, String excludes)
  {
    Collection collection = (Collection) UasComponent.get(id);

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
    task.setComponentId(collection.getOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("Orthorectification Processing (ODM) [" + task.getComponent().getName() + "]");
    task.setMessage("The images uploaded to ['" + task.getComponent().getName() + "'] are submitted for orthorectification processing. Check back later for updates.");
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
        S3Object s3Obj = download(sessionId, id, item.getKey());

        try (S3ObjectInputStream istream = s3Obj.getObjectContent())
        {
          zos.putNextEntry(new ZipEntry(item.getName()));

          IOUtils.copy(istream, zos);

          zos.closeEntry();
        }

        filenames.add(item.getName());
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
    UasComponent uasComponent = UasComponent.get(siteItem.getId());

    uasComponent.lock();

    uasComponent = Converter.toExistingUasComponent(siteItem);

    uasComponent.apply();

    uasComponent.unlock();

    SiteItem updatedSiteItem = Converter.toSiteItem(uasComponent, false);

    return updatedSiteItem;
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    UasComponent uasComponent = UasComponent.get(id);

    uasComponent.delete();
  }

  @Request(RequestType.SESSION)
  public void removeObject(String sessionId, String id, String key)
  {
    UasComponent uasComponent = UasComponent.get(id);

    uasComponent.delete(key);
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
      ImageryProcessingJob.processImages(parser, infile);
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
    Map<String, String> params = parser.getCustomParams();
    Boolean createCollection = new Boolean(params.get("create"));

    if (createCollection)
    {
      String missionId = params.get("mission");
      String folderName = params.get("folderName");

      UasComponent.validateFolderName(missionId, folderName);
    }
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
    UasComponent component = UasComponent.get(id);

    return component.getSiteObjects(key, pageNumber, pageSize);
  }

  @Request(RequestType.SESSION)
  public S3Object download(String sessionId, String id, String key)
  {
    UasComponent component = UasComponent.get(id);

    return component.download(key);
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
    return UasComponent.features();
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
    return UasComponent.bbox();
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
      UasComponent component = UasComponent.get(id);

      if (component instanceof Collection)
      {
        response.put("platform", component.getValue(Collection.PLATFORM));
        response.put("sensor", component.getValue(Collection.SENSOR));
      }
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public SiteItem get(String sessionId, String id)
  {
    UasComponent component = UasComponent.get(id);

    return Converter.toSiteItem(component, false);
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
