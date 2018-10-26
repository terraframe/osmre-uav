package gov.geoplatform.uasdm.datamanagement;

@com.runwaysdk.business.ClassSignature(hash = -1417734618)
public abstract class MissionDTOBase extends gov.geoplatform.uasdm.datamanagement.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.datamanagement.Mission";
  private static final long serialVersionUID = -1417734618;
  
  protected MissionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected MissionDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.CollectionDTO> getAllCollections()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.CollectionDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.CollectionDTO> getAllCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.CollectionDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO> getAllCollectionsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO> getAllCollectionsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO addCollections(gov.geoplatform.uasdm.datamanagement.CollectionDTO child)
  {
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO addCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.datamanagement.CollectionDTO child)
  {
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public void removeCollections(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllCollections()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public static void removeAllCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO> getAllProject()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO> getAllProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO> getAllProjectRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO> getAllProjectRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO addProject(gov.geoplatform.uasdm.datamanagement.ProjectDTO parent)
  {
    return (gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO addProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.datamanagement.ProjectDTO parent)
  {
    return (gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public void removeProject(gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllProject()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public static void removeAllProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.datamanagement.ProjectHasMissionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.datamanagement.MissionDTO) dto;
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
  
  public static gov.geoplatform.uasdm.datamanagement.MissionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.datamanagement.MissionQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.datamanagement.MissionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.MissionDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.MissionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.MissionDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.MissionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
