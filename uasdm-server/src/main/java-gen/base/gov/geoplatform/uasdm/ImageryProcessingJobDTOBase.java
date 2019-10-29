package gov.geoplatform.uasdm;

@com.runwaysdk.business.ClassSignature(hash = 1629529206)
public abstract class ImageryProcessingJobDTOBase extends com.runwaysdk.system.scheduler.ExecutableJobDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.ImageryProcessingJob";
  private static final long serialVersionUID = 1629529206;
  
  protected ImageryProcessingJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImageryProcessingJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String IMAGERYFILE = "imageryFile";
  public static java.lang.String OUTFILENAMEPREFIX = "outFileNamePrefix";
  public static java.lang.String UPLOADTARGET = "uploadTarget";
  public static java.lang.String WORKFLOWTASK = "workflowTask";
  public String getImageryFile()
  {
    return getValue(IMAGERYFILE);
  }
  
  public void setImageryFile(String value)
  {
    if(value == null)
    {
      setValue(IMAGERYFILE, "");
    }
    else
    {
      setValue(IMAGERYFILE, value);
    }
  }
  
  public boolean isImageryFileWritable()
  {
    return isWritable(IMAGERYFILE);
  }
  
  public boolean isImageryFileReadable()
  {
    return isReadable(IMAGERYFILE);
  }
  
  public boolean isImageryFileModified()
  {
    return isModified(IMAGERYFILE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeFileMdDTO getImageryFileMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeFileMdDTO) getAttributeDTO(IMAGERYFILE).getAttributeMdDTO();
  }
  
  public String getOutFileNamePrefix()
  {
    return getValue(OUTFILENAMEPREFIX);
  }
  
  public void setOutFileNamePrefix(String value)
  {
    if(value == null)
    {
      setValue(OUTFILENAMEPREFIX, "");
    }
    else
    {
      setValue(OUTFILENAMEPREFIX, value);
    }
  }
  
  public boolean isOutFileNamePrefixWritable()
  {
    return isWritable(OUTFILENAMEPREFIX);
  }
  
  public boolean isOutFileNamePrefixReadable()
  {
    return isReadable(OUTFILENAMEPREFIX);
  }
  
  public boolean isOutFileNamePrefixModified()
  {
    return isModified(OUTFILENAMEPREFIX);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getOutFileNamePrefixMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(OUTFILENAMEPREFIX).getAttributeMdDTO();
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
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getUploadTargetMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(UPLOADTARGET).getAttributeMdDTO();
  }
  
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskDTO getWorkflowTask()
  {
    if(getValue(WORKFLOWTASK) == null || getValue(WORKFLOWTASK).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.AbstractWorkflowTaskDTO.get(getRequest(), getValue(WORKFLOWTASK));
    }
  }
  
  public String getWorkflowTaskOid()
  {
    return getValue(WORKFLOWTASK);
  }
  
  public void setWorkflowTask(gov.geoplatform.uasdm.bus.AbstractWorkflowTaskDTO value)
  {
    if(value == null)
    {
      setValue(WORKFLOWTASK, "");
    }
    else
    {
      setValue(WORKFLOWTASK, value.getOid());
    }
  }
  
  public boolean isWorkflowTaskWritable()
  {
    return isWritable(WORKFLOWTASK);
  }
  
  public boolean isWorkflowTaskReadable()
  {
    return isReadable(WORKFLOWTASK);
  }
  
  public boolean isWorkflowTaskModified()
  {
    return isModified(WORKFLOWTASK);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getWorkflowTaskMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(WORKFLOWTASK).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.ImageryProcessingJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.ImageryProcessingJobDTO) dto;
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
  
  public static gov.geoplatform.uasdm.ImageryProcessingJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.ImageryProcessingJobQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.ImageryProcessingJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.ImageryProcessingJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.ImageryProcessingJobDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.ImageryProcessingJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.ImageryProcessingJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.ImageryProcessingJobDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.ImageryProcessingJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
