package gov.geoplatform.uasdm.datamanagement;

@com.runwaysdk.business.ClassSignature(hash = -2079729473)
public abstract class ProjectDTOBase extends gov.geoplatform.uasdm.datamanagement.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.datamanagement.Project";
  private static final long serialVersionUID = -2079729473;
  
  protected ProjectDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ProjectDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO> getAllMissions()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO> getAllMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO> getAllMissionsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO> getAllMissionsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO addMissions(gov.geoplatform.uasdm.datamanagement.MissionDTO child)
  {
    return (gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO addMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.datamanagement.MissionDTO child)
  {
    return (gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public void removeMissions(gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllMissions()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public static void removeAllMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteDTO> getAllSite()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteDTO> getAllSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO> getAllSiteRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO> getAllSiteRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO addSite(gov.geoplatform.uasdm.datamanagement.SiteDTO parent)
  {
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO addSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.datamanagement.SiteDTO parent)
  {
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public void removeSite(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllSite()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public static void removeAllSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.ProjectDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.datamanagement.ProjectDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createBusiness(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
  public static gov.geoplatform.uasdm.datamanagement.ProjectQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.datamanagement.ProjectQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.datamanagement.ProjectDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.ProjectDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.ProjectDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.ProjectDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.ProjectDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.ProjectDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.ProjectDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
