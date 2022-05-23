package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -606854194)
public abstract class WorkflowTaskDTOBase extends gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.WorkflowTask";
  private static final long serialVersionUID = -606854194;
  
  protected WorkflowTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected WorkflowTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String COMPONENT = "component";
  public static java.lang.String UPLOADTARGET = "uploadTarget";
  public static java.lang.String WORKFLOWTYPE = "workflowType";
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
  
  public String getUploadTarget()
  {
    return getValue(UPLOADTARGET);
  }
  
  public void setUploadTarget(String value)
  {
    if(value == null)
    {
      setValue(UPLOADTARGET, "");
    }
    else
    {
      setValue(UPLOADTARGET, value);
    }
  }
  
  public boolean isUploadTargetWritable()
  {
    return isWritable(UPLOADTARGET);
  }
  
  public boolean isUploadTargetReadable()
  {
    return isReadable(UPLOADTARGET);
  }
  
  public boolean isUploadTargetModified()
  {
    return isModified(UPLOADTARGET);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getUploadTargetMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(UPLOADTARGET).getAttributeMdDTO();
  }
  
  public String getWorkflowType()
  {
    return getValue(WORKFLOWTYPE);
  }
  
  public void setWorkflowType(String value)
  {
    if(value == null)
    {
      setValue(WORKFLOWTYPE, "");
    }
    else
    {
      setValue(WORKFLOWTYPE, value);
    }
  }
  
  public boolean isWorkflowTypeWritable()
  {
    return isWritable(WORKFLOWTYPE);
  }
  
  public boolean isWorkflowTypeReadable()
  {
    return isReadable(WORKFLOWTYPE);
  }
  
  public boolean isWorkflowTypeModified()
  {
    return isModified(WORKFLOWTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getWorkflowTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(WORKFLOWTYPE).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.WorkflowTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.WorkflowTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.WorkflowTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.WorkflowTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.WorkflowTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.WorkflowTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.WorkflowTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.WorkflowTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.WorkflowTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.WorkflowTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.WorkflowTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
