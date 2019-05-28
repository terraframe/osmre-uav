package gov.geoplatform.uasdm.bus;

import java.text.DateFormat;
import java.util.Locale;

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class ImageryWorkflowTask extends ImageryWorkflowTaskBase implements ImageryWorkflowTaskIF
{
  private static final long serialVersionUID = 214749939;
  
  public ImageryWorkflowTask()
  {
    super();
  }
  
  public ImageryComponent getImageryComponent()
  {
    return this.getImagery();
  } 
  
  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);

    JSONObject obj = super.toJSON();
    obj.put("uploadId", this.getUpLoadId());
    obj.put("imagery", this.getImageryOid());
    obj.put("message", this.getMessage());
    obj.put("status", this.getStatus());
    obj.put("lastUpdateDate", format.format(this.getLastUpdateDate()));
    obj.put("createDate", format.format(this.getCreateDate()));

    return obj;
  }

  public static ImageryWorkflowTask getTaskByUploadId(String uploadId)
  {
    ImageryWorkflowTaskQuery query = new ImageryWorkflowTaskQuery(new QueryFactory());
    query.WHERE(query.getUpLoadId().EQ(uploadId));

    OIterator<? extends ImageryWorkflowTask> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }
  
  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return this.getImagery().getName();
  }
}
