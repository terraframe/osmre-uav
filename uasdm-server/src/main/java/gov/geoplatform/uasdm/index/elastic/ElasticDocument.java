package gov.geoplatform.uasdm.index.elastic;

public class ElasticDocument
{
  private String oid;

  private String key;

  private String filename;

  private String siteId;

  private String siteName;

  private String projectId;

  private String projectName;

  private String missionId;

  private String missionName;

  private String collectionId;

  private String collectionName;

  private String content;

  private String description;

  private String bureau;

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public String getSiteId()
  {
    return siteId;
  }

  public void setSiteId(String siteId)
  {
    this.siteId = siteId;
  }

  public String getSiteName()
  {
    return siteName;
  }

  public void setSiteName(String siteName)
  {
    this.siteName = siteName;
  }

  public String getProjectId()
  {
    return projectId;
  }

  public void setProjectId(String projectId)
  {
    this.projectId = projectId;
  }

  public String getProjectName()
  {
    return projectName;
  }

  public void setProjectName(String projectName)
  {
    this.projectName = projectName;
  }

  public String getMissionId()
  {
    return missionId;
  }

  public void setMissionId(String missionId)
  {
    this.missionId = missionId;
  }

  public String getMissionName()
  {
    return missionName;
  }

  public void setMissionName(String missionName)
  {
    this.missionName = missionName;
  }

  public String getCollectionId()
  {
    return collectionId;
  }

  public void setCollectionId(String collectionId)
  {
    this.collectionId = collectionId;
  }

  public String getCollectionName()
  {
    return collectionName;
  }

  public void setCollectionName(String collectionName)
  {
    this.collectionName = collectionName;
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public String getBureau()
  {
    return bureau;
  }

  public void setBureau(String bureau)
  {
    this.bureau = bureau;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public void populate(String fieldName, String value)
  {
    if (fieldName.equals("collectionId"))
    {
      this.collectionId = value;
    }
    else if (fieldName.equals("collectionName"))
    {
      this.collectionName = value;
    }
    else if (fieldName.equals("missionId"))
    {
      this.missionId = value;
    }
    else if (fieldName.equals("missionName"))
    {
      this.missionName = value;
    }
    else if (fieldName.equals("projectId"))
    {
      this.projectId = value;
    }
    else if (fieldName.equals("projectName"))
    {
      this.projectName = value;
    }
    else if (fieldName.equals("siteId"))
    {
      this.siteId = value;
    }
    else if (fieldName.equals("siteName"))
    {
      this.siteName = value;
    }
    else
    {
      throw new UnsupportedOperationException("Unknown field value [" + fieldName + "]");
    }
  }

}
