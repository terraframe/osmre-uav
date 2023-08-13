package gov.geoplatform.uasdm.view;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import gov.geoplatform.uasdm.Util;

public class Artifact
{
  private List<SiteObject> objects;

  private boolean          report;

  private String           folder;

  private String[]         extensions;

  private String           ptEpsg;

  private Date             startDate;

  private Date             endDate;

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

    objects.stream().map(a -> a.getPtEpsg()).filter(a -> a != null).findAny().ifPresent(a -> this.setPtEpsg(a));
    objects.stream().map(a -> a.getStartDate()).filter(a -> a != null).findAny().ifPresent(a -> this.setStartDate(a));
    objects.stream().map(a -> a.getEndDate()).filter(a -> a != null).findAny().ifPresent(a -> this.setEndDate(a));
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

  public String getPtEpsg()
  {
    return ptEpsg;
  }

  public void setPtEpsg(String ptEpsg)
  {
    this.ptEpsg = ptEpsg;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
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

    objects.stream().map(a -> a.getPtEpsg()).filter(a -> a != null).findAny().ifPresent(a -> this.setPtEpsg(a));
    objects.stream().map(a -> a.getStartDate()).filter(a -> a != null).findAny().ifPresent(a -> this.setStartDate(a));
    objects.stream().map(a -> a.getEndDate()).filter(a -> a != null).findAny().ifPresent(a -> this.setEndDate(a));
  }

  public boolean hasObjects()
  {
    return this.objects.size() > 0;
  }

  public JSONObject toJSON()
  {
    return this.toJSON(true);
  }

  public JSONObject toJSON(boolean includeItems)
  {
    JSONArray items = new JSONArray();

    this.objects.forEach(object -> items.put(object.toJSON()));

    JSONObject object = new JSONObject();
    object.put("report", this.report);
    object.put("folder", this.folder);

    if (includeItems)
    {
      object.put("items", items);
    }

    if (ptEpsg != null)
    {
      object.put("ptEpsg", this.ptEpsg);
    }

    if (this.startDate != null)
    {
      object.put("startDate", Util.formatIso8601(this.startDate, false));
    }

    if (this.endDate != null)
    {
      object.put("endDate", Util.formatIso8601(this.endDate, false));
    }

    return object;
  }
}
