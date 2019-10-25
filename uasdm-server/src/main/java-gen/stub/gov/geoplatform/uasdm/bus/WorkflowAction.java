package gov.geoplatform.uasdm.bus;

import java.text.DateFormat;
import java.util.Locale;

import org.json.JSONObject;

public class WorkflowAction extends WorkflowActionBase
{
  public static final long serialVersionUID = -893446595;

  public WorkflowAction()
  {
    super();
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    JSONObject obj = new JSONObject();
    obj.put("createDate", format.format(this.getCreateDate()));
    obj.put("lastUpdatedDate", format.format(this.getLastUpdateDate()));
    obj.put("type", this.getActionType());
    obj.put("description", this.getDescription());

    return obj;
  }
}
