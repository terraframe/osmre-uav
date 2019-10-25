package gov.geoplatform.uasdm.model;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.Entity;
import com.runwaysdk.dataaccess.DataAccessException;

import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.view.RequestParser;

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
  public static UasComponentIF getOrCreateUasComponentFromRequestParser(RequestParser parser)
  {
    if (parser.getUasComponentOid() != null && !parser.getUasComponentOid().trim().equals(""))
    {
      return UasComponent.get(parser.getUasComponentOid());
    }
    else if (parser.getSelections() != null)
    {
      JSONArray selections = parser.getSelections();

      // The root object will always already be created
      UasComponent component = UasComponent.get(selections.getJSONObject(0).getString("value"));

      for (int i = 1; i < selections.length(); i++)
      {
        JSONObject selection = selections.getJSONObject(i);

        if (selection.getBoolean("isNew"))
        {
          String name = selection.getString("label");

          // Try to find a component with the same name and parent
          UasComponent child = component.getChild(name);

          if (child == null)
          {
            child = component.createChild(selection.getString("type"));
            child.setName(name);

            if (child instanceof Collection && selection.has(Collection.PLATFORM))
            {
              child.setValue(Collection.PLATFORM, selection.getString(Collection.PLATFORM));
            }

            if (child instanceof Collection && selection.has(Collection.SENSOR))
            {
              child.setValue(Collection.SENSOR, selection.getString(Collection.SENSOR));
            }

            child.applyWithParent(component);
          }

          component = child;
        }
        else
        {
          component = UasComponent.get(selection.getString("value"));
        }
      }

      return component;
    }
    else
    {
      return null;
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
  public static AbstractWorkflowTask getWorkflowTaskForUpload(RequestParser parser)
  {
    return AbstractUploadTask.getTaskByUploadId(parser.getUuid());
  }
}
