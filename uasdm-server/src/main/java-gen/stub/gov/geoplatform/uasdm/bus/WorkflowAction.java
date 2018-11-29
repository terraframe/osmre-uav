package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

public class WorkflowAction extends WorkflowActionBase
{
  private static final long serialVersionUID = -893446595;
  
  public WorkflowAction()
  {
    super();
  }
  
  public JSONObject toJSON()
  {
	JSONObject obj = new JSONObject();
	obj.put("createDate", this.getCreateDate());
	obj.put("lastUpdatedDate", this.getLastUpdateDate());
	obj.put("type", this.getActionType());
	obj.put("description", this.getDescription());
	obj.put("task", this.getWorkflowTask());
	
	return obj;
  }
}
