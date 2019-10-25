package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -1711716023)
public abstract class UasComponentDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.UasComponent";
  public static final long serialVersionUID = -1711716023;
  
  protected UasComponentDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected UasComponentDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String NAME = "name";
  public static java.lang.String OID = "oid";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String S3LOCATION = "s3location";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
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
  
  public String getName()
  {
    return getValue(NAME);
  }
  
  public void setName(String value)
  {
    if(value == null)
    {
      setValue(NAME, "");
    }
    else
    {
      setValue(NAME, value);
    }
  }
  
  public boolean isNameWritable()
  {
    return isWritable(NAME);
  }
  
  public boolean isNameReadable()
  {
    return isReadable(NAME);
  }
  
  public boolean isNameModified()
  {
    return isModified(NAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(NAME).getAttributeMdDTO();
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
  
  public String getS3location()
  {
    return getValue(S3LOCATION);
  }
  
  public void setS3location(String value)
  {
    if(value == null)
    {
      setValue(S3LOCATION, "");
    }
    else
    {
      setValue(S3LOCATION, value);
    }
  }
  
  public boolean isS3locationWritable()
  {
    return isWritable(S3LOCATION);
  }
  
  public boolean isS3locationReadable()
  {
    return isReadable(S3LOCATION);
  }
  
  public boolean isS3locationModified()
  {
    return isModified(S3LOCATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getS3locationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(S3LOCATION).getAttributeMdDTO();
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
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO> getAllComponents()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO> getAllComponents(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO> getAllComponentsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO> getAllComponentsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.ComponentHasComponentDTO addComponents(gov.geoplatform.uasdm.bus.UasComponentDTO child)
  {
    return (gov.geoplatform.uasdm.bus.ComponentHasComponentDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ComponentHasComponentDTO addComponents(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.UasComponentDTO child)
  {
    return (gov.geoplatform.uasdm.bus.ComponentHasComponentDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public void removeComponents(gov.geoplatform.uasdm.bus.ComponentHasComponentDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeComponents(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllComponents()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public static void removeAllComponents(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO> getAllComponent()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO> getAllComponent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.UasComponentDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO> getAllComponentRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO> getAllComponentRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.ComponentHasComponentDTO addComponent(gov.geoplatform.uasdm.bus.UasComponentDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.ComponentHasComponentDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ComponentHasComponentDTO addComponent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.UasComponentDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.ComponentHasComponentDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public void removeComponent(gov.geoplatform.uasdm.bus.ComponentHasComponentDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeComponent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllComponent()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public static void removeAllComponent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.bus.ComponentHasComponentDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.UasComponentDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.UasComponentDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.UasComponentQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.UasComponentQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.UasComponentDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.UasComponentDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.UasComponentDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.UasComponentDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.UasComponentDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.UasComponentDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.UasComponentDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
