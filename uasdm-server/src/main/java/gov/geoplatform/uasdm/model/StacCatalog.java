package gov.geoplatform.uasdm.model;

import java.util.List;

/*
 * This document explains the structure and content of a STAC Catalog object. A
 * STAC Catalog object represents a logical group of other Catalog, Collection,
 * and Item objects. These Items can be linked to directly from a Catalog, or
 * the Catalog can link to other Catalogs (often called sub-catalogs) that
 * contain links to Collections and Items. The division of sub-catalogs is up to
 * the implementor, but is generally done to aid the ease of online browsing by
 * people.
 */
public class StacCatalog
{

  // string REQUIRED. Set to Catalog if this Catalog only implements the Catalog
  // spec.
  private String type;

  // string REQUIRED. The STAC version the Catalog implements.
  private String stac_version;

  // [string] A list of extension identifiers the Catalog implements.
  private List<String> stac_extensions;

  // string REQUIRED. Identifier for the Catalog.
  private String id;

  // string A short descriptive one-line title for the Catalog.
  private String title;

  // string REQUIRED. Detailed multi-line description to fully explain the
  // Catalog. CommonMark 0.29 syntax MAY be used for rich text representation.
  private String description;

  // [Link Object] REQUIRED. A list of references to other documents.
  private List<StacLink> links;

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getStac_version()
  {
    return stac_version;
  }

  public void setStac_version(String stac_version)
  {
    this.stac_version = stac_version;
  }

  public List<String> getStac_extensions()
  {
    return stac_extensions;
  }

  public void setStac_extensions(List<String> stac_extensions)
  {
    this.stac_extensions = stac_extensions;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public List<StacLink> getLinks()
  {
    return links;
  }

  public void setLinks(List<StacLink> links)
  {
    this.links = links;
  }

}
