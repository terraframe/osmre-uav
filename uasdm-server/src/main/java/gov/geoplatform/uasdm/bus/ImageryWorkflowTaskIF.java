package gov.geoplatform.uasdm.bus;

import gov.geoplatform.uasdm.view.RequestParser;

import org.json.JSONObject;

import com.runwaysdk.business.Entity;
import com.runwaysdk.dataaccess.DataAccessException;

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
   * If the {@link RequestParser} contains an ID of a {@link UasComponent}, then return the component or return null.
   * 
   * @param parser
   * @return the {@link RequestParser} contains an ID of a {@link UasComponent}, then return the component or return null.
   */
  public static UasComponent getUasComponentFromRequestParser(RequestParser parser)
  {
    if (parser.getUasComponentOid() != null && !parser.getUasComponentOid().trim().equals(""))
    {
      return UasComponent.get(parser.getUasComponentOid());
    }
    else
    {
      return null;
    }
  }

  /**
   * Returns the {@link AbstractWorkflowTask} for the given {@link UasComponent} or null if none exists.
   * 
   * @param uasComponent
   * @param parser
   * @return
   */
  public static AbstractWorkflowTask getWorkflowTaskForComponent(UasComponent uasComponent, RequestParser parser)
  {
    if (uasComponent instanceof Imagery)
    {
      return ImageryWorkflowTask.getTaskByUploadId(parser.getUuid());
    }
    else
    {
      return WorkflowTask.getTaskByUploadId(parser.getUuid());
    }
  }

}
