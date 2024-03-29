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
package gov.geoplatform.uasdm.odm;

@com.runwaysdk.business.ClassSignature(hash = -4166497)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImageryODMUploadTask.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ImageryODMUploadTaskBase extends gov.geoplatform.uasdm.bus.ImageryWorkflowTask
{
  public final static String CLASS = "gov.geoplatform.uasdm.odm.ImageryODMUploadTask";
  public static java.lang.String ODMUUID = "odmUUID";
  public static java.lang.String PROCESSINGTASK = "processingTask";
  private static final long serialVersionUID = -4166497;
  
  public ImageryODMUploadTaskBase()
  {
    super();
  }
  
  public String getOdmUUID()
  {
    return getValue(ODMUUID);
  }
  
  public void validateOdmUUID()
  {
    this.validateAttribute(ODMUUID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getOdmUUIDMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(ODMUUID);
  }
  
  public void setOdmUUID(String value)
  {
    if(value == null)
    {
      setValue(ODMUUID, "");
    }
    else
    {
      setValue(ODMUUID, value);
    }
  }
  
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTask getProcessingTask()
  {
    if (getValue(PROCESSINGTASK).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.get(getValue(PROCESSINGTASK));
    }
  }
  
  public String getProcessingTaskOid()
  {
    return getValue(PROCESSINGTASK);
  }
  
  public void validateProcessingTask()
  {
    this.validateAttribute(PROCESSINGTASK);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getProcessingTaskMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(PROCESSINGTASK);
  }
  
  public void setProcessingTask(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask value)
  {
    if(value == null)
    {
      setValue(PROCESSINGTASK, "");
    }
    else
    {
      setValue(PROCESSINGTASK, value.getOid());
    }
  }
  
  public void setProcessingTaskId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(PROCESSINGTASK, "");
    }
    else
    {
      setValue(PROCESSINGTASK, oid);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static ImageryODMUploadTaskQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    ImageryODMUploadTaskQuery query = new ImageryODMUploadTaskQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static ImageryODMUploadTask get(String oid)
  {
    return (ImageryODMUploadTask) com.runwaysdk.business.Business.get(oid);
  }
  
  public static ImageryODMUploadTask getByKey(String key)
  {
    return (ImageryODMUploadTask) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static ImageryODMUploadTask lock(java.lang.String oid)
  {
    ImageryODMUploadTask _instance = ImageryODMUploadTask.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static ImageryODMUploadTask unlock(java.lang.String oid)
  {
    ImageryODMUploadTask _instance = ImageryODMUploadTask.get(oid);
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
