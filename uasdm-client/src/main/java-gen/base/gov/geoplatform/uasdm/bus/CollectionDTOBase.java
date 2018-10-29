package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -343225131)
public abstract class CollectionDTOBase extends gov.geoplatform.uasdm.bus.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Collection";
  private static final long serialVersionUID = -343225131;
  
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
  public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getAllMission()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getAllMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO> getAllMissionRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO> getAllMissionRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.MissionHasCollectionDTO addMission(gov.geoplatform.uasdm.bus.MissionDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.MissionHasCollectionDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.MissionHasCollectionDTO addMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.MissionDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.MissionHasCollectionDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public void removeMission(gov.geoplatform.uasdm.bus.MissionHasCollectionDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllMission()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static void removeAllMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.CollectionDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.CollectionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.CollectionQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.CollectionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.CollectionDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.CollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.CollectionDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.CollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
