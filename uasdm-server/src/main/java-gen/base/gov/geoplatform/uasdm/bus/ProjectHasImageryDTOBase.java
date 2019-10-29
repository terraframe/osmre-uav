package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 588405468)
public abstract class ProjectHasImageryDTOBase extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.ProjectHasImagery";
  private static final long serialVersionUID = 588405468;
  
  public ProjectHasImageryDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String parentOid, java.lang.String childOid)
  {
    super(clientRequest, parentOid, childOid);
    
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given RelationshipDTO into a new DTO.
  * 
  * @param relationshipDTO The RelationshipDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ProjectHasImageryDTOBase(com.runwaysdk.business.RelationshipDTO relationshipDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(relationshipDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public gov.geoplatform.uasdm.bus.ProjectDTO getParent()
  {
    return gov.geoplatform.uasdm.bus.ProjectDTO.get(getRequest(), super.getParentOid());
  }
  
    public gov.geoplatform.uasdm.bus.ImageryDTO getChild()
  {
    return gov.geoplatform.uasdm.bus.ImageryDTO.get(getRequest(), super.getChildOid());
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.RelationshipDTO dto = (com.runwaysdk.business.RelationshipDTO) clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) dto;
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryQueryDTO parentQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String parentOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
    queryDTO.addCondition("parent_oid", "EQ", parentOid);
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryQueryDTO) clientRequest.queryRelationships(queryDTO);
  }
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryQueryDTO childQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String childOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
    queryDTO.addCondition("child_oid", "EQ", childOid);
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryQueryDTO) clientRequest.queryRelationships(queryDTO);
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
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
