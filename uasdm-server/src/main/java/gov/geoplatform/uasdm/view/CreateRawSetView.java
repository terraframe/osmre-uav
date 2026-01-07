package gov.geoplatform.uasdm.view;

import java.util.List;

public class CreateRawSetView
{
  private String       collectionId;

  private String       name;

  private List<String> files;

  public String getCollectionId()
  {
    return collectionId;
  }

  public void setCollectionId(String collectionId)
  {
    this.collectionId = collectionId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public List<String> getFiles()
  {
    return files;
  }

  public void setFiles(List<String> files)
  {
    this.files = files;
  }

}
