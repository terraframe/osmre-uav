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

@com.runwaysdk.business.ClassSignature(hash = 224274385)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ODMRun.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ODMRunBase extends com.runwaysdk.business.graph.VertexObject
{
  public final static String CLASS = "gov.geoplatform.uasdm.graph.ODMRun";
  public final static java.lang.String COMPONENT = "component";
  public final static java.lang.String CONFIG = "config";
  public final static java.lang.String OID = "oid";
  public final static java.lang.String OUTPUT = "output";
  public final static java.lang.String REPORT = "report";
  public final static java.lang.String RUNEND = "runEnd";
  public final static java.lang.String RUNSTART = "runStart";
  public final static java.lang.String SEQ = "seq";
  public final static java.lang.String WORKFLOWTASK = "workflowTask";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 224274385;
  
  public ODMRunBase()
  {
    super();
  }
  
  public gov.geoplatform.uasdm.graph.UasComponent getComponent()
  {
    return (gov.geoplatform.uasdm.graph.UasComponent) this.getObjectValue(COMPONENT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getComponentMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(COMPONENT);
  }
  
  public void setComponent(gov.geoplatform.uasdm.graph.UasComponent value)
  {
    this.setValue(COMPONENT, value);
  }
  
  public String getConfig()
  {
    return (String) this.getObjectValue(CONFIG);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getConfigMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CONFIG);
  }
  
  public void setConfig(String value)
  {
    this.setValue(CONFIG, value);
  }
  
  public String getOid()
  {
    return (String) this.getObjectValue(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getOutput()
  {
    return (String) this.getObjectValue(OUTPUT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getOutputMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(OUTPUT);
  }
  
  public void setOutput(String value)
  {
    this.setValue(OUTPUT, value);
  }
  
  public gov.geoplatform.uasdm.graph.Document getReport()
  {
    return (gov.geoplatform.uasdm.graph.Document) this.getObjectValue(REPORT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getReportMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(REPORT);
  }
  
  public void setReport(gov.geoplatform.uasdm.graph.Document value)
  {
    this.setValue(REPORT, value);
  }
  
  public java.util.Date getRunEnd()
  {
    return (java.util.Date) this.getObjectValue(RUNEND);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getRunEndMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(RUNEND);
  }
  
  public void setRunEnd(java.util.Date value)
  {
    this.setValue(RUNEND, value);
  }
  
  public java.util.Date getRunStart()
  {
    return (java.util.Date) this.getObjectValue(RUNSTART);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getRunStartMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(RUNSTART);
  }
  
  public void setRunStart(java.util.Date value)
  {
    this.setValue(RUNSTART, value);
  }
  
  public Long getSeq()
  {
    return (Long) this.getObjectValue(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public void setSeq(Long value)
  {
    this.setValue(SEQ, value);
  }
  
  public gov.geoplatform.uasdm.odm.ODMProcessingTask getWorkflowTask()
  {
    if (this.getObjectValue(WORKFLOWTASK) == null)
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.odm.ODMProcessingTask.get( (String) this.getObjectValue(WORKFLOWTASK));
    }
  }
  
  public String getWorkflowTaskOid()
  {
    return (String) this.getObjectValue(WORKFLOWTASK);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getWorkflowTaskMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.graph.ODMRun.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(WORKFLOWTASK);
  }
  
  public void setWorkflowTask(gov.geoplatform.uasdm.odm.ODMProcessingTask value)
  {
    this.setValue(WORKFLOWTASK, value.getOid());
  }
  
  public void setWorkflowTaskId(java.lang.String oid)
  {
    this.setValue(WORKFLOWTASK, oid);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public com.runwaysdk.business.graph.EdgeObject addODMRunOutputChild(gov.geoplatform.uasdm.graph.Document document)
  {
    return super.addChild(document, "gov.geoplatform.uasdm.graph.ODMRunOutput");
  }
  
  public void removeODMRunOutputChild(gov.geoplatform.uasdm.graph.Document document)
  {
    super.removeChild(document, "gov.geoplatform.uasdm.graph.ODMRunOutput");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.Document> getODMRunOutputChildDocuments()
  {
    return super.getChildren("gov.geoplatform.uasdm.graph.ODMRunOutput",gov.geoplatform.uasdm.graph.Document.class);
  }
  
  public com.runwaysdk.business.graph.EdgeObject addODMRunInputParent(gov.geoplatform.uasdm.graph.Document document)
  {
    return super.addParent(document, "gov.geoplatform.uasdm.graph.ODMRunInput");
  }
  
  public void removeODMRunInputParent(gov.geoplatform.uasdm.graph.Document document)
  {
    super.removeParent(document, "gov.geoplatform.uasdm.graph.ODMRunInput");
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<gov.geoplatform.uasdm.graph.Document> getODMRunInputParentDocuments()
  {
    return super.getParents("gov.geoplatform.uasdm.graph.ODMRunInput", gov.geoplatform.uasdm.graph.Document.class);
  }
  
  public static ODMRun get(String oid)
  {
    return (ODMRun) com.runwaysdk.business.graph.VertexObject.get(CLASS, oid);
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