package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -954671978)
public abstract class ImageryWorkflowTaskDTOBase extends gov.geoplatform.uasdm.bus.AbstractWorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.ImageryWorkflowTask";
  private static final long serialVersionUID = -954671978;
  
  protected ImageryWorkflowTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImageryWorkflowTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String IMAGERY = "imagery";
  public static java.lang.String UPLOADID = "upLoadId";
  public gov.geoplatform.uasdm.bus.ImageryDTO getImagery()
  {
    if(getValue(IMAGERY) == null || getValue(IMAGERY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.ImageryDTO.get(getRequest(), getValue(IMAGERY));
    }
  }
  
  public String getImageryOid()
  {
    return getValue(IMAGERY);
  }
  
  public void setImagery(gov.geoplatform.uasdm.bus.ImageryDTO value)
  {
    if(value == null)
    {
      setValue(IMAGERY, "");
    }
    else
    {
      setValue(IMAGERY, value.getOid());
    }
  }
  
  public boolean isImageryWritable()
  {
    return isWritable(IMAGERY);
  }
  
  public boolean isImageryReadable()
  {
    return isReadable(IMAGERY);
  }
  
  public boolean isImageryModified()
  {
    return isModified(IMAGERY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getImageryMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(IMAGERY).getAttributeMdDTO();
  }
  
  public String getUpLoadId()
  {
    return getValue(UPLOADID);
  }
  
  public void setUpLoadId(String value)
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
  
  public boolean isUpLoadIdWritable()
  {
    return isWritable(UPLOADID);
  }
  
  public boolean isUpLoadIdReadable()
  {
    return isReadable(UPLOADID);
  }
  
  public boolean isUpLoadIdModified()
  {
    return isModified(UPLOADID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getUpLoadIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(UPLOADID).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}