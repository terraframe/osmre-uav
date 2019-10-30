package gov.geoplatform.uasdm.bus;

import java.text.DateFormat;
import java.util.Locale;

import org.json.JSONObject;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ImageryIF;
import gov.geoplatform.uasdm.model.ImageryWorkflowTaskIF;

public class ImageryWorkflowTask extends ImageryWorkflowTaskBase implements ImageryWorkflowTaskIF
{
  private static final long serialVersionUID = 214749939;

  public ImageryWorkflowTask()
  {
    super();
  }

  public ImageryComponent getImageryComponent()
  {
    return this.getImageryInstance();
  }

  public ImageryIF getImageryInstance()
  {
    return ComponentFacade.getImagery(this.getImagery());
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUploadId());
    obj.put("imagery", this.getImagery());
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
    return this.getImageryInstance().getName();
  }
}
