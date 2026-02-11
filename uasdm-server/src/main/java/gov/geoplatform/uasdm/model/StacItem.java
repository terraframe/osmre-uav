/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.serialization.DateDeserializer;
import gov.geoplatform.uasdm.serialization.DateSerializer;
import gov.geoplatform.uasdm.serialization.EnvelopeDeserializer;
import gov.geoplatform.uasdm.serialization.EnvelopeSerializer;
import gov.geoplatform.uasdm.serialization.GeoJsonDeserializer;
import gov.geoplatform.uasdm.serialization.GeoJsonSerializer;

/*
 * This document explains the structure and content of a SpatioTemporal Asset
 * Catalog (STAC) Item. An Item is a GeoJSON Feature augmented with foreign
 * members relevant to a STAC object. These include fields that identify the
 * time range and assets of the Item. An Item is the core object in a STAC
 * Catalog, containing the core metadata that enables any client to search or
 * crawl online catalogs of spatial 'assets' (e.g., satellite imagery, derived
 * data, DEMs). Version 1.0.0.
 */
// @JsonSerialize(using = StacItemSerializer.class)
@JsonPropertyOrder({ "stacVersion", "stacExtensions", "type", "id", "bbox", "geometry", "properties", "collection", "links", "assets" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class StacItem implements JSONSerializable
{
  private static final String NAMESPACE = "geoplatform";

  private static final String EXTENSION = "https://raw.githubusercontent.com/terraframe/osmre-uav/refs/heads/master/geoplatform/v0.1.0/schema.json";

  @JsonPropertyOrder({ "maximum", "mean", "minimum", "stddev", "validPercent" })
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Statistics
  {
    private Double maximum;

    private Double mean;

    private Double minimum;

    private Double stddev;

    @JsonProperty("valid_percent")
    private Float  validPercent;

    public Double getMaximum()
    {
      return maximum;
    }

    public void setMaximum(Double maximum)
    {
      this.maximum = maximum;
    }

    public Double getMean()
    {
      return mean;
    }

    public void setMean(Double mean)
    {
      this.mean = mean;
    }

    public Double getMinimum()
    {
      return minimum;
    }

    public void setMinimum(Double minimum)
    {
      this.minimum = minimum;
    }

    public Double getStddev()
    {
      return stddev;
    }

    public void setStddev(Double stddev)
    {
      this.stddev = stddev;
    }

    public Float getValidPercent()
    {
      return validPercent;
    }

    public void setValidPercent(Float validPercent)
    {
      this.validPercent = validPercent;
    }

  }

  @JsonPropertyOrder({ "name", "statistics" })
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Band
  {
    private String     name;

    private Statistics statistics;

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public Statistics getStatistics()
    {
      return statistics;
    }

    public void setStatistics(Statistics statistics)
    {
      this.statistics = statistics;
    }
  }

  @JsonPropertyOrder({ "href", "type", "title", "description", "roles" })
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Asset
  {
    // string REQUIRED. URI to the asset object. Relative and absolute URI are
    // both allowed.
    private String       href;

    // string The displayed title for clients and users.
    @JsonInclude(Include.NON_NULL)
    private String       title;

    // string A description of the Asset providing additional details, such as
    // how it was processed or created. CommonMark 0.29 syntax MAY be used for
    // rich text representation.
    @JsonInclude(Include.NON_NULL)
    private String       description;

    // string Media type of the asset. See the common media types in the best
    // practice doc for commonly used asset types.
    @JsonInclude(Include.NON_NULL)
    private String       type;

    // [string] The semantic roles of the asset, similar to the use of rel in
    // links.
    /*
     * Like the Link rel field, the roles field can be given any value, however
     * here are a few standardized role names.
     * 
     * thumbnail - An asset that represents a thumbnail of the Item, typically a
     * true color image (for Items with assets in the visible wavelengths),
     * lower-resolution (typically smaller 600x600 pixels), and typically a JPEG
     * or PNG (suitable for display in a web browser). Multiple assets may have
     * this purpose, but it recommended that the type and roles be unique
     * tuples. For example, Sentinel-2 L2A provides thumbnail images in both
     * JPEG and JPEG2000 formats, and would be distinguished by their media
     * types.
     * 
     * overview - An asset that represents a possibly larger view than the
     * thumbnail of the Item, for example, a true color composite of multi-band
     * data.
     * 
     * data - The data itself. This is a suggestion for a common role for data
     * files to be used in case data providers don't come up with their own
     * names and semantics.
     * 
     * metadata - A metadata sidecar file describing the data in this Item, for
     * example the Landsat-8 MTL file.
     */
    @JsonInclude(Include.NON_NULL)
    private List<String> roles;

    private List<Band>   bands;

    public Asset()
    {
      this.roles = new LinkedList<String>();
      this.bands = new LinkedList<Band>();
    }

    public String getHref()
    {
      return href;
    }

    public void setHref(String href)
    {
      this.href = href;
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

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public List<String> getRoles()
    {
      return roles;
    }

    public void setRoles(List<String> roles)
    {
      this.roles = roles;
    }

    public void addRole(String role)
    {
      this.roles.add(role);
    }

    public List<Band> getBands()
    {
      return bands;
    }

    public void setBands(List<Band> bands)
    {
      this.bands = bands;
    }

    public void addBand(Band band)
    {
      this.bands.add(band);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties
  {
    @JsonDeserialize(using = DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    private Date                   datetime;

    @JsonInclude(Include.NON_NULL)
    private String                 title;

    @JsonInclude(Include.NON_NULL)
    private String                 description;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("platform")
    private String                 platform;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(NAMESPACE + ":sensor")
    private String                 sensor;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(NAMESPACE + ":collection")
    private String                 collection;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("mission")
    private String                 mission;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(NAMESPACE + ":project")
    private String                 project;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(NAMESPACE + ":site")
    private String                 site;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(NAMESPACE + ":faa_number")
    private String                 faaNumber;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(NAMESPACE + ":serial_number")
    private String                 serialNumber;

    @JsonProperty(NAMESPACE + ":operational")
    private List<StacLocation>     operational;

    @JsonProperty(NAMESPACE + ":agency")
    private List<StacOrganization> agency;

    public Date getDatetime()
    {
      return datetime;
    }

    public void setDatetime(Date datetime)
    {
      this.datetime = datetime;
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

    public String getPlatform()
    {
      return platform;
    }

    public void setPlatform(String platform)
    {
      this.platform = platform;
    }

    public String getSensor()
    {
      return sensor;
    }

    public void setSensor(String sensor)
    {
      this.sensor = sensor;
    }

    public String getCollection()
    {
      return collection;
    }

    public void setCollection(String collection)
    {
      this.collection = collection;
    }

    public String getMission()
    {
      return mission;
    }

    public void setMission(String mission)
    {
      this.mission = mission;
    }

    public String getProject()
    {
      return project;
    }

    public void setProject(String project)
    {
      this.project = project;
    }

    public String getSite()
    {
      return site;
    }

    public void setSite(String site)
    {
      this.site = site;
    }

    public String getFaaNumber()
    {
      return faaNumber;
    }

    public void setFaaNumber(String faaNumber)
    {
      this.faaNumber = faaNumber;
    }

    public String getSerialNumber()
    {
      return serialNumber;
    }

    public void setSerialNumber(String serialNumber)
    {
      this.serialNumber = serialNumber;
    }

    public List<StacOrganization> getAgency()
    {
      return agency;
    }

    public void setAgency(List<StacOrganization> agency)
    {
      this.agency = agency;
    }

    public List<StacLocation> getOperational()
    {
      return operational;
    }

    public void setOperational(List<StacLocation> operational)
    {
      this.operational = operational;
    }

    public void set(String fieldName, String value)
    {
      if (fieldName.equals("siteName"))
      {
        this.site = value;
      }
      else if (fieldName.equals("projectName"))
      {
        this.project = value;
      }
      else if (fieldName.equals("missionName"))
      {
        this.mission = value;
      }
      else if (fieldName.equals("collectionName"))
      {
        this.collection = value;
      }
      else
      {
        throw new UnsupportedOperationException("Unsupport field [" + fieldName + "]");
      }
    }

  }

  // string REQUIRED. Type of the GeoJSON Object. MUST be set to Feature.
  private String             type;

  // string REQUIRED. The STAC version the Item implements.
  @JsonProperty("stac_version")
  private String             stacVersion;

  // [string] A list of extensions the Item implements.
  @JsonProperty("stac_extensions")
  private List<String>       stacExtensions;

  // string REQUIRED. Provider identifier. The ID should be unique within the
  // Collection that contains the Item.
  private String             id;

  // GeoJSON Geometry Object | null REQUIRED. Defines the full footprint of the
  // asset represented by this item, formatted according to RFC 7946, section
  // 3.1. The footprint should be the default GeoJSON geometry, though
  // additional geometries can be included. Coordinates are specified in
  // Longitude/Latitude or Longitude/Latitude/Elevation based on WGS 84.
  @JsonDeserialize(using = GeoJsonDeserializer.class)
  @JsonSerialize(using = GeoJsonSerializer.class)
  private Geometry           geometry;

  // [number] REQUIRED if geometry is not null. Bounding Box of the asset
  // represented by this Item, formatted according to RFC 7946, section 5.
  @JsonDeserialize(using = EnvelopeDeserializer.class)
  @JsonSerialize(using = EnvelopeSerializer.class)
  private Envelope           bbox;

  // Properties Object REQUIRED. A dictionary of additional metadata for the
  // Item.
  private Properties         properties;

  // [Link Object] REQUIRED. List of link objects to resources and related URLs.
  // A link with the rel set to self is strongly recommended.
  private List<StacLink>     links;

  // Map<string, Asset Object> REQUIRED. Dictionary of asset objects that can be
  // downloaded, each with a unique key.
  private Map<String, Asset> assets;

  // string The id of the STAC Collection this Item references to (see
  // collection relation type). This field is required if such a relation type
  // is present and is not allowed otherwise. This field provides an easy way
  // for a user to search for any Items that belong in a specified Collection.
  // Must be a non-empty string.
  @JsonInclude(Include.NON_NULL)
  private String             collection;

  @JsonIgnore
  private boolean            published;

  public StacItem()
  {
    this.published = false;
    this.type = "Feature";
    this.stacVersion = "1.1.0";
    this.properties = new Properties();
    this.links = new LinkedList<StacLink>();
    this.assets = new TreeMap<String, StacItem.Asset>();
    this.stacExtensions = new LinkedList<String>();
    this.stacExtensions.add(EXTENSION);
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getStacVersion()
  {
    return stacVersion;
  }

  public void setStacVersion(String stacVersion)
  {
    this.stacVersion = stacVersion;
  }

  public List<String> getStacExtensions()
  {
    return stacExtensions;
  }

  public void setStacExtensions(List<String> stacExtensions)
  {
    this.stacExtensions = stacExtensions;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public Geometry getGeometry()
  {
    return geometry;
  }

  public void setGeometry(Geometry geometry)
  {
    this.geometry = geometry;
  }

  public Envelope getBbox()
  {
    return bbox;
  }

  public void setBbox(Envelope bbox)
  {
    this.bbox = bbox;
  }

  public Properties getProperties()
  {
    return properties;
  }

  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  public List<StacLink> getLinks()
  {
    return links;
  }

  public void setLinks(List<StacLink> links)
  {
    this.links = links;
  }

  public void addLink(StacLink link)
  {
    this.links.add(link);
  }

  public Map<String, Asset> getAssets()
  {
    return assets;
  }

  public void setAssets(Map<String, Asset> assets)
  {
    this.assets = assets;
  }

  public void addAsset(String name, Asset asset)
  {
    this.assets.put(name, asset);
  }

  public void removeAsset(String name)
  {
    this.assets.remove(name);
  }

  public String getCollection()
  {
    return collection;
  }

  public void setCollection(String collection)
  {
    this.collection = collection;
  }

  public boolean isPublished()
  {
    return published;
  }

  public void setPublished(boolean published)
  {
    this.published = published;
  }

  @Override
  public Object toJSON()
  {
    try
    {
      ObjectMapper mapper = new ObjectMapper();
      String str = mapper.writeValueAsString(this);

      return new JSONObject(str);
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static Asset buildAsset(String type, String title, String href, String... roles)
  {
    Asset asset = new Asset();
    asset.setType(type);
    asset.setTitle(title);
    asset.setHref(href);

    for (String role : roles)
    {
      asset.addRole(role);
    }

    return asset;
  }

  public static Statistics buildStatistics(Double maximum, Double mean, Double minimum, Double stddev, Float validPercent)
  {
    Statistics statistics = new Statistics();
    statistics.setMaximum(maximum);
    statistics.setMean(mean);
    statistics.setMinimum(minimum);
    statistics.setStddev(stddev);
    statistics.setValidPercent(validPercent);

    return statistics;
  }

  public static Band buildBand(String name, Statistics statistics)
  {
    Band band = new Band();
    band.setName(name);
    band.setStatistics(statistics);

    return band;
  }

  public static Asset buildAsset(String type, String title, String href, List<Band> bands, String... roles)
  {
    Asset asset = new Asset();
    asset.setType(type);
    asset.setTitle(title);
    asset.setHref(href);

    if (bands.size() > 0)
    {
      bands.stream().forEach(asset::addBand);
    }
    else
    {
      asset.setBands(null);
    }

    for (String role : roles)
    {
      asset.addRole(role);
    }

    return asset;
  }
}
