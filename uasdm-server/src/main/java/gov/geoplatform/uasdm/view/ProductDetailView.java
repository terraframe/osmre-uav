package gov.geoplatform.uasdm.view;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.bus.Document;

public class ProductDetailView extends ProductView
{
  private List<Document> documents;

  private String         pilotName;

  private Date           dateTime;

  private String         sensor;

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

  public List<Document> getDocuments()
  {
    return documents;
  }

  public void setDocuments(List<Document> documents)
  {
    this.documents = documents;
  }

  public JSONObject toJSON()
  {
    JSONArray array = new JSONArray();

    for (Document document : documents)
    {
      array.put(document.toJSON());
    }

    JSONObject object = super.toJSON();
    object.put("pilotName", this.pilotName);
    object.put("dateTime", this.dateTime);
    object.put("sensor", this.sensor);
    object.put("documents", array);

    return object;
  }
}
