package gov.geoplatform.uasdm.processing;

@com.runwaysdk.business.ClassSignature(hash = 475800370)
public abstract class FargateProcessingTaskDTOBase extends gov.geoplatform.uasdm.bus.WorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.processing.FargateProcessingTask";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 475800370;
  
  protected FargateProcessingTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected FargateProcessingTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CONFIGURATIONJSON = "configurationJson";
  public static java.lang.String PROCESSFILENAMEARRAY = "processFilenameArray";
  public static java.lang.String PROCESSINGTYPE = "processingType";
  public static java.lang.String TASKARN = "taskArn";
  public static java.lang.String TASKDEFINITIONARN = "taskDefinitionArn";
  public String getConfigurationJson()
  {
    return getValue(CONFIGURATIONJSON);
  }
  
  public void setConfigurationJson(String value)
  {
    if(value == null)
    {
      setValue(CONFIGURATIONJSON, "");
    }
    else
    {
      setValue(CONFIGURATIONJSON, value);
    }
  }
  
  public boolean isConfigurationJsonWritable()
  {
    return isWritable(CONFIGURATIONJSON);
  }
  
  public boolean isConfigurationJsonReadable()
  {
    return isReadable(CONFIGURATIONJSON);
  }
  
  public boolean isConfigurationJsonModified()
  {
    return isModified(CONFIGURATIONJSON);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getConfigurationJsonMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CONFIGURATIONJSON).getAttributeMdDTO();
  }
  
  public String getProcessFilenameArray()
  {
    return getValue(PROCESSFILENAMEARRAY);
  }
  
  public void setProcessFilenameArray(String value)
  {
    if(value == null)
    {
      setValue(PROCESSFILENAMEARRAY, "");
    }
    else
    {
      setValue(PROCESSFILENAMEARRAY, value);
    }
  }
  
  public boolean isProcessFilenameArrayWritable()
  {
    return isWritable(PROCESSFILENAMEARRAY);
  }
  
  public boolean isProcessFilenameArrayReadable()
  {
    return isReadable(PROCESSFILENAMEARRAY);
  }
  
  public boolean isProcessFilenameArrayModified()
  {
    return isModified(PROCESSFILENAMEARRAY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProcessFilenameArrayMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROCESSFILENAMEARRAY).getAttributeMdDTO();
  }
  
  public String getProcessingType()
  {
    return getValue(PROCESSINGTYPE);
  }
  
  public void setProcessingType(String value)
  {
    if(value == null)
    {
      setValue(PROCESSINGTYPE, "");
    }
    else
    {
      setValue(PROCESSINGTYPE, value);
    }
  }
  
  public boolean isProcessingTypeWritable()
  {
    return isWritable(PROCESSINGTYPE);
  }
  
  public boolean isProcessingTypeReadable()
  {
    return isReadable(PROCESSINGTYPE);
  }
  
  public boolean isProcessingTypeModified()
  {
    return isModified(PROCESSINGTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProcessingTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROCESSINGTYPE).getAttributeMdDTO();
  }
  
  public String getTaskArn()
  {
    return getValue(TASKARN);
  }
  
  public void setTaskArn(String value)
  {
    if(value == null)
    {
      setValue(TASKARN, "");
    }
    else
    {
      setValue(TASKARN, value);
    }
  }
  
  public boolean isTaskArnWritable()
  {
    return isWritable(TASKARN);
  }
  
  public boolean isTaskArnReadable()
  {
    return isReadable(TASKARN);
  }
  
  public boolean isTaskArnModified()
  {
    return isModified(TASKARN);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getTaskArnMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(TASKARN).getAttributeMdDTO();
  }
  
  public String getTaskDefinitionArn()
  {
    return getValue(TASKDEFINITIONARN);
  }
  
  public void setTaskDefinitionArn(String value)
  {
    if(value == null)
    {
      setValue(TASKDEFINITIONARN, "");
    }
    else
    {
      setValue(TASKDEFINITIONARN, value);
    }
  }
  
  public boolean isTaskDefinitionArnWritable()
  {
    return isWritable(TASKDEFINITIONARN);
  }
  
  public boolean isTaskDefinitionArnReadable()
  {
    return isReadable(TASKDEFINITIONARN);
  }
  
  public boolean isTaskDefinitionArnModified()
  {
    return isModified(TASKDEFINITIONARN);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getTaskDefinitionArnMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(TASKDEFINITIONARN).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.processing.FargateProcessingTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.processing.FargateProcessingTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.processing.FargateProcessingTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
