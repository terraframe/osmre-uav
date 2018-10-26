package gov.geoplatform.uasdm.datamanagement;

@com.runwaysdk.business.ClassSignature(hash = 56984704)
public abstract class SiteHasProjectsDTOBase extends gov.geoplatform.uasdm.datamanagement.ComponentHasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.datamanagement.SiteHasProjects";
  private static final long serialVersionUID = 56984704;
  
  public SiteHasProjectsDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String parentOid, java.lang.String childOid)
  {
    super(clientRequest, parentOid, childOid);
    
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given RelationshipDTO into a new DTO.
  * 
  * @param relationshipDTO The RelationshipDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected SiteHasProjectsDTOBase(com.runwaysdk.business.RelationshipDTO relationshipDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(relationshipDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public gov.geoplatform.uasdm.datamanagement.SiteDTO getParent()
  {
    return gov.geoplatform.uasdm.datamanagement.SiteDTO.get(getRequest(), super.getParentOid());
  }
  
    public gov.geoplatform.uasdm.datamanagement.ProjectDTO getChild()
  {
    return gov.geoplatform.uasdm.datamanagement.ProjectDTO.get(getRequest(), super.getChildOid());
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.RelationshipDTO dto = (com.runwaysdk.business.RelationshipDTO) clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) dto;
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsQueryDTO parentQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String parentOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
    queryDTO.addCondition("parent_oid", "EQ", parentOid);
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsQueryDTO) clientRequest.queryRelationships(queryDTO);
  }
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsQueryDTO childQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String childOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
    queryDTO.addCondition("child_oid", "EQ", childOid);
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsQueryDTO) clientRequest.queryRelationships(queryDTO);
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
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
