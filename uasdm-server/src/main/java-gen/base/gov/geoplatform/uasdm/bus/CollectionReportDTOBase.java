package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -1410662061)
public abstract class CollectionReportDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.CollectionReport";
  private static final long serialVersionUID = -1410662061;
  
  protected CollectionReportDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected CollectionReportDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ALLSTORAGESIZE = "allStorageSize";
  public static java.lang.String BUREAU = "bureau";
  public static java.lang.String BUREAUNAME = "bureauName";
  public static java.lang.String COLLECTION = "collection";
  public static java.lang.String COLLECTIONDATE = "collectionDate";
  public static java.lang.String COLLECTIONNAME = "collectionName";
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String DOWNLOADCOUNTS = "downloadCounts";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String EROSARCHIVED = "erosArchived";
  public static java.lang.String EROSMETADATACOMPLETE = "erosMetadataComplete";
  public static java.lang.String FAAIDNUMBER = "faaIdNumber";
  public static java.lang.String HILLSHADE = "hillshade";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String MISSION = "mission";
  public static java.lang.String MISSIONNAME = "missionName";
  public static java.lang.String ODMPROCESSING = "odmProcessing";
  public static java.lang.String OID = "oid";
  public static java.lang.String ORTHOMOSAIC = "orthomosaic";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String PLATFORM = "platform";
  public static java.lang.String PLATFORMNAME = "platformName";
  public static java.lang.String POINTCLOUD = "pointCloud";
  public static java.lang.String PRODUCTSLINK = "productsLink";
  public static java.lang.String PRODUCTSSHARED = "productsShared";
  public static java.lang.String PROJECT = "project";
  public static java.lang.String PROJECTNAME = "projectName";
  public static java.lang.String RAWIMAGESCOUNT = "rawImagesCount";
  public static java.lang.String RAWIMAGESLINK = "rawImagesLink";
  public static java.lang.String SENSOR = "sensor";
  public static java.lang.String SENSORNAME = "sensorName";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SERIALNUMBER = "serialNumber";
  public static java.lang.String SITE = "site";
  public static java.lang.String SITELATDECIMALDEGREE = "siteLatDecimalDegree";
  public static java.lang.String SITELONGDECIMALDEGREE = "siteLongDecimalDegree";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String SITENAME = "siteName";
  public static java.lang.String TYPE = "type";
  public static java.lang.String UAV = "uav";
  public static java.lang.String USERNAME = "userName";
  public static java.lang.String VIDEO = "video";
  public Long getAllStorageSize()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(ALLSTORAGESIZE));
  }
  
  public void setAllStorageSize(Long value)
  {
    if(value == null)
    {
      setValue(ALLSTORAGESIZE, "");
    }
    else
    {
      setValue(ALLSTORAGESIZE, java.lang.Long.toString(value));
    }
  }
  
  public boolean isAllStorageSizeWritable()
  {
    return isWritable(ALLSTORAGESIZE);
  }
  
  public boolean isAllStorageSizeReadable()
  {
    return isReadable(ALLSTORAGESIZE);
  }
  
  public boolean isAllStorageSizeModified()
  {
    return isModified(ALLSTORAGESIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getAllStorageSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(ALLSTORAGESIZE).getAttributeMdDTO();
  }
  
  public gov.geoplatform.uasdm.bus.BureauDTO getBureau()
  {
    if(getValue(BUREAU) == null || getValue(BUREAU).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.BureauDTO.get(getRequest(), getValue(BUREAU));
    }
  }
  
  public String getBureauOid()
  {
    return getValue(BUREAU);
  }
  
  public void setBureau(gov.geoplatform.uasdm.bus.BureauDTO value)
  {
    if(value == null)
    {
      setValue(BUREAU, "");
    }
    else
    {
      setValue(BUREAU, value.getOid());
    }
  }
  
  public boolean isBureauWritable()
  {
    return isWritable(BUREAU);
  }
  
  public boolean isBureauReadable()
  {
    return isReadable(BUREAU);
  }
  
  public boolean isBureauModified()
  {
    return isModified(BUREAU);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getBureauMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(BUREAU).getAttributeMdDTO();
  }
  
  public String getBureauName()
  {
    return getValue(BUREAUNAME);
  }
  
  public void setBureauName(String value)
  {
    if(value == null)
    {
      setValue(BUREAUNAME, "");
    }
    else
    {
      setValue(BUREAUNAME, value);
    }
  }
  
  public boolean isBureauNameWritable()
  {
    return isWritable(BUREAUNAME);
  }
  
  public boolean isBureauNameReadable()
  {
    return isReadable(BUREAUNAME);
  }
  
  public boolean isBureauNameModified()
  {
    return isModified(BUREAUNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getBureauNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(BUREAUNAME).getAttributeMdDTO();
  }
  
  public java.util.Date getCollectionDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(COLLECTIONDATE));
  }
  
  public void setCollectionDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(COLLECTIONDATE, "");
    }
    else
    {
      setValue(COLLECTIONDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isCollectionDateWritable()
  {
    return isWritable(COLLECTIONDATE);
  }
  
  public boolean isCollectionDateReadable()
  {
    return isReadable(COLLECTIONDATE);
  }
  
  public boolean isCollectionDateModified()
  {
    return isModified(COLLECTIONDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getCollectionDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(COLLECTIONDATE).getAttributeMdDTO();
  }
  
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
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getCollectionNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(COLLECTIONNAME).getAttributeMdDTO();
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
  
  public Long getDownloadCounts()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(DOWNLOADCOUNTS));
  }
  
  public void setDownloadCounts(Long value)
  {
    if(value == null)
    {
      setValue(DOWNLOADCOUNTS, "");
    }
    else
    {
      setValue(DOWNLOADCOUNTS, java.lang.Long.toString(value));
    }
  }
  
  public boolean isDownloadCountsWritable()
  {
    return isWritable(DOWNLOADCOUNTS);
  }
  
  public boolean isDownloadCountsReadable()
  {
    return isReadable(DOWNLOADCOUNTS);
  }
  
  public boolean isDownloadCountsModified()
  {
    return isModified(DOWNLOADCOUNTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getDownloadCountsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(DOWNLOADCOUNTS).getAttributeMdDTO();
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
  
  public Boolean getErosArchived()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(EROSARCHIVED));
  }
  
  public void setErosArchived(Boolean value)
  {
    if(value == null)
    {
      setValue(EROSARCHIVED, "");
    }
    else
    {
      setValue(EROSARCHIVED, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isErosArchivedWritable()
  {
    return isWritable(EROSARCHIVED);
  }
  
  public boolean isErosArchivedReadable()
  {
    return isReadable(EROSARCHIVED);
  }
  
  public boolean isErosArchivedModified()
  {
    return isModified(EROSARCHIVED);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getErosArchivedMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(EROSARCHIVED).getAttributeMdDTO();
  }
  
  public Boolean getErosMetadataComplete()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(EROSMETADATACOMPLETE));
  }
  
  public void setErosMetadataComplete(Boolean value)
  {
    if(value == null)
    {
      setValue(EROSMETADATACOMPLETE, "");
    }
    else
    {
      setValue(EROSMETADATACOMPLETE, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isErosMetadataCompleteWritable()
  {
    return isWritable(EROSMETADATACOMPLETE);
  }
  
  public boolean isErosMetadataCompleteReadable()
  {
    return isReadable(EROSMETADATACOMPLETE);
  }
  
  public boolean isErosMetadataCompleteModified()
  {
    return isModified(EROSMETADATACOMPLETE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getErosMetadataCompleteMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(EROSMETADATACOMPLETE).getAttributeMdDTO();
  }
  
  public String getFaaIdNumber()
  {
    return getValue(FAAIDNUMBER);
  }
  
  public void setFaaIdNumber(String value)
  {
    if(value == null)
    {
      setValue(FAAIDNUMBER, "");
    }
    else
    {
      setValue(FAAIDNUMBER, value);
    }
  }
  
  public boolean isFaaIdNumberWritable()
  {
    return isWritable(FAAIDNUMBER);
  }
  
  public boolean isFaaIdNumberReadable()
  {
    return isReadable(FAAIDNUMBER);
  }
  
  public boolean isFaaIdNumberModified()
  {
    return isModified(FAAIDNUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getFaaIdNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(FAAIDNUMBER).getAttributeMdDTO();
  }
  
  public Boolean getHillshade()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(HILLSHADE));
  }
  
  public void setHillshade(Boolean value)
  {
    if(value == null)
    {
      setValue(HILLSHADE, "");
    }
    else
    {
      setValue(HILLSHADE, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isHillshadeWritable()
  {
    return isWritable(HILLSHADE);
  }
  
  public boolean isHillshadeReadable()
  {
    return isReadable(HILLSHADE);
  }
  
  public boolean isHillshadeModified()
  {
    return isModified(HILLSHADE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getHillshadeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(HILLSHADE).getAttributeMdDTO();
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
  
  public String getMissionName()
  {
    return getValue(MISSIONNAME);
  }
  
  public void setMissionName(String value)
  {
    if(value == null)
    {
      setValue(MISSIONNAME, "");
    }
    else
    {
      setValue(MISSIONNAME, value);
    }
  }
  
  public boolean isMissionNameWritable()
  {
    return isWritable(MISSIONNAME);
  }
  
  public boolean isMissionNameReadable()
  {
    return isReadable(MISSIONNAME);
  }
  
  public boolean isMissionNameModified()
  {
    return isModified(MISSIONNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getMissionNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(MISSIONNAME).getAttributeMdDTO();
  }
  
  public String getOdmProcessing()
  {
    return getValue(ODMPROCESSING);
  }
  
  public void setOdmProcessing(String value)
  {
    if(value == null)
    {
      setValue(ODMPROCESSING, "");
    }
    else
    {
      setValue(ODMPROCESSING, value);
    }
  }
  
  public boolean isOdmProcessingWritable()
  {
    return isWritable(ODMPROCESSING);
  }
  
  public boolean isOdmProcessingReadable()
  {
    return isReadable(ODMPROCESSING);
  }
  
  public boolean isOdmProcessingModified()
  {
    return isModified(ODMPROCESSING);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getOdmProcessingMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(ODMPROCESSING).getAttributeMdDTO();
  }
  
  public Boolean getOrthomosaic()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(ORTHOMOSAIC));
  }
  
  public void setOrthomosaic(Boolean value)
  {
    if(value == null)
    {
      setValue(ORTHOMOSAIC, "");
    }
    else
    {
      setValue(ORTHOMOSAIC, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isOrthomosaicWritable()
  {
    return isWritable(ORTHOMOSAIC);
  }
  
  public boolean isOrthomosaicReadable()
  {
    return isReadable(ORTHOMOSAIC);
  }
  
  public boolean isOrthomosaicModified()
  {
    return isModified(ORTHOMOSAIC);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getOrthomosaicMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(ORTHOMOSAIC).getAttributeMdDTO();
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
  
  public String getPlatformName()
  {
    return getValue(PLATFORMNAME);
  }
  
  public void setPlatformName(String value)
  {
    if(value == null)
    {
      setValue(PLATFORMNAME, "");
    }
    else
    {
      setValue(PLATFORMNAME, value);
    }
  }
  
  public boolean isPlatformNameWritable()
  {
    return isWritable(PLATFORMNAME);
  }
  
  public boolean isPlatformNameReadable()
  {
    return isReadable(PLATFORMNAME);
  }
  
  public boolean isPlatformNameModified()
  {
    return isModified(PLATFORMNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPlatformNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(PLATFORMNAME).getAttributeMdDTO();
  }
  
  public Boolean getPointCloud()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(POINTCLOUD));
  }
  
  public void setPointCloud(Boolean value)
  {
    if(value == null)
    {
      setValue(POINTCLOUD, "");
    }
    else
    {
      setValue(POINTCLOUD, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isPointCloudWritable()
  {
    return isWritable(POINTCLOUD);
  }
  
  public boolean isPointCloudReadable()
  {
    return isReadable(POINTCLOUD);
  }
  
  public boolean isPointCloudModified()
  {
    return isModified(POINTCLOUD);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getPointCloudMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(POINTCLOUD).getAttributeMdDTO();
  }
  
  public String getProductsLink()
  {
    return getValue(PRODUCTSLINK);
  }
  
  public void setProductsLink(String value)
  {
    if(value == null)
    {
      setValue(PRODUCTSLINK, "");
    }
    else
    {
      setValue(PRODUCTSLINK, value);
    }
  }
  
  public boolean isProductsLinkWritable()
  {
    return isWritable(PRODUCTSLINK);
  }
  
  public boolean isProductsLinkReadable()
  {
    return isReadable(PRODUCTSLINK);
  }
  
  public boolean isProductsLinkModified()
  {
    return isModified(PRODUCTSLINK);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getProductsLinkMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(PRODUCTSLINK).getAttributeMdDTO();
  }
  
  public Boolean getProductsShared()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(PRODUCTSSHARED));
  }
  
  public void setProductsShared(Boolean value)
  {
    if(value == null)
    {
      setValue(PRODUCTSSHARED, "");
    }
    else
    {
      setValue(PRODUCTSSHARED, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isProductsSharedWritable()
  {
    return isWritable(PRODUCTSSHARED);
  }
  
  public boolean isProductsSharedReadable()
  {
    return isReadable(PRODUCTSSHARED);
  }
  
  public boolean isProductsSharedModified()
  {
    return isModified(PRODUCTSSHARED);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getProductsSharedMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(PRODUCTSSHARED).getAttributeMdDTO();
  }
  
  public String getProjectName()
  {
    return getValue(PROJECTNAME);
  }
  
  public void setProjectName(String value)
  {
    if(value == null)
    {
      setValue(PROJECTNAME, "");
    }
    else
    {
      setValue(PROJECTNAME, value);
    }
  }
  
  public boolean isProjectNameWritable()
  {
    return isWritable(PROJECTNAME);
  }
  
  public boolean isProjectNameReadable()
  {
    return isReadable(PROJECTNAME);
  }
  
  public boolean isProjectNameModified()
  {
    return isModified(PROJECTNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getProjectNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(PROJECTNAME).getAttributeMdDTO();
  }
  
  public Integer getRawImagesCount()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(RAWIMAGESCOUNT));
  }
  
  public void setRawImagesCount(Integer value)
  {
    if(value == null)
    {
      setValue(RAWIMAGESCOUNT, "");
    }
    else
    {
      setValue(RAWIMAGESCOUNT, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isRawImagesCountWritable()
  {
    return isWritable(RAWIMAGESCOUNT);
  }
  
  public boolean isRawImagesCountReadable()
  {
    return isReadable(RAWIMAGESCOUNT);
  }
  
  public boolean isRawImagesCountModified()
  {
    return isModified(RAWIMAGESCOUNT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getRawImagesCountMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(RAWIMAGESCOUNT).getAttributeMdDTO();
  }
  
  public String getRawImagesLink()
  {
    return getValue(RAWIMAGESLINK);
  }
  
  public void setRawImagesLink(String value)
  {
    if(value == null)
    {
      setValue(RAWIMAGESLINK, "");
    }
    else
    {
      setValue(RAWIMAGESLINK, value);
    }
  }
  
  public boolean isRawImagesLinkWritable()
  {
    return isWritable(RAWIMAGESLINK);
  }
  
  public boolean isRawImagesLinkReadable()
  {
    return isReadable(RAWIMAGESLINK);
  }
  
  public boolean isRawImagesLinkModified()
  {
    return isModified(RAWIMAGESLINK);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getRawImagesLinkMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(RAWIMAGESLINK).getAttributeMdDTO();
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
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSensorNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SENSORNAME).getAttributeMdDTO();
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
  
  public String getSerialNumber()
  {
    return getValue(SERIALNUMBER);
  }
  
  public void setSerialNumber(String value)
  {
    if(value == null)
    {
      setValue(SERIALNUMBER, "");
    }
    else
    {
      setValue(SERIALNUMBER, value);
    }
  }
  
  public boolean isSerialNumberWritable()
  {
    return isWritable(SERIALNUMBER);
  }
  
  public boolean isSerialNumberReadable()
  {
    return isReadable(SERIALNUMBER);
  }
  
  public boolean isSerialNumberModified()
  {
    return isModified(SERIALNUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSerialNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SERIALNUMBER).getAttributeMdDTO();
  }
  
  public java.math.BigDecimal getSiteLatDecimalDegree()
  {
    return com.runwaysdk.constants.MdAttributeDecimalUtil.getTypeSafeValue(getValue(SITELATDECIMALDEGREE));
  }
  
  public void setSiteLatDecimalDegree(java.math.BigDecimal value)
  {
    if(value == null)
    {
      setValue(SITELATDECIMALDEGREE, "");
    }
    else
    {
      setValue(SITELATDECIMALDEGREE, value.toString());
    }
  }
  
  public boolean isSiteLatDecimalDegreeWritable()
  {
    return isWritable(SITELATDECIMALDEGREE);
  }
  
  public boolean isSiteLatDecimalDegreeReadable()
  {
    return isReadable(SITELATDECIMALDEGREE);
  }
  
  public boolean isSiteLatDecimalDegreeModified()
  {
    return isModified(SITELATDECIMALDEGREE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getSiteLatDecimalDegreeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(SITELATDECIMALDEGREE).getAttributeMdDTO();
  }
  
  public java.math.BigDecimal getSiteLongDecimalDegree()
  {
    return com.runwaysdk.constants.MdAttributeDecimalUtil.getTypeSafeValue(getValue(SITELONGDECIMALDEGREE));
  }
  
  public void setSiteLongDecimalDegree(java.math.BigDecimal value)
  {
    if(value == null)
    {
      setValue(SITELONGDECIMALDEGREE, "");
    }
    else
    {
      setValue(SITELONGDECIMALDEGREE, value.toString());
    }
  }
  
  public boolean isSiteLongDecimalDegreeWritable()
  {
    return isWritable(SITELONGDECIMALDEGREE);
  }
  
  public boolean isSiteLongDecimalDegreeReadable()
  {
    return isReadable(SITELONGDECIMALDEGREE);
  }
  
  public boolean isSiteLongDecimalDegreeModified()
  {
    return isModified(SITELONGDECIMALDEGREE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getSiteLongDecimalDegreeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(SITELONGDECIMALDEGREE).getAttributeMdDTO();
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
  
  public String getSiteName()
  {
    return getValue(SITENAME);
  }
  
  public void setSiteName(String value)
  {
    if(value == null)
    {
      setValue(SITENAME, "");
    }
    else
    {
      setValue(SITENAME, value);
    }
  }
  
  public boolean isSiteNameWritable()
  {
    return isWritable(SITENAME);
  }
  
  public boolean isSiteNameReadable()
  {
    return isReadable(SITENAME);
  }
  
  public boolean isSiteNameModified()
  {
    return isModified(SITENAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSiteNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SITENAME).getAttributeMdDTO();
  }
  
  public String getUserName()
  {
    return getValue(USERNAME);
  }
  
  public void setUserName(String value)
  {
    if(value == null)
    {
      setValue(USERNAME, "");
    }
    else
    {
      setValue(USERNAME, value);
    }
  }
  
  public boolean isUserNameWritable()
  {
    return isWritable(USERNAME);
  }
  
  public boolean isUserNameReadable()
  {
    return isReadable(USERNAME);
  }
  
  public boolean isUserNameModified()
  {
    return isModified(USERNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getUserNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(USERNAME).getAttributeMdDTO();
  }
  
  public Boolean getVideo()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(VIDEO));
  }
  
  public void setVideo(Boolean value)
  {
    if(value == null)
    {
      setValue(VIDEO, "");
    }
    else
    {
      setValue(VIDEO, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isVideoWritable()
  {
    return isWritable(VIDEO);
  }
  
  public boolean isVideoReadable()
  {
    return isReadable(VIDEO);
  }
  
  public boolean isVideoModified()
  {
    return isModified(VIDEO);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getVideoMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(VIDEO).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionReportDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.CollectionReportDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.CollectionReportQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.CollectionReportQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.CollectionReportDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionReportDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.CollectionReportDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.CollectionReportDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.CollectionReportDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.CollectionReportDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.CollectionReportDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
