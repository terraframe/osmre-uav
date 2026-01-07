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
package gov.geoplatform.uasdm.bus;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.RawSet;
import gov.geoplatform.uasdm.graph.Sensor.CollectionFormat;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentRawSet;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.raw.FileUploadProcessor;
import gov.geoplatform.uasdm.view.CreateRawSetView;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;
import net.geoprism.GeoprismUser;

public class Collection extends CollectionBase implements ImageryComponent, CollectionIF
{
  private static final long serialVersionUID = 1371809368;

  final Logger              log              = LoggerFactory.getLogger(Collection.class);

  public Collection()
  {
    super();
  }

  @Override
  public void regenerateMetadata()
  {
  }

  /**
   * Returns null, as a Collection cannot have a child.
   */
  @Override
  public UasComponent createDefaultChild()
  {
    // TODO throw exception.

    return null;
  }

  @Override
  public Optional<CollectionMetadata> getMetadata()
  {
    return Optional.empty();
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
  public Boolean getHasAllZip()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setHasAllZip(Boolean b)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPocEmail()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPocName()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMultiSpectral()
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public boolean isRadiometric()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLidar()
  {
    throw new UnsupportedOperationException();
  }

  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addMission((Mission) uasComponent);
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

  public static JSONArray toMetadataMessage(java.util.Collection<Collection> collections)
  {
    JSONArray messages = new JSONArray();

    for (Collection collection : collections)
    {
      messages.put(collection.toMetadataMessage());
    }

    return messages;
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

      this.createS3Folder(this.buildVideoKey());

      this.createS3Folder(this.buildPointCloudKey());

      this.createS3Folder(this.buildDemKey());

      this.createS3Folder(this.buildOrthoKey());
    }
  }

  @Transaction
  public void delete()
  {
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

  public String buildRawKey()
  {
    return this.getS3location() + RAW + "/";
  }

  public String buildVideoKey()
  {
    return this.getS3location() + VIDEO + "/";
  }

  public String buildPointCloudKey()
  {
    return this.getS3location() + PTCLOUD + "/";
  }

  public String buildDemKey()
  {
    return this.getS3location() + DEM + "/";
  }

  public String buildOrthoKey()
  {
    return this.getS3location() + ORTHO + "/";
  }

  @Override
  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationFileResource file, String uploadTarget, ProductIF product)
  {
    return new FileUploadProcessor().process(task, file, this, uploadTarget, product);
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
    if (!folder.equals(RAW) && ( pageNumber != null || pageSize != null ))
    {
      throw new ProgrammingErrorException(new UnsupportedOperationException("Pagination only supported for raw right now."));
    }

    SiteObjectsResultSet rs = super.getSiteObjects(folder, objects, pageNumber, pageSize);

    return rs;
  }

  @Override
  public Long getNumberOfChildren()
  {
    // int count = 0;
    // count += this.getItemCount(this.buildRawKey());
    // count += this.getItemCount(this.buildPointCloudKey());
    // count += this.getItemCount(this.buildDemKey());
    // count += this.getItemCount(this.buildOrthoKey());

    DocumentQuery query = new DocumentQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(this));

    return Long.valueOf(query.getCount());
  }

  public Collection getUasComponent()
  {
    return this;
  }

  public Logger getLog()
  {
    return this.log;
  }

  @Override
  public Set<String> getExcludes()
  {
    return new TreeSet<String>();
  }

  @Override
  public AbstractWorkflowTask createWorkflowTask(String userOid, String uploadId, String uploadTarget)
  {
    WorkflowTask workflowTask = new WorkflowTask();
    workflowTask.setUploadId(uploadId);
    workflowTask.setComponent(this.getOid());
    workflowTask.setGeoprismUserId(userOid);
    workflowTask.setTaskLabel("UAV data upload for collection [" + this.getName() + "]");

    return workflowTask;
  }

  public static java.util.Collection<CollectionIF> getMissingMetadata(Integer pageNumber, Integer pageSize)
  {
    java.util.Collection<CollectionIF> collectionList = new LinkedHashSet<CollectionIF>();

    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      CollectionQuery cQ = getMissingMetadataQuery(singleActor);

      cQ.restrictRows(pageSize, pageNumber);

      try (OIterator<? extends Collection> i = cQ.getIterator())
      {
        for (Collection collection : i)
        {
          collectionList.add(collection);
        }
      }
    }

    return collectionList;
  }

  public static long getMissingMetadataCount()
  {
    SingleActor singleActor = GeoprismUser.getCurrentUser();

    if (singleActor != null)
    {
      return getMissingMetadataQuery(singleActor).getCount();
    }

    return 0l;
  }

  public static CollectionQuery getMissingMetadataQuery(SingleActor singleActor)
  {
    QueryFactory qf = new QueryFactory();

    CollectionQuery cQ = new CollectionQuery(qf);

    CollectionUploadEventQuery eQ = new CollectionUploadEventQuery(qf);

    // Get Events created by the current user
    eQ.WHERE(eQ.getGeoprismUser().EQ(singleActor));

    // Get Collections associated with those tasks
    cQ.WHERE(cQ.getOid().EQ(eQ.getComponent()));

    // Get the Missions of those Collections;
    cQ.AND(cQ.getMetadataUploaded().EQ(false).OR(cQ.getMetadataUploaded().EQ((Boolean) null)));

    return cQ;
  }

  @Override
  public CollectionFormat getFormat()
  {
    return null;
  }

  @Override
  public void setFormat(CollectionFormat format)
  {
    
  }

  @Override
  public void setFormat(String format)
  {
    
  }

  @Override
  public List<ComponentRawSet> getDerivedRawSets(String sortField, String sortOrder)
  {
    // TODO Auto-generated method stub
    return null;
  }  
}
