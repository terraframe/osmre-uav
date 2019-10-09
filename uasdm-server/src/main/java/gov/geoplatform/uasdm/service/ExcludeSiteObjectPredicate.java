package gov.geoplatform.uasdm.service;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.json.JSONArray;

import gov.geoplatform.uasdm.view.SiteObject;

public class ExcludeSiteObjectPredicate implements Predicate<SiteObject>
{
  private Set<String> filenames;

  public ExcludeSiteObjectPredicate(JSONArray filenames)
  {
    this.filenames = new TreeSet<String>();

    for (int i = 0; i < filenames.length(); i++)
    {
      this.filenames.add(filenames.getString(i));
    }
  }

  @Override
  public boolean test(SiteObject t)
  {
    return this.filenames.contains(t.getName());
  }
}
