package gov.geoplatform.uasdm.bus;

import java.text.DateFormat;
import java.util.Locale;

import org.json.JSONObject;

import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;

public class WorkflowTask extends WorkflowTaskBase implements ImageryWorkflowTaskIF
{
  private static final long  serialVersionUID = 1976980729;

  public static final String NEEDS_METADATA   = "NEEDS_METADATA";

  public WorkflowTask()
  {
    super();
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUploadId());
    obj.put("collection", this.getComponent());
    obj.put("message", this.getMessage());
    obj.put("status", this.getStatus());
    obj.put("lastUpdateDate", format.format(this.getLastUpdateDate()));
    obj.put("createDate", format.format(this.getCreateDate()));

    return obj;
  }

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return this.getImageryComponent().getName();
  }

  public ImageryComponent getImageryComponent()
  {
    return (ImageryComponent) getComponentInstance();
  }

  public UasComponent getComponentInstance()
  {
    return UasComponent.get(this.getComponent());
  }
}
