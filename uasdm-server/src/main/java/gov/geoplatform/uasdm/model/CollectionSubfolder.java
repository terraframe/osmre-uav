package gov.geoplatform.uasdm.model;

public enum CollectionSubfolder
{
  RAW("raw"),
  VIDEO("video"),
  PTCLOUD("ptcloud"),
  INACCESSIBLE_SUPPORT("inaccessiblesupport"),
  ACCESSIBLE_SUPPORT("accessiblesupport"),
  DEM("dem"),
  ORTHO("ortho"),
  GEOREF("georef"),
  ODM_ALL("odm_all"),
  ODM_ALL_GDAL("odm_all/gdal"),
  ODM_ALL_POTREE("odm_all/potree");
  
  private String folderName;
  
  private CollectionSubfolder(String folderName)
  {
    this.folderName = folderName;
  }

  public String getFolderName()
  {
    return folderName;
  }

  public void setFolderName(String folderName)
  {
    this.folderName = folderName;
  }
}
