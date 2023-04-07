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
package gov.geoplatform.uasdm.model;

import java.math.BigDecimal;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.Entity;
import com.runwaysdk.dataaccess.DataAccessException;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.Util;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.MissingUploadMessage;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Mission;
import gov.geoplatform.uasdm.graph.Project;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.view.RequestParser;
import gov.geoplatform.uasdm.view.RequestParserIF;
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

  /**
   * If the {@link RequestParser} contains an ID of a {@link UasComponent}, then
   * return the component or return null.
   * 
   * @param parser
   * @return the {@link RequestParser} contains an ID of a {@link UasComponent},
   *         then return the component or return null.
   */
  public static UasComponentIF getOrCreateUasComponentFromRequestParser(RequestParserIF parser)
  {
    if (parser.getUasComponentOid() != null && !parser.getUasComponentOid().trim().equals(""))
    {
      return ComponentFacade.getComponent(parser.getUasComponentOid());
    }
    else if (parser.getSelections() != null)
    {
      JSONArray selections = parser.getSelections();

      return createUasComponent(selections);
    }
    else
    {
      return null;
    }
  }

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
            if (selection.has(Collection.EXIFINCLUDED))
            {
              child.setValue(Collection.EXIFINCLUDED, selection.getBoolean(Collection.EXIFINCLUDED));
            }

            setDecimalValue(selection, child, Collection.NORTHBOUND);
            setDecimalValue(selection, child, Collection.SOUTHBOUND);
            setDecimalValue(selection, child, Collection.EASTBOUND);
            setDecimalValue(selection, child, Collection.WESTBOUND);
            setDateValue(selection, child, Collection.ACQUISITIONDATESTART);
            setDateValue(selection, child, Collection.ACQUISITIONDATEEND);
            setDateValue(selection, child, Collection.COLLECTIONDATE);

            if (selection.has(Collection.UAV))
            {
              child.setValue(Collection.UAV, selection.getString(Collection.UAV));
            }

            if (selection.has(Collection.SENSOR))
            {
              child.setValue(Collection.COLLECTIONSENSOR, selection.getString(Collection.SENSOR));
            }

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

          }

          child.applyWithParent(component);

          // Upload the metadata file
          if (child instanceof CollectionIF)
          {
            new MetadataXMLGenerator().generateAndUpload((CollectionIF) child);

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

  public static void setDateValue(JSONObject selection, UasComponentIF child, String attributeName)
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

  public static void setDecimalValue(JSONObject selection, UasComponentIF child, String attributeName)
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

  /**
   * Returns the {@link AbstractWorkflowTask} for the given {@link UasComponent}
   * or null if none exists.
   * 
   * @param parser
   * 
   * @return
   */
  public static AbstractWorkflowTask getWorkflowTaskForUpload(RequestParserIF parser)
  {
    return AbstractUploadTask.getTaskByUploadId(parser.getUuid());
  }
}