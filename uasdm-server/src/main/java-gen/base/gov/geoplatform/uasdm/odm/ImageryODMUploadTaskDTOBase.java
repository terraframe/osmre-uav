package gov.geoplatform.uasdm.odm;

@com.runwaysdk.business.ClassSignature(hash = 865295135)
public abstract class ImageryODMUploadTaskDTOBase extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.odm.ImageryODMUploadTask";
  private static final long serialVersionUID = 865295135;
  
  protected ImageryODMUploadTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImageryODMUploadTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ODMUUID = "odmUUID";
  public static java.lang.String PROCESSINGTASK = "processingTask";
  public String getOdmUUID()
  {
    return getValue(ODMUUID);
  }
  
  public void setOdmUUID(String value)
  {
    if(value == null)
    {
      setValue(ODMUUID, "");
    }
    else
    {
      setValue(ODMUUID, value);
    }
  }
  
  public boolean isOdmUUIDWritable()
  {
    return isWritable(ODMUUID);
  }
  
  public boolean isOdmUUIDReadable()
  {
    return isReadable(ODMUUID);
  }
  
  public boolean isOdmUUIDModified()
  {
    return isModified(ODMUUID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getOdmUUIDMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(ODMUUID).getAttributeMdDTO();
  }
  
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskDTO getProcessingTask()
  {
    if(getValue(PROCESSINGTASK) == null || getValue(PROCESSINGTASK).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskDTO.get(getRequest(), getValue(PROCESSINGTASK));
    }
  }
  
  public String getProcessingTaskOid()
  {
    return getValue(PROCESSINGTASK);
  }
  
  public void setProcessingTask(gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskDTO value)
  {
    if(value == null)
    {
      setValue(PROCESSINGTASK, "");
    }
    else
    {
      setValue(PROCESSINGTASK, value.getOid());
    }
  }
  
  public boolean isProcessingTaskWritable()
  {
    return isWritable(PROCESSINGTASK);
  }
  
  public boolean isProcessingTaskReadable()
  {
    return isReadable(PROCESSINGTASK);
  }
  
  public boolean isProcessingTaskModified()
  {
    return isModified(PROCESSINGTASK);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getProcessingTaskMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(PROCESSINGTASK).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.odm.ImageryODMUploadTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.odm.ImageryODMUploadTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.odm.ImageryODMUploadTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
