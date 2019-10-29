package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1728278482)
public abstract class AbstractUploadTaskDTOBase extends gov.geoplatform.uasdm.bus.AbstractWorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.AbstractUploadTask";
  private static final long serialVersionUID = 1728278482;
  
  protected AbstractUploadTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected AbstractUploadTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String UPLOADID = "uploadId";
  public String getUploadId()
  {
    return getValue(UPLOADID);
  }
  
  public void setUploadId(String value)
  {
    if(value == null)
    {
      setValue(UPLOADID, "");
    }
    else
    {
      setValue(UPLOADID, value);
    }
  }
  
  public boolean isUploadIdWritable()
  {
    return isWritable(UPLOADID);
  }
  
  public boolean isUploadIdReadable()
  {
    return isReadable(UPLOADID);
  }
  
  public boolean isUploadIdModified()
  {
    return isModified(UPLOADID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getUploadIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(UPLOADID).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.AbstractUploadTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.AbstractUploadTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
