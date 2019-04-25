package gov.geoplatform.uasdm.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.S3Object;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.Mission;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.SiteQuery;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
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
  public List<TreeComponent> getRoots(String sessionId, String id)
  {
    LinkedList<TreeComponent> roots = new LinkedList<TreeComponent>();

    QueryFactory qf = new QueryFactory();
    SiteQuery q = new SiteQuery(qf);
    q.ORDER_BY_ASC(q.getName());

    OIterator<? extends Site> i = q.getIterator();

    try
    {
      i.forEach(s -> roots.add(Converter.toSiteItem(s, false)));
    }
    finally
    {
      i.close();
    }

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
            if (chi instanceof SiteItem)
            {
              ( (SiteItem) chi ).setHasChildren(true);
            }

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
  public SiteItem newChild(String sessionId, String parentId)
  {
    if (parentId != null)
    {
      UasComponent uasComponent = UasComponent.get(parentId);

      UasComponent childUasComponent = uasComponent.createChild();

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
  public void removeTask(String sessionId, String uploadId)
  {
    WorkflowTask wfTask = WorkflowTask.getTaskByUploadId(uploadId);

    if (wfTask != null)
    {
      wfTask.delete();
    }
    else
    {
      logger.error("Attempt to delete task with id [" + uploadId + "] which does not exist.");
    }
  }

  @Request(RequestType.SESSION)
  public void handleUploadFinish(String sessionId, RequestParser parser, File infile)
  {
    try
    {
      WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());
      task.lock();
      task.setStatus("Processing");
      task.setMessage("Processing archived files");
      task.apply();

      Collection collection = task.getCollection();
      collection.uploadArchive(task, infile);

      task.lock();
      task.setStatus("Complete");
      task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
      task.apply();
      
      startODMProcessing(infile, task);
    }
    finally
    {
      FileUtils.deleteQuietly(infile);
    }
  }
  
  private void startODMProcessing(File infile, WorkflowTask uploadTask)
  {
    ODMProcessingTask task = new ODMProcessingTask();
    task.setUpLoadId(uploadTask.getUpLoadId());
    task.setCollectionId(uploadTask.getCollectionOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("Orthorectification Processing (ODM) [" + task.getCollection().getName() + "]");
    task.setMessage("Your images are submitted for processing. Check back later for updates.");
    task.apply();
    
    task.initiate(infile);
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
  public void uploadMetadata(String sessionId, String missionId, MultipartFileParameter file)
  {
    try (InputStream istream = file.getInputStream())
    {
      Mission mission = Mission.get(missionId);
      mission.uploadMetadata(file.getFilename(), istream);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Request(RequestType.SESSION)
  public List<SiteObject> getItems(String sessionId, String id, String key)
  {
    return this.getObjects(id, key);
  }

  public List<SiteObject> getObjects(String id, String key)
  {
    UasComponent component = UasComponent.get(id);

    return component.getSiteObjects(key);
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
      items.addAll(this.getObjects(id, null));
    }
    else
    {
      items.addAll(this.getObjects(id, key));
    }

    return items;
  }

  @Request(RequestType.SESSION)
  public JSONArray bbox(String sessionId)
  {
    return UasComponent.bbox();
  }
}
