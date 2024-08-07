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
package gov.geoplatform.uasdm.graph;

@com.runwaysdk.business.ClassSignature(hash = -229712764)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to Document.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class DocumentBase extends com.runwaysdk.business.graph.VertexObject
{
  public final static String CLASS = "gov.geoplatform.uasdm.graph.Document";
  public final static java.lang.String DESCRIPTION = "description";
  public final static java.lang.String EXCLUDE = "exclude";
  public final static java.lang.String FILESIZE = "fileSize";
  public final static java.lang.String LASTMODIFIED = "lastModified";
  public final static java.lang.String NAME = "name";
  public final static java.lang.String OID = "oid";
  public final static java.lang.String ORTHOCORRECTIONMODEL = "orthoCorrectionModel";
  public final static java.lang.String PROJECTIONNAME = "projectionName";
  public final static java.lang.String PTEPSG = "ptEpsg";
  public final static java.lang.String S3LOCATION = "s3location";
  public final static java.lang.String SEQ = "seq";
  public final static java.lang.String TOOL = "tool";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -229712764;
  
  public DocumentBase()
  {
    super();
  }
  
  public String getDescription()
  {
    return (String) this.getObjectValue(DESCRIPTION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getDescriptionMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(DESCRIPTION);
  }
  
  public void setDescription(String value)
  {
    this.setValue(DESCRIPTION, value);
  }
  
  public Boolean getExclude()
  {
    return (Boolean) this.getObjectValue(EXCLUDE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF getExcludeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF)mdClassIF.definesAttribute(EXCLUDE);
  }
  
  public void setExclude(Boolean value)
  {
    this.setValue(EXCLUDE, value);
  }
  
  public Long getFileSize()
  {
    return (Long) this.getObjectValue(FILESIZE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getFileSizeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(FILESIZE);
  }
  
  public void setFileSize(Long value)
  {
    this.setValue(FILESIZE, value);
  }
  
  public java.util.Date getLastModified()
  {
    return (java.util.Date) this.getObjectValue(LASTMODIFIED);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getLastModifiedMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(LASTMODIFIED);
  }
  
  public void setLastModified(java.util.Date value)
  {
    this.setValue(LASTMODIFIED, value);
  }
  
  public String getName()
  {
    return (String) this.getObjectValue(NAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(NAME);
  }
  
  public void setName(String value)
  {
    this.setValue(NAME, value);
  }
  
  public String getOid()
  {
    return (String) this.getObjectValue(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getOrthoCorrectionModel()
  {
    return (String) this.getObjectValue(ORTHOCORRECTIONMODEL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getOrthoCorrectionModelMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(ORTHOCORRECTIONMODEL);
  }
  
  public void setOrthoCorrectionModel(String value)
  {
    this.setValue(ORTHOCORRECTIONMODEL, value);
  }
  
  public String getProjectionName()
  {
    return (String) this.getObjectValue(PROJECTIONNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getProjectionNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(PROJECTIONNAME);
  }
  
  public void setProjectionName(String value)
  {
    this.setValue(PROJECTIONNAME, value);
  }
  
  public Integer getPtEpsg()
  {
    return (Integer) this.getObjectValue(PTEPSG);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF getPtEpsgMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF)mdClassIF.definesAttribute(PTEPSG);
  }
  
  public void setPtEpsg(Integer value)
  {
    this.setValue(PTEPSG, value);
  }
  
  public String getS3location()
  {
    return (String) this.getObjectValue(S3LOCATION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getS3locationMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(S3LOCATION);
  }
  
  public void setS3location(String value)
  {
    this.setValue(S3LOCATION, value);
  }
  
  public Long getSeq()
  {
    return (Long) this.getObjectValue(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public void setSeq(Long value)
  {
    this.setValue(SEQ, value);
  }
  
  public String getTool()
  {
    return (String) this.getObjectValue(TOOL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getToolMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.Document.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(TOOL);
  }
  
  public void setTool(String value)
  {
    this.setValue(TOOL, value);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public com.runwaysdk.business.graph.EdgeObject addDocumentGeneratedProductChild(gov.geoplatform.uasdm.graph.Product product)
  {
    return super.addChild(product, "gov.geoplatform.uasdm.graph.DocumentGeneratedProduct");
  }
  
  public void removeDocumentGeneratedProductChild(gov.geoplatform.uasdm.graph.Product product)
  {
    super.removeChild(product, "gov.geoplatform.uasdm.graph.DocumentGeneratedProduct");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.Product> getDocumentGeneratedProductChildProducts()
  {
    return super.getChildren("gov.geoplatform.uasdm.graph.DocumentGeneratedProduct",gov.geoplatform.uasdm.graph.Product.class);
  }
  
  public com.runwaysdk.business.graph.EdgeObject addODMRunInputChild(gov.geoplatform.uasdm.graph.ODMRun oDMRun)
  {
    return super.addChild(oDMRun, "gov.geoplatform.uasdm.graph.ODMRunInput");
  }
  
  public void removeODMRunInputChild(gov.geoplatform.uasdm.graph.ODMRun oDMRun)
  {
    super.removeChild(oDMRun, "gov.geoplatform.uasdm.graph.ODMRunInput");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.ODMRun> getODMRunInputChildODMRuns()
  {
    return super.getChildren("gov.geoplatform.uasdm.graph.ODMRunInput",gov.geoplatform.uasdm.graph.ODMRun.class);
  }
  
  public com.runwaysdk.business.graph.EdgeObject addComponentHasDocumentParent(gov.geoplatform.uasdm.graph.UasComponent uasComponent)
  {
    return super.addParent(uasComponent, "gov.geoplatform.uasdm.graph.ComponentHasDocument");
  }
  
  public void removeComponentHasDocumentParent(gov.geoplatform.uasdm.graph.UasComponent uasComponent)
  {
    super.removeParent(uasComponent, "gov.geoplatform.uasdm.graph.ComponentHasDocument");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.UasComponent> getComponentHasDocumentParentUasComponents()
  {
    return super.getParents("gov.geoplatform.uasdm.graph.ComponentHasDocument", gov.geoplatform.uasdm.graph.UasComponent.class);
  }
  
  public com.runwaysdk.business.graph.EdgeObject addODMRunOutputParent(gov.geoplatform.uasdm.graph.ODMRun oDMRun)
  {
    return super.addParent(oDMRun, "gov.geoplatform.uasdm.graph.ODMRunOutput");
  }
  
  public void removeODMRunOutputParent(gov.geoplatform.uasdm.graph.ODMRun oDMRun)
  {
    super.removeParent(oDMRun, "gov.geoplatform.uasdm.graph.ODMRunOutput");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.ODMRun> getODMRunOutputParentODMRuns()
  {
    return super.getParents("gov.geoplatform.uasdm.graph.ODMRunOutput", gov.geoplatform.uasdm.graph.ODMRun.class);
  }
  
  public com.runwaysdk.business.graph.EdgeObject addProductHasDocumentParent(gov.geoplatform.uasdm.graph.Product product)
  {
    return super.addParent(product, "gov.geoplatform.uasdm.graph.ProductHasDocument");
  }
  
  public void removeProductHasDocumentParent(gov.geoplatform.uasdm.graph.Product product)
  {
    super.removeParent(product, "gov.geoplatform.uasdm.graph.ProductHasDocument");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.Product> getProductHasDocumentParentProducts()
  {
    return super.getParents("gov.geoplatform.uasdm.graph.ProductHasDocument", gov.geoplatform.uasdm.graph.Product.class);
  }
  
  public static Document get(String oid)
  {
    return (Document) com.runwaysdk.business.graph.VertexObject.get(CLASS, oid);
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
