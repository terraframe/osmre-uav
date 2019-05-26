package gov.geoplatform.uasdm.bus;

public interface AbstractWorkflowTaskIF
{
  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel();
  
  /**
   * {@link AbstractWorkflowTask#createAction()}
   */
  public void createAction(String message, String type);
  
  /**
   * {@link AbstractWorkflowTask#getGeoprismUser()}
   */
  public net.geoprism.GeoprismUser getGeoprismUser();

  /**
   * {@link AbstractWorkflowTask#getMessage(String)}
   */
  public String getMessage();
  
  /**
   * {@link AbstractWorkflowTask#setMessage(String)}
   * @param value
   */
  public void setMessage(String value);
  
  /**
   * {@link AbstractWorkflowTask#getStatus(String)}
   */
  public String getStatus();
  
  /**
   * {@link AbstractWorkflowTask#setStatus(String)}
   * @param value
   */
  public void setStatus(String value);
  
  /**
   * {@link AbstractWorkflowTask#getTaskLabel(String)}
   */
  public String getTaskLabel();

  /**
   * {@link Entity#apply()}
   */
  public void apply();
  
  /**
   * {@link Element#lock()}
   */
  public void lock();
}
