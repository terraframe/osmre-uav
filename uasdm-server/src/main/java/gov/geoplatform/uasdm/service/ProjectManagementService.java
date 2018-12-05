package gov.geoplatform.uasdm.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import gov.geoplatform.uasdm.view.Converter;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.view.SiteItem;

public class ProjectManagementService
{

  final Logger log = LoggerFactory.getLogger(ProjectManagementService.class);

  @Request(RequestType.SESSION)
  public List<SiteItem> getChildren(String sessionId, String parentid)
  {
    LinkedList<SiteItem> children = new LinkedList<SiteItem>();

    UasComponent uasComponent = UasComponent.get(parentid);

    OIterator<? extends UasComponent> i = uasComponent.getAllComponents();

    try
    {
      i.forEach(c -> children.add(Converter.toSiteItem(c)));
    }
    finally
    {
      i.close();
    }
    return children;
  }

  @Request(RequestType.SESSION)
  public List<SiteItem> getRoots(String sessionId)
  {
    LinkedList<SiteItem> roots = new LinkedList<SiteItem>();

    QueryFactory qf = new QueryFactory();
    SiteQuery q = new SiteQuery(qf);

    OIterator<? extends Site> i = q.getIterator();

    try
    {
      i.forEach(s -> roots.add(Converter.toSiteItem(s)));
    }
    finally
    {
      i.close();
    }

    return roots;
  }

  @Request(RequestType.SESSION)
  /**
   * Should this method return null if the given parent has no children?
   * 
   * @param sessionId
   * @param parentId
   * @return
   */
  public SiteItem newChild(String sessionId, String parentId)
  {
    UasComponent uasComponent = UasComponent.get(parentId);

    UasComponent childUasComponent = uasComponent.createChild();

    if (childUasComponent != null)
    {
      return Converter.toSiteItem(childUasComponent);
    }
    else
    {
      return null;
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
    UasComponent parent = UasComponent.get(parentId);

    UasComponent child = Converter.toNewUasComponent(parent, siteItem);

    if (child != null)
    {
      // child.setS3location(child.buildS3Key(parent));

      child.applyWithParent(parent);

      // parent.addComponent(child).apply();

      return Converter.toSiteItem(child);
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

    return Converter.toSiteItem(uasComponent);
  }

  @Request(RequestType.SESSION)
  public SiteItem update(String sessionId, SiteItem siteItem)
  {
    UasComponent uasComponent = UasComponent.get(siteItem.getId());

    uasComponent.lock();

    uasComponent = Converter.toExistingUasComponent(siteItem);

    uasComponent.apply();

    uasComponent.unlock();

    SiteItem updatedSiteItem = Converter.toSiteItem(uasComponent);

    return updatedSiteItem;
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    UasComponent uasComponent = UasComponent.get(id);

    uasComponent.delete();
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
      task.setMessage("The Uploaded successfully completed.  All files except those mentioned were archived.");
      task.apply();
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
      String name = params.get("name");

      UasComponent.validateName(missionId, name);
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
    UasComponent component = UasComponent.get(id);

    return component.getSiteObjects(key);
  }

  @Request(RequestType.SESSION)
  public S3Object download(String sessionId, String id, String key)
  {
    UasComponent component = UasComponent.get(id);

    return component.download(key);
  }
}
