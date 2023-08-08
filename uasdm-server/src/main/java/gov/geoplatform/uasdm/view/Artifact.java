package gov.geoplatform.uasdm.view;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Artifact
{
  private List<SiteObject> objects;

  private boolean          report;

  private String           folder;

  private String[]         extensions;

  public Artifact(String folder, String... extensions)
  {
    this.folder = folder;
    this.extensions = extensions;
    this.report = false;
    this.objects = new LinkedList<SiteObject>();
  }

  public List<SiteObject> getObjects()
  {
    return objects;
  }

  public void setObjects(List<SiteObject> objects)
  {
    this.objects = objects;
  }

  public boolean isReport()
  {
    return report;
  }

  public void setReport(boolean report)
  {
    this.report = report;
  }

  public String getFolder()
  {
    return folder;
  }

  public void setFolder(String folder)
  {
    this.folder = folder;
  }

  public String[] getExtensions()
  {
    return extensions;
  }

  public void setExtensions(String[] extensions)
  {
    this.extensions = extensions;
  }

  public void process(SiteObject object)
  {
    if (object.getKey().contains("/" + this.folder + "/"))
    {
      for (String extension : extensions)
      {
        if (object.getKey().toUpperCase().endsWith(extension))
        {
          this.objects.add(object);

          break;
        }
      }

      if (object.getKey().toUpperCase().endsWith("REPORT.PDF"))
      {
        this.report = true;
      }
    }
  }

  public boolean hasObjects()
  {
    return this.objects.size() > 0;
  }

  public JSONObject toJSON()
  {
    JSONArray items = new JSONArray();

    this.objects.forEach(object -> items.put(object.toJSON()));

    JSONObject object = new JSONObject();
    object.put("report", this.report);
    object.put("items", items);
    return object;
  }
}
