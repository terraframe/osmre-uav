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
package gov.geoplatform.uasdm.model;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.Entity;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.DataAccessException;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.MissingUploadMessage;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.UasComponent;
import net.geoprism.GeoprismUser;

public interface ImageryWorkflowTaskIF extends AbstractWorkflowTaskIF
{
  public JSONObject toJSON();

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel();

  public ImageryComponent getImageryComponent();

  /**
   * Locks the given Entity by the current treads.
   * 
   * @throws DataAccessException
   *           if the Entity is locked by another user
   */
  public void appLock();

  /**
   * {@link Entity#apply()}
   */
  public void apply();

  public static UasComponentIF createUasComponent(JSONArray selections)
  {
    // The root object will always already be created
    UasComponentIF component = ComponentFacade.getComponent(selections.getJSONObject(0).getString("value"));

    for (int i = 1; i < selections.length(); i++)
    {
      JSONObject selection = selections.getJSONObject(i);

      if (selection.getBoolean("isNew"))
      {
        String name = selection.getString("label");

        // Try to find a component with the same name and parent
        UasComponentIF child = component.getChild(name);

        if (child == null)
        {
          child = component.createChild(selection.getString("type"));
          child.setName(name);
          setBooleanValue(selection, child, UasComponent.ISPRIVATE);

          // child.setValue(Collection.U, value);

          if (selection.has(UasComponent.DESCRIPTION))
          {
            child.setValue(UasComponent.DESCRIPTION, selection.getString(UasComponent.DESCRIPTION));
          }

          if (child instanceof ProjectIF)
          {
            if (selection.has(Project.SHORTNAME))
            {
              child.setValue(Project.SHORTNAME, selection.getString(Project.SHORTNAME));
            }

            if (selection.has(Project.RESTRICTED))
            {
              child.setValue(Project.RESTRICTED, selection.getBoolean(Project.RESTRICTED));
            }

            setDateValue(selection, child, Project.SUNSETDATE);
          }
          else if (child instanceof MissionIF)
          {
            if (selection.has(Mission.CONTRACTINGOFFICE))
            {
              child.setValue(Mission.CONTRACTINGOFFICE, selection.getString(Mission.CONTRACTINGOFFICE));
            }

            if (selection.has(Mission.VENDOR))
            {
              child.setValue(Mission.VENDOR, selection.getString(Mission.VENDOR));
            }

          }
          else if (child instanceof CollectionIF)
          {
            if (selection.has(Collection.POINT_OF_CONTACT))
            {
              JSONObject poc = selection.getJSONObject(Collection.POINT_OF_CONTACT);

              if (poc.has(Collection.NAME))
              {
                child.setValue(Collection.POCNAME, poc.getString(Collection.NAME));
              }

              if (poc.has(Collection.EMAIL))
              {
                child.setValue(Collection.POCEMAIL, poc.getString(Collection.EMAIL));
              }
            }
            
            if (selection.has(Collection.FORMAT))
              ((CollectionIF)child).setFormat(selection.getString(Collection.FORMAT));
          }

          child.applyWithParent(component);

          // Upload the metadata file
          if (child instanceof CollectionIF)
          {
            createMetadata(selection, child, null, (VertexObject) child, EdgeType.COLLECTION_HAS_METADATA);

            MissingUploadMessage message = new MissingUploadMessage();
            message.setComponent(child.getOid());
            message.setGeoprismUser(GeoprismUser.getCurrentUser());
            message.apply();
          }
        }

        component = child;
      }
      else
      {
        component = ComponentFacade.getComponent(selection.getString("value"));
      }
    }

    return component;
  }

