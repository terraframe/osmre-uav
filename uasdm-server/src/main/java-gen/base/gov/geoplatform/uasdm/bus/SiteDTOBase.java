package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -106457095)
public abstract class SiteDTOBase extends gov.geoplatform.uasdm.bus.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Site";
  private static final long serialVersionUID = -106457095;
  
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
  
  public static java.lang.String BUREAU = "bureau";
  public static java.lang.String OTHERBUREAUTXT = "otherBureauTxt";
  public gov.geoplatform.uasdm.bus.BureauDTO getBureau()
  {
    if(getValue(BUREAU) == null || getValue(BUREAU).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.BureauDTO.get(getRequest(), getValue(BUREAU));
    }
  }
  
  public String getBureauOid()
  {
    return getValue(BUREAU);
  }
  
  public void setBureau(gov.geoplatform.uasdm.bus.BureauDTO value)
  {
    if(value == null)
    {
      setValue(BUREAU, "");
    }
    else
    {
      setValue(BUREAU, value.getOid());
    }
  }
  
  public boolean isBureauWritable()
  {
    return isWritable(BUREAU);
  }
  
  public boolean isBureauReadable()
  {
    return isReadable(BUREAU);
  }
  
  public boolean isBureauModified()
  {
    return isModified(BUREAU);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getBureauMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(BUREAU).getAttributeMdDTO();
  }
  
  public String getOtherBureauTxt()
  {
    return getValue(OTHERBUREAUTXT);
  }
  
  public void setOtherBureauTxt(String value)
  {
    if(value == null)
    {
      setValue(OTHERBUREAUTXT, "");
    }
    else
    {
      setValue(OTHERBUREAUTXT, value);
    }
  }
  
  public boolean isOtherBureauTxtWritable()
  {
    return isWritable(OTHERBUREAUTXT);
  }
  
  public boolean isOtherBureauTxtReadable()
  {
    return isReadable(OTHERBUREAUTXT);
  }
  
  public boolean isOtherBureauTxtModified()
  {
    return isModified(OTHERBUREAUTXT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getOtherBureauTxtMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(OTHERBUREAUTXT).getAttributeMdDTO();
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO> getAllProjects()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO> getAllProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO> getAllProjectsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO> getAllProjectsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.SiteHasProjectsDTO addProjects(gov.geoplatform.uasdm.bus.ProjectDTO child)
  {
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsDTO addProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.ProjectDTO child)
  {
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public void removeProjects(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllProjects()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public static void removeAllProjects(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.SiteDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.SiteQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.SiteQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.SiteDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.SiteDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.SiteDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.SiteDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.SiteDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}