package gov.geoplatform.uasdm.view;

public class AssignImageSetView
{
  private Boolean isNew;

  private String  collectionId;

  private String  name;

  private String  documentId;

  public AssignImageSetView()
  {
    this.isNew = false;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

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

  public String getDocumentId()
  {
    return documentId;
  }

  public void setDocumentId(String documentId)
  {
    this.documentId = documentId;
  }

}
