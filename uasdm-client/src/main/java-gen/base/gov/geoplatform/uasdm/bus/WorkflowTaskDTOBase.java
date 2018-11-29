package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 361010444)
public abstract class WorkflowTaskDTOBase extends gov.geoplatform.uasdm.bus.AbstractWorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.WorkflowTask";
  private static final long serialVersionUID = 361010444;
  
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
  
  public static java.lang.String COLLECTION = "collection";
  public static java.lang.String UPLOADID = "upLoadId";
  public gov.geoplatform.uasdm.bus.CollectionDTO getCollection()
  {
    if(getValue(COLLECTION) == null || getValue(COLLECTION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.CollectionDTO.get(getRequest(), getValue(COLLECTION));
    }
  }
  
  public String getCollectionOid()
  {
    return getValue(COLLECTION);
  }
  
  public void setCollection(gov.geoplatform.uasdm.bus.CollectionDTO value)
  {
    if(value == null)
    {
      setValue(COLLECTION, "");
    }
    else
    {
      setValue(COLLECTION, value.getOid());
    }
  }
  
  public boolean isCollectionWritable()
  {
    return isWritable(COLLECTION);
  }
  
  public boolean isCollectionReadable()
  {
    return isReadable(COLLECTION);
  }
  
  public boolean isCollectionModified()
  {
    return isModified(COLLECTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getCollectionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(COLLECTION).getAttributeMdDTO();
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
