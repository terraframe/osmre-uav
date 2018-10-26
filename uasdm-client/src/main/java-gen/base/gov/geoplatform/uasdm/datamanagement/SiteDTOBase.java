package gov.geoplatform.uasdm.datamanagement;

@com.runwaysdk.business.ClassSignature(hash = 708942801)
public abstract class SiteDTOBase extends gov.geoplatform.uasdm.datamanagement.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.datamanagement.Site";
  private static final long serialVersionUID = 708942801;
  
  protected SiteDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected SiteDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO> getAllProjects()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO> getAllProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.ProjectDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO> getAllProjectsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO> getAllProjectsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO addProjects(gov.geoplatform.uasdm.datamanagement.ProjectDTO child)
  {
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO addProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.datamanagement.ProjectDTO child)
  {
    return (gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public void removeProjects(gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllProjects()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public static void removeAllProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.datamanagement.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.datamanagement.SiteDTO) dto;
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
  
  public static gov.geoplatform.uasdm.datamanagement.SiteQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.datamanagement.SiteQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.datamanagement.SiteDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.SiteDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.SiteDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.datamanagement.SiteDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.datamanagement.SiteDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.datamanagement.SiteDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
