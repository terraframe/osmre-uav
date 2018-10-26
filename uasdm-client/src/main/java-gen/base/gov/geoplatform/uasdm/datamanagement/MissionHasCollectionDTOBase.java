package gov.geoplatform.uasdm.datamanagement;

@com.runwaysdk.business.ClassSignature(hash = -585100309)
public abstract class MissionHasCollectionDTOBase extends gov.geoplatform.uasdm.datamanagement.ComponentHasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.datamanagement.MissionHasCollection";
  private static final long serialVersionUID = -585100309;
  
  public MissionHasCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String parentOid, java.lang.String childOid)
  {
    super(clientRequest, parentOid, childOid);
    
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given RelationshipDTO into a new DTO.
  * 
  * @param relationshipDTO The RelationshipDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected MissionHasCollectionDTOBase(com.runwaysdk.business.RelationshipDTO relationshipDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(relationshipDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public gov.geoplatform.uasdm.datamanagement.MissionDTO getParent()
  {
    return gov.geoplatform.uasdm.datamanagement.MissionDTO.get(getRequest(), super.getParentOid());
  }
  
    public gov.geoplatform.uasdm.datamanagement.CollectionDTO getChild()
  {
    return gov.geoplatform.uasdm.datamanagement.CollectionDTO.get(getRequest(), super.getChildOid());
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.RelationshipDTO dto = (com.runwaysdk.business.RelationshipDTO) clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) dto;
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionQueryDTO parentQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String parentOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
    queryDTO.addCondition("parent_oid", "EQ", parentOid);
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionQueryDTO) clientRequest.queryRelationships(queryDTO);
  }
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionQueryDTO childQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String childOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS);
    queryDTO.addCondition("child_oid", "EQ", childOid);
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionQueryDTO) clientRequest.queryRelationships(queryDTO);
  }
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createRelationship(this);
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
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.MissionHasCollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
