package gov.geoplatform.uasdm.view;

import java.util.Date;

import org.json.JSONObject;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.Page;

public class ProductDetailView extends ProductView
{
  private Page<DocumentIF> page;

  private String           pilotName;

  private Date             dateTime;

  private String           sensor;

  public String getPilotName()
  {
    return pilotName;
  }

  public void setPilotName(String pilotName)
  {
    this.pilotName = pilotName;
  }

  public Date getDateTime()
  {
    return dateTime;
  }

  public void setDateTime(Date dateTime)
  {
    this.dateTime = dateTime;
  }

  public String getSensor()
  {
    return sensor;
  }

  public void setSensor(String sensor)
  {
    this.sensor = sensor;
  }

  public Page<DocumentIF> getPage()
  {
    return page;
  }

  public void setPage(Page<DocumentIF> page)
  {
    this.page = page;
  }

  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    object.put("pilotName", this.pilotName);
    object.put("dateTime", this.dateTime);
    object.put("sensor", this.sensor);
    object.put("page", page.toJSON());

    return object;
  }
}
