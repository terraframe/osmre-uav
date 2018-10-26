package gov.geoplatform.uasdm.datamanagement;

@com.runwaysdk.business.ClassSignature(hash = 895365189)
public abstract class CollectionDTOBase extends gov.geoplatform.uasdm.datamanagement.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.datamanagement.Collection";
  private static final long serialVersionUID = 895365189;
  
  protected CollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected CollectionDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO> getAllMission()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO> getAllMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO> getAllMissionRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO> getAllMissionRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO addMission(gov.geoplatform.uasdm.datamanagement.MissionDTO parent)
  {
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO addMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.datamanagement.MissionDTO parent)
  {
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public void removeMission(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllMission()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public static void removeAllMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.CollectionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.datamanagement.CollectionDTO) dto;
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
  
  public static gov.geoplatform.uasdm.datamanagement.CollectionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.datamanagement.CollectionQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.datamanagement.CollectionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.CollectionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.CollectionDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.CollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.CollectionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.CollectionDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.CollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
