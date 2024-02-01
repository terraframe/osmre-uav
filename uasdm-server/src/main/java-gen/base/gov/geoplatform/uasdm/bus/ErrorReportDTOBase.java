package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1257854298)
public abstract class ErrorReportDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.ErrorReport";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1257854298;
  
  protected ErrorReportDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ErrorReportDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String COLLECTIONNAME = "collectionName";
  public static java.lang.String COLLECTIONPOCNAME = "collectionPocName";
  public static java.lang.String COLLECTIONS3PATH = "collectionS3Path";
  public static java.lang.String COLLECTIONSIZE = "collectionSize";
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String ERRORDATE = "errorDate";
  public static java.lang.String FAILREASON = "failReason";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String ODMCONFIG = "odmConfig";
  public static java.lang.String OID = "oid";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String SENSORNAME = "sensorName";
  public static java.lang.String SENSORTYPE = "sensorType";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
  public static java.lang.String UAVFAAID = "uavFaaId";
  public static java.lang.String UAVSERIALNUMBER = "uavSerialNumber";
  public String getCollectionName()
  {
    return getValue(COLLECTIONNAME);
  }
  
  public void setCollectionName(String value)
  {
    if(value == null)
    {
      setValue(COLLECTIONNAME, "");
    }
    else
    {
      setValue(COLLECTIONNAME, value);
    }
  }
  
  public boolean isCollectionNameWritable()
  {
    return isWritable(COLLECTIONNAME);
  }
  
  public boolean isCollectionNameReadable()
  {
    return isReadable(COLLECTIONNAME);
  }
  
  public boolean isCollectionNameModified()
  {
    return isModified(COLLECTIONNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCollectionNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(COLLECTIONNAME).getAttributeMdDTO();
  }
  
  public String getCollectionPocName()
  {
    return getValue(COLLECTIONPOCNAME);
  }
  
  public void setCollectionPocName(String value)
  {
    if(value == null)
    {
      setValue(COLLECTIONPOCNAME, "");
    }
    else
    {
      setValue(COLLECTIONPOCNAME, value);
    }
  }
  
  public boolean isCollectionPocNameWritable()
  {
    return isWritable(COLLECTIONPOCNAME);
  }
  
  public boolean isCollectionPocNameReadable()
  {
    return isReadable(COLLECTIONPOCNAME);
  }
  
  public boolean isCollectionPocNameModified()
  {
    return isModified(COLLECTIONPOCNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCollectionPocNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(COLLECTIONPOCNAME).getAttributeMdDTO();
  }
  
  public String getCollectionS3Path()
  {
    return getValue(COLLECTIONS3PATH);
  }
  
  public void setCollectionS3Path(String value)
  {
    if(value == null)
    {
      setValue(COLLECTIONS3PATH, "");
    }
    else
    {
      setValue(COLLECTIONS3PATH, value);
    }
  }
  
  public boolean isCollectionS3PathWritable()
  {
    return isWritable(COLLECTIONS3PATH);
  }
  
  public boolean isCollectionS3PathReadable()
  {
    return isReadable(COLLECTIONS3PATH);
  }
  
  public boolean isCollectionS3PathModified()
  {
    return isModified(COLLECTIONS3PATH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCollectionS3PathMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(COLLECTIONS3PATH).getAttributeMdDTO();
  }
  
  public Long getCollectionSize()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(COLLECTIONSIZE));
  }
  
  public void setCollectionSize(Long value)
  {
    if(value == null)
    {
      setValue(COLLECTIONSIZE, "");
    }
    else
    {
      setValue(COLLECTIONSIZE, java.lang.Long.toString(value));
    }
  }
  
  public boolean isCollectionSizeWritable()
  {
    return isWritable(COLLECTIONSIZE);
  }
  
  public boolean isCollectionSizeReadable()
  {
    return isReadable(COLLECTIONSIZE);
  }
  
  public boolean isCollectionSizeModified()
  {
    return isModified(COLLECTIONSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getCollectionSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(COLLECTIONSIZE).getAttributeMdDTO();
  }
  
  public java.util.Date getCreateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(CREATEDATE));
  }
  
  public boolean isCreateDateWritable()
  {
    return isWritable(CREATEDATE);
  }
  
  public boolean isCreateDateReadable()
  {
    return isReadable(CREATEDATE);
  }
  
  public boolean isCreateDateModified()
  {
    return isModified(CREATEDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getCreateDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(CREATEDATE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getCreatedBy()
  {
    if(getValue(CREATEDBY) == null || getValue(CREATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(CREATEDBY));
    }
  }
  
  public String getCreatedByOid()
  {
    return getValue(CREATEDBY);
  }
  
  public boolean isCreatedByWritable()
  {
    return isWritable(CREATEDBY);
  }
  
  public boolean isCreatedByReadable()
  {
    return isReadable(CREATEDBY);
  }
  
  public boolean isCreatedByModified()
  {
    return isModified(CREATEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getCreatedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(CREATEDBY).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.metadata.MdDomainDTO getEntityDomain()
  {
    if(getValue(ENTITYDOMAIN) == null || getValue(ENTITYDOMAIN).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdDomainDTO.get(getRequest(), getValue(ENTITYDOMAIN));
    }
  }
  
  public String getEntityDomainOid()
  {
    return getValue(ENTITYDOMAIN);
  }
  
  public void setEntityDomain(com.runwaysdk.system.metadata.MdDomainDTO value)
  {
    if(value == null)
    {
      setValue(ENTITYDOMAIN, "");
    }
    else
    {
      setValue(ENTITYDOMAIN, value.getOid());
    }
  }
  
  public boolean isEntityDomainWritable()
  {
    return isWritable(ENTITYDOMAIN);
  }
  
  public boolean isEntityDomainReadable()
  {
    return isReadable(ENTITYDOMAIN);
  }
  
  public boolean isEntityDomainModified()
  {
    return isModified(ENTITYDOMAIN);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getEntityDomainMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(ENTITYDOMAIN).getAttributeMdDTO();
  }
  
  public java.util.Date getErrorDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(ERRORDATE));
  }
  
  public void setErrorDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(ERRORDATE, "");
    }
    else
    {
      setValue(ERRORDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATETIME_FORMAT).format(value));
    }
  }
  
  public boolean isErrorDateWritable()
  {
    return isWritable(ERRORDATE);
  }
  
  public boolean isErrorDateReadable()
  {
    return isReadable(ERRORDATE);
  }
  
  public boolean isErrorDateModified()
  {
    return isModified(ERRORDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getErrorDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(ERRORDATE).getAttributeMdDTO();
  }
  
  public String getFailReason()
  {
    return getValue(FAILREASON);
  }
  
  public void setFailReason(String value)
  {
    if(value == null)
    {
      setValue(FAILREASON, "");
    }
    else
    {
      setValue(FAILREASON, value);
    }
  }
  
  public boolean isFailReasonWritable()
  {
    return isWritable(FAILREASON);
  }
  
  public boolean isFailReasonReadable()
  {
    return isReadable(FAILREASON);
  }
  
  public boolean isFailReasonModified()
  {
    return isModified(FAILREASON);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getFailReasonMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(FAILREASON).getAttributeMdDTO();
  }
  
  public String getKeyName()
  {
    return getValue(KEYNAME);
  }
  
  public void setKeyName(String value)
  {
    if(value == null)
    {
      setValue(KEYNAME, "");
    }
    else
    {
      setValue(KEYNAME, value);
    }
  }
  
  public boolean isKeyNameWritable()
  {
    return isWritable(KEYNAME);
  }
  
  public boolean isKeyNameReadable()
  {
    return isReadable(KEYNAME);
  }
  
  public boolean isKeyNameModified()
  {
    return isModified(KEYNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getKeyNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(KEYNAME).getAttributeMdDTO();
  }
  
  public java.util.Date getLastUpdateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTUPDATEDATE));
  }
  
  public boolean isLastUpdateDateWritable()
  {
    return isWritable(LASTUPDATEDATE);
  }
  
  public boolean isLastUpdateDateReadable()
  {
    return isReadable(LASTUPDATEDATE);
  }
  
  public boolean isLastUpdateDateModified()
  {
    return isModified(LASTUPDATEDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getLastUpdateDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(LASTUPDATEDATE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getLastUpdatedBy()
  {
    if(getValue(LASTUPDATEDBY) == null || getValue(LASTUPDATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(LASTUPDATEDBY));
    }
  }
  
  public String getLastUpdatedByOid()
  {
    return getValue(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByWritable()
  {
    return isWritable(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByReadable()
  {
    return isReadable(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByModified()
  {
    return isModified(LASTUPDATEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getLastUpdatedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(LASTUPDATEDBY).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getLockedBy()
  {
    if(getValue(LOCKEDBY) == null || getValue(LOCKEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(LOCKEDBY));
    }
  }
  
  public String getLockedByOid()
  {
    return getValue(LOCKEDBY);
  }
  
  public boolean isLockedByWritable()
  {
    return isWritable(LOCKEDBY);
  }
  
  public boolean isLockedByReadable()
  {
    return isReadable(LOCKEDBY);
  }
  
  public boolean isLockedByModified()
  {
    return isModified(LOCKEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getLockedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(LOCKEDBY).getAttributeMdDTO();
  }
  
  public String getOdmConfig()
  {
    return getValue(ODMCONFIG);
  }
  
  public void setOdmConfig(String value)
  {
    if(value == null)
    {
      setValue(ODMCONFIG, "");
    }
    else
    {
      setValue(ODMCONFIG, value);
    }
  }
  
  public boolean isOdmConfigWritable()
  {
    return isWritable(ODMCONFIG);
  }
  
  public boolean isOdmConfigReadable()
  {
    return isReadable(ODMCONFIG);
  }
  
  public boolean isOdmConfigModified()
  {
    return isModified(ODMCONFIG);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getOdmConfigMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ODMCONFIG).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.ActorDTO getOwner()
  {
    if(getValue(OWNER) == null || getValue(OWNER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.ActorDTO.get(getRequest(), getValue(OWNER));
    }
  }
  
  public String getOwnerOid()
  {
    return getValue(OWNER);
  }
  
  public void setOwner(com.runwaysdk.system.ActorDTO value)
  {
    if(value == null)
    {
      setValue(OWNER, "");
    }
    else
    {
      setValue(OWNER, value.getOid());
    }
  }
  
  public boolean isOwnerWritable()
  {
    return isWritable(OWNER);
  }
  
  public boolean isOwnerReadable()
  {
    return isReadable(OWNER);
  }
  
  public boolean isOwnerModified()
  {
    return isModified(OWNER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getOwnerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(OWNER).getAttributeMdDTO();
  }
  
  public String getSensorName()
  {
    return getValue(SENSORNAME);
  }
  
  public void setSensorName(String value)
  {
    if(value == null)
    {
      setValue(SENSORNAME, "");
    }
    else
    {
      setValue(SENSORNAME, value);
    }
  }
  
  public boolean isSensorNameWritable()
  {
    return isWritable(SENSORNAME);
  }
  
  public boolean isSensorNameReadable()
  {
    return isReadable(SENSORNAME);
  }
  
  public boolean isSensorNameModified()
  {
    return isModified(SENSORNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getSensorNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(SENSORNAME).getAttributeMdDTO();
  }
  
  public String getSensorType()
  {
    return getValue(SENSORTYPE);
  }
  
  public void setSensorType(String value)
  {
    if(value == null)
    {
      setValue(SENSORTYPE, "");
    }
    else
    {
      setValue(SENSORTYPE, value);
    }
  }
  
  public boolean isSensorTypeWritable()
  {
    return isWritable(SENSORTYPE);
  }
  
  public boolean isSensorTypeReadable()
  {
    return isReadable(SENSORTYPE);
  }
  
  public boolean isSensorTypeModified()
  {
    return isModified(SENSORTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getSensorTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(SENSORTYPE).getAttributeMdDTO();
  }
  
  public Long getSeq()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(SEQ));
  }
  
  public boolean isSeqWritable()
  {
    return isWritable(SEQ);
  }
  
  public boolean isSeqReadable()
  {
    return isReadable(SEQ);
  }
  
  public boolean isSeqModified()
  {
    return isModified(SEQ);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getSeqMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(SEQ).getAttributeMdDTO();
  }
  
  public String getSiteMaster()
  {
    return getValue(SITEMASTER);
  }
  
  public boolean isSiteMasterWritable()
  {
    return isWritable(SITEMASTER);
  }
  
  public boolean isSiteMasterReadable()
  {
    return isReadable(SITEMASTER);
  }
  
  public boolean isSiteMasterModified()
  {
    return isModified(SITEMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSiteMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SITEMASTER).getAttributeMdDTO();
  }
  
  public String getUavFaaId()
  {
    return getValue(UAVFAAID);
  }
  
  public void setUavFaaId(String value)
  {
    if(value == null)
    {
      setValue(UAVFAAID, "");
    }
    else
    {
      setValue(UAVFAAID, value);
    }
  }
  
  public boolean isUavFaaIdWritable()
  {
    return isWritable(UAVFAAID);
  }
  
  public boolean isUavFaaIdReadable()
  {
    return isReadable(UAVFAAID);
  }
  
  public boolean isUavFaaIdModified()
  {
    return isModified(UAVFAAID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getUavFaaIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(UAVFAAID).getAttributeMdDTO();
  }
  
  public String getUavSerialNumber()
  {
    return getValue(UAVSERIALNUMBER);
  }
  
  public void setUavSerialNumber(String value)
  {
    if(value == null)
    {
      setValue(UAVSERIALNUMBER, "");
    }
    else
    {
      setValue(UAVSERIALNUMBER, value);
    }
  }
  
  public boolean isUavSerialNumberWritable()
  {
    return isWritable(UAVSERIALNUMBER);
  }
  
  public boolean isUavSerialNumberReadable()
  {
    return isReadable(UAVSERIALNUMBER);
  }
  
  public boolean isUavSerialNumberModified()
  {
    return isModified(UAVSERIALNUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getUavSerialNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(UAVSERIALNUMBER).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.ErrorReportDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.ErrorReportDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.ErrorReportQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.ErrorReportQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.ErrorReportDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ErrorReportDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ErrorReportDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ErrorReportDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ErrorReportDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ErrorReportDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ErrorReportDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
