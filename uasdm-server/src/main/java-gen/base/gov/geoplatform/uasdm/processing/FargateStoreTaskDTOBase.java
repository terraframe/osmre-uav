package gov.geoplatform.uasdm.processing;

@com.runwaysdk.business.ClassSignature(hash = 995742410)
public abstract class FargateStoreTaskDTOBase extends gov.geoplatform.uasdm.bus.WorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.processing.FargateStoreTask";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 995742410;
  
  protected FargateStoreTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected FargateStoreTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String PROCESSINGJOBID = "processingJobId";
  public static java.lang.String PROCESSINGRUN = "processingRun";
  public static java.lang.String PROCESSINGTYPE = "processingType";
  public static java.lang.String TASKARN = "taskArn";
  public static java.lang.String TASKDEFINITIONARN = "taskDefinitionArn";
  public String getProcessingJobId()
  {
    return getValue(PROCESSINGJOBID);
  }
  
  public void setProcessingJobId(String value)
  {
    if(value == null)
    {
      setValue(PROCESSINGJOBID, "");
    }
    else
    {
      setValue(PROCESSINGJOBID, value);
    }
  }
  
  public boolean isProcessingJobIdWritable()
  {
    return isWritable(PROCESSINGJOBID);
  }
  
  public boolean isProcessingJobIdReadable()
  {
    return isReadable(PROCESSINGJOBID);
  }
  
  public boolean isProcessingJobIdModified()
  {
    return isModified(PROCESSINGJOBID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProcessingJobIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROCESSINGJOBID).getAttributeMdDTO();
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
  
  public static gov.geoplatform.uasdm.processing.FargateStoreTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.processing.FargateStoreTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.processing.FargateStoreTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.processing.FargateStoreTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.processing.FargateStoreTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.processing.FargateStoreTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.processing.FargateStoreTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.processing.FargateStoreTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.processing.FargateStoreTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.processing.FargateStoreTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.processing.FargateStoreTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
