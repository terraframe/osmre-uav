package gov.geoplatform.uasdm.lidar;

@com.runwaysdk.business.ClassSignature(hash = 1588890494)
public abstract class LidarProcessingTaskDTOBase extends gov.geoplatform.uasdm.bus.WorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.lidar.LidarProcessingTask";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1588890494;
  
  protected LidarProcessingTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected LidarProcessingTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CONFIGURATIONJSON = "configurationJson";
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
  
  public static gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.lidar.LidarProcessingTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.lidar.LidarProcessingTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.lidar.LidarProcessingTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
