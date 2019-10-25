package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -1507023348)
public abstract class CollectionDTOBase extends gov.geoplatform.uasdm.bus.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Collection";
  public static final long serialVersionUID = -1507023348;
  
  protected CollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected CollectionDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String IMAGEHEIGHT = "imageHeight";
  public static java.lang.String IMAGEWIDTH = "imageWidth";
  public static java.lang.String METADATAUPLOADED = "metadataUploaded";
  public static java.lang.String PRIVILEGETYPE = "privilegeType";
  public Integer getImageHeight()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(IMAGEHEIGHT));
  }
  
  public void setImageHeight(Integer value)
  {
    if(value == null)
    {
      setValue(IMAGEHEIGHT, "");
    }
    else
    {
      setValue(IMAGEHEIGHT, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isImageHeightWritable()
  {
    return isWritable(IMAGEHEIGHT);
  }
  
  public boolean isImageHeightReadable()
  {
    return isReadable(IMAGEHEIGHT);
  }
  
  public boolean isImageHeightModified()
  {
    return isModified(IMAGEHEIGHT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getImageHeightMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(IMAGEHEIGHT).getAttributeMdDTO();
  }
  
  public Integer getImageWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(IMAGEWIDTH));
  }
  
  public void setImageWidth(Integer value)
  {
    if(value == null)
    {
      setValue(IMAGEWIDTH, "");
    }
    else
    {
      setValue(IMAGEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isImageWidthWritable()
  {
    return isWritable(IMAGEWIDTH);
  }
  
  public boolean isImageWidthReadable()
  {
    return isReadable(IMAGEWIDTH);
  }
  
  public boolean isImageWidthModified()
  {
    return isModified(IMAGEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getImageWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(IMAGEWIDTH).getAttributeMdDTO();
  }
  
  public Boolean getMetadataUploaded()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(METADATAUPLOADED));
  }
  
  public void setMetadataUploaded(Boolean value)
  {
    if(value == null)
    {
      setValue(METADATAUPLOADED, "");
    }
    else
    {
      setValue(METADATAUPLOADED, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isMetadataUploadedWritable()
  {
    return isWritable(METADATAUPLOADED);
  }
  
  public boolean isMetadataUploadedReadable()
  {
    return isReadable(METADATAUPLOADED);
  }
  
  public boolean isMetadataUploadedModified()
  {
    return isModified(METADATAUPLOADED);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getMetadataUploadedMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(METADATAUPLOADED).getAttributeMdDTO();
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.bus.AllPrivilegeTypeDTO> getPrivilegeType()
  {
    return (java.util.List<gov.geoplatform.uasdm.bus.AllPrivilegeTypeDTO>) com.runwaysdk.transport.conversion.ConversionFacade.convertEnumDTOsFromEnumNames(getRequest(), gov.geoplatform.uasdm.bus.AllPrivilegeTypeDTO.CLASS, getEnumNames(PRIVILEGETYPE));
  }
  
  public java.util.List<String> getPrivilegeTypeEnumNames()
  {
    return getEnumNames(PRIVILEGETYPE);
  }
  
  public void addPrivilegeType(gov.geoplatform.uasdm.bus.AllPrivilegeTypeDTO enumDTO)
  {
    addEnumItem(PRIVILEGETYPE, enumDTO.toString());
  }
  
  public void removePrivilegeType(gov.geoplatform.uasdm.bus.AllPrivilegeTypeDTO enumDTO)
  {
    removeEnumItem(PRIVILEGETYPE, enumDTO.toString());
  }
  
  public void clearPrivilegeType()
  {
    clearEnum(PRIVILEGETYPE);
  }
  
  public boolean isPrivilegeTypeWritable()
  {
    return isWritable(PRIVILEGETYPE);
  }
  
  public boolean isPrivilegeTypeReadable()
  {
    return isReadable(PRIVILEGETYPE);
  }
  
  public boolean isPrivilegeTypeModified()
  {
    return isModified(PRIVILEGETYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO getPrivilegeTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO) getAttributeDTO(PRIVILEGETYPE).getAttributeMdDTO();
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getAllMission()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getAllMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO> getAllMissionRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO> getAllMissionRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.MissionHasCollectionDTO addMission(gov.geoplatform.uasdm.bus.MissionDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.MissionHasCollectionDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.MissionHasCollectionDTO addMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.MissionDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.MissionHasCollectionDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public void removeMission(gov.geoplatform.uasdm.bus.MissionHasCollectionDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllMission()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static void removeAllMission(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.CollectionDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.CollectionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.CollectionQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.CollectionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.CollectionDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.CollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.CollectionDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.CollectionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
