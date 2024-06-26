/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1607889406)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to Product.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ProductBase extends com.runwaysdk.business.Business
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Product";
  public static java.lang.String BOUNDINGBOX = "boundingBox";
  public static java.lang.String COMPONENT = "component";
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
  public static java.lang.String PUBLISHED = "published";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
  private static final long serialVersionUID = 1607889406;
  
  public ProductBase()
  {
    super();
  }
  
  public String getBoundingBox()
  {
    return getValue(BOUNDINGBOX);
  }
  
  public void validateBoundingBox()
  {
    this.validateAttribute(BOUNDINGBOX);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getBoundingBoxMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(BOUNDINGBOX);
  }
  
  public void setBoundingBox(String value)
  {
    if(value == null)
    {
      setValue(BOUNDINGBOX, "");
    }
    else
    {
      setValue(BOUNDINGBOX, value);
    }
  }
  
  public gov.geoplatform.uasdm.bus.UasComponent getComponent()
  {
    if (getValue(COMPONENT).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.UasComponent.get(getValue(COMPONENT));
    }
  }
  
  public String getComponentOid()
  {
    return getValue(COMPONENT);
  }
  
  public void validateComponent()
  {
    this.validateAttribute(COMPONENT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getComponentMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(COMPONENT);
  }
  
  public void setComponent(gov.geoplatform.uasdm.bus.UasComponent value)
  {
    if(value == null)
    {
      setValue(COMPONENT, "");
    }
    else
    {
      setValue(COMPONENT, value.getOid());
    }
  }
  
  public void setComponentId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(COMPONENT, "");
    }
    else
    {
      setValue(COMPONENT, oid);
    }
  }
  
  public java.util.Date getCreateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(CREATEDATE));
  }
  
  public void validateCreateDate()
  {
    this.validateAttribute(CREATEDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getCreateDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(CREATEDATE);
  }
  
  public com.runwaysdk.system.SingleActor getCreatedBy()
  {
    if (getValue(CREATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(CREATEDBY));
    }
  }
  
  public String getCreatedByOid()
  {
    return getValue(CREATEDBY);
  }
  
  public void validateCreatedBy()
  {
    this.validateAttribute(CREATEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getCreatedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(CREATEDBY);
  }
  
  public com.runwaysdk.system.metadata.MdDomain getEntityDomain()
  {
    if (getValue(ENTITYDOMAIN).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdDomain.get(getValue(ENTITYDOMAIN));
    }
  }
  
  public String getEntityDomainOid()
  {
    return getValue(ENTITYDOMAIN);
  }
  
  public void validateEntityDomain()
  {
    this.validateAttribute(ENTITYDOMAIN);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getEntityDomainMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(ENTITYDOMAIN);
  }
  
  public void setEntityDomain(com.runwaysdk.system.metadata.MdDomain value)
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
  
  public void setEntityDomainId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(ENTITYDOMAIN, "");
    }
    else
    {
      setValue(ENTITYDOMAIN, oid);
    }
  }
  
  public String getKeyName()
  {
    return getValue(KEYNAME);
  }
  
  public void validateKeyName()
  {
    this.validateAttribute(KEYNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getKeyNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(KEYNAME);
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
  
  public java.util.Date getLastUpdateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTUPDATEDATE));
  }
  
  public void validateLastUpdateDate()
  {
    this.validateAttribute(LASTUPDATEDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getLastUpdateDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(LASTUPDATEDATE);
  }
  
  public com.runwaysdk.system.SingleActor getLastUpdatedBy()
  {
    if (getValue(LASTUPDATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(LASTUPDATEDBY));
    }
  }
  
  public String getLastUpdatedByOid()
  {
    return getValue(LASTUPDATEDBY);
  }
  
  public void validateLastUpdatedBy()
  {
    this.validateAttribute(LASTUPDATEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getLastUpdatedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(LASTUPDATEDBY);
  }
  
  public com.runwaysdk.system.SingleActor getLockedBy()
  {
    if (getValue(LOCKEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(LOCKEDBY));
    }
  }
  
  public String getLockedByOid()
  {
    return getValue(LOCKEDBY);
  }
  
  public void validateLockedBy()
  {
    this.validateAttribute(LOCKEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getLockedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(LOCKEDBY);
  }
  
  public String getName()
  {
    return getValue(NAME);
  }
  
  public void validateName()
  {
    this.validateAttribute(NAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(NAME);
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
  
  public String getOid()
  {
    return getValue(OID);
  }
  
  public void validateOid()
  {
    this.validateAttribute(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public com.runwaysdk.system.Actor getOwner()
  {
    if (getValue(OWNER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.Actor.get(getValue(OWNER));
    }
  }
  
  public String getOwnerOid()
  {
    return getValue(OWNER);
  }
  
  public void validateOwner()
  {
    this.validateAttribute(OWNER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getOwnerMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(OWNER);
  }
  
  public void setOwner(com.runwaysdk.system.Actor value)
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
  
  public void setOwnerId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(OWNER, "");
    }
    else
    {
      setValue(OWNER, oid);
    }
  }
  
  public Boolean getPublished()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(PUBLISHED));
  }
  
  public void validatePublished()
  {
    this.validateAttribute(PUBLISHED);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF getPublishedMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF)mdClassIF.definesAttribute(PUBLISHED);
  }
  
  public void setPublished(Boolean value)
  {
    if(value == null)
    {
      setValue(PUBLISHED, "");
    }
    else
    {
      setValue(PUBLISHED, java.lang.Boolean.toString(value));
    }
  }
  
  public Long getSeq()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(SEQ));
  }
  
  public void validateSeq()
  {
    this.validateAttribute(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public String getSiteMaster()
  {
    return getValue(SITEMASTER);
  }
  
  public void validateSiteMaster()
  {
    this.validateAttribute(SITEMASTER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getSiteMasterMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(SITEMASTER);
  }
  
  public String getType()
  {
    return getValue(TYPE);
  }
  
  public void validateType()
  {
    this.validateAttribute(TYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Product.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(TYPE);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static ProductQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    ProductQuery query = new ProductQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public gov.geoplatform.uasdm.bus.ProductHasDocument addDocuments(gov.geoplatform.uasdm.bus.Document document)
  {
    return (gov.geoplatform.uasdm.bus.ProductHasDocument) addChild(document, gov.geoplatform.uasdm.bus.ProductHasDocument.CLASS);
  }
  
  public void removeDocuments(gov.geoplatform.uasdm.bus.Document document)
  {
    removeAllChildren(document, gov.geoplatform.uasdm.bus.ProductHasDocument.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.Document> getAllDocuments()
  {
    return (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.Document>) getChildren(gov.geoplatform.uasdm.bus.ProductHasDocument.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.ProductHasDocument> getAllDocumentsRel()
  {
    return (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.ProductHasDocument>) getChildRelationships(gov.geoplatform.uasdm.bus.ProductHasDocument.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public gov.geoplatform.uasdm.bus.ProductHasDocument getDocumentsRel(gov.geoplatform.uasdm.bus.Document document)
  {
    com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.ProductHasDocument> iterator = (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.ProductHasDocument>) getRelationshipsWithChild(document, gov.geoplatform.uasdm.bus.ProductHasDocument.CLASS);
    try
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
      else
      {
        return null;
      }
    }
    finally
    {
      iterator.close();
    }
  }
  
  public gov.geoplatform.uasdm.bus.DocumentGeneratedProduct addGeneratedDocuments(gov.geoplatform.uasdm.bus.Document document)
  {
    return (gov.geoplatform.uasdm.bus.DocumentGeneratedProduct) addParent(document, gov.geoplatform.uasdm.bus.DocumentGeneratedProduct.CLASS);
  }
  
  public void removeGeneratedDocuments(gov.geoplatform.uasdm.bus.Document document)
  {
    removeAllParents(document, gov.geoplatform.uasdm.bus.DocumentGeneratedProduct.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.Document> getAllGeneratedDocuments()
  {
    return (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.Document>) getParents(gov.geoplatform.uasdm.bus.DocumentGeneratedProduct.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.DocumentGeneratedProduct> getAllGeneratedDocumentsRel()
  {
    return (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.DocumentGeneratedProduct>) getParentRelationships(gov.geoplatform.uasdm.bus.DocumentGeneratedProduct.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public gov.geoplatform.uasdm.bus.DocumentGeneratedProduct getGeneratedDocumentsRel(gov.geoplatform.uasdm.bus.Document document)
  {
    com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.DocumentGeneratedProduct> iterator = (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.DocumentGeneratedProduct>) getRelationshipsWithParent(document, gov.geoplatform.uasdm.bus.DocumentGeneratedProduct.CLASS);
    try
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
      else
      {
        return null;
      }
    }
    finally
    {
      iterator.close();
    }
  }
  
  public static Product get(String oid)
  {
    return (Product) com.runwaysdk.business.Business.get(oid);
  }
  
  public static Product getByKey(String key)
  {
    return (Product) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static Product lock(java.lang.String oid)
  {
    Product _instance = Product.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static Product unlock(java.lang.String oid)
  {
    Product _instance = Product.get(oid);
    _instance.unlock();
    
    return _instance;
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}