  public static CollectionMetadata createMetadata(JSONObject json, UasComponentIF component, Product product, VertexObject vertexHasMetadata, String vertexHasMetadataEdge)
  {
    CollectionMetadata metadata = new CollectionMetadata();

    if (json.has(CollectionMetadata.EXIFINCLUDED))
    {
      metadata.setValue(CollectionMetadata.EXIFINCLUDED, json.getBoolean(CollectionMetadata.EXIFINCLUDED));
    }

    setDecimalValue(json, metadata, CollectionMetadata.NORTHBOUND);
    setDecimalValue(json, metadata, CollectionMetadata.SOUTHBOUND);
    setDecimalValue(json, metadata, CollectionMetadata.EASTBOUND);
    setDecimalValue(json, metadata, CollectionMetadata.WESTBOUND);
    setDateValue(json, metadata, CollectionMetadata.ACQUISITIONDATESTART);
    setDateValue(json, metadata, CollectionMetadata.ACQUISITIONDATEEND);
    setDateValue(json, metadata, CollectionMetadata.COLLECTIONDATE);
    setDateValue(json, metadata, CollectionMetadata.COLLECTIONENDDATE);
    setIntegerValue(json, metadata, CollectionMetadata.FLYINGHEIGHT);
    setIntegerValue(json, metadata, CollectionMetadata.NUMBEROFFLIGHTS);
    setIntegerValue(json, metadata, CollectionMetadata.PERCENTENDLAP);
    setIntegerValue(json, metadata, CollectionMetadata.PERCENTSIDELAP);
    setDecimalValue(json, metadata, CollectionMetadata.AREACOVERED);
    setStringValue(json, metadata, CollectionMetadata.WEATHERCONDITIONS);

    if (json.has(CollectionMetadata.UAV))
    {
      metadata.setValue(CollectionMetadata.UAV, json.getString(CollectionMetadata.UAV));
    }

    if (json.has(CollectionMetadata.SENSOR))
    {
      metadata.setValue(CollectionMetadata.SENSOR, json.getString(CollectionMetadata.SENSOR));
    }

    metadata.apply();
    ( (VertexObject) vertexHasMetadata ).addChild(metadata, vertexHasMetadataEdge).apply();

    new MetadataXMLGenerator().generateAndUpload(component, product, metadata);

    return metadata;
  }

  public static Date getDateValue(JSONObject selection, String attributeName)
  {
    if (selection.has(attributeName))
    {
      if (!selection.isNull(attributeName))
      {
        try
        {
          return Util.parseIso8601(selection.getString(attributeName), false);
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }
    }

    return null;
  }

  public static void setDateValue(JSONObject selection, ComponentWithAttributes child, String attributeName)
  {
    if (selection.has(attributeName))
    {
      if (!selection.isNull(attributeName))
      {

        try
        {
          child.setValue(attributeName, Util.parseIso8601(selection.getString(attributeName), false));
        }
        catch (ParseException e)
        {
          GenericException exception = new GenericException(e);
          exception.setUserMessage(e.getMessage());
          throw exception;
        }
      }
      else
      {
        child.setValue(attributeName, null);
      }
    }
  }

  public static void setDecimalValue(JSONObject selection, ComponentWithAttributes child, String attributeName)
  {
    if (selection.has(attributeName))
    {
      if (!selection.isNull(attributeName))
      {
        child.setValue(attributeName, new BigDecimal(selection.getDouble(attributeName)));
      }
      else
      {
        child.setValue(attributeName, null);
      }
    }
  }

  public static void setBooleanValue(JSONObject selection, ComponentWithAttributes child, String attributeName)
  {
    if (selection.has(attributeName))
    {
      if (!selection.isNull(attributeName))
      {
        child.setValue(attributeName, Boolean.valueOf(selection.getBoolean(attributeName)));
      }
      else
      {
        child.setValue(attributeName, false);
      }
    }
  }

  public static void setIntegerValue(JSONObject selection, ComponentWithAttributes child, String attributeName)
  {
    if (selection.has(attributeName))
    {
      if (!selection.isNull(attributeName))
      {
        child.setValue(attributeName, Integer.valueOf(selection.getInt(attributeName)));
      }
      else
      {
        child.setValue(attributeName, null);
      }
    }
  }

  public static void setStringValue(JSONObject selection, ComponentWithAttributes child, String attributeName)
  {
    if (selection.has(attributeName))
    {
      if (!selection.isNull(attributeName))
      {
        child.setValue(attributeName, selection.getString(attributeName));
      }
      else
      {
        child.setValue(attributeName, null);
      }
    }
  }

  /**
   * Returns the {@link AbstractWorkflowTask} for the given {@link UasComponent}
   * or null if none exists.
   * 
   * @param parser
   * 
   * @return
   */
  public static AbstractWorkflowTask getWorkflowTaskForUpload(String uploadId)
  {
    return AbstractUploadTask.getTaskByUploadId(uploadId);
  }

}