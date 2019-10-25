package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1014746709)
public abstract class ImageryDTOBase extends gov.geoplatform.uasdm.bus.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Imagery";
  public static final long serialVersionUID = 1014746709;
  
  protected ImageryDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImageryDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO> getAllProject()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO> getAllProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO> getAllProjectRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO> getAllProjectRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.ProjectHasImageryDTO addProject(gov.geoplatform.uasdm.bus.ProjectDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryDTO addProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.ProjectDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public void removeProject(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllProject()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public static void removeAllProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.ImageryDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.ImageryQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.ImageryQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.ImageryDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ImageryDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ImageryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ImageryDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ImageryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
