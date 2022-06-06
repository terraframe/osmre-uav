package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1076751601)
public abstract class MissingUploadMessageDTOBase extends gov.geoplatform.uasdm.bus.AbstractMessageDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.MissingUploadMessage";
  private static final long serialVersionUID = 1076751601;
  
  protected MissingUploadMessageDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected MissingUploadMessageDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String COMPONENT = "component";
  public String getComponent()
  {
    return getValue(COMPONENT);
  }
  
  public void setComponent(String value)
  {
    if(value == null)
    {
      setValue(COMPONENT, "");
    }
    else
    {
      setValue(COMPONENT, value);
    }
  }
  
  public boolean isComponentWritable()
  {
    return isWritable(COMPONENT);
  }
  
  public boolean isComponentReadable()
  {
    return isReadable(COMPONENT);
  }
  
  public boolean isComponentModified()
  {
    return isModified(COMPONENT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeUUIDMdDTO getComponentMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeUUIDMdDTO) getAttributeDTO(COMPONENT).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.MissingUploadMessageDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.MissingUploadMessageDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.MissingUploadMessageQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.MissingUploadMessageQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.MissingUploadMessageDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.MissingUploadMessageDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.MissingUploadMessageDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.MissingUploadMessageDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.MissingUploadMessageDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.MissingUploadMessageDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.MissingUploadMessageDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
