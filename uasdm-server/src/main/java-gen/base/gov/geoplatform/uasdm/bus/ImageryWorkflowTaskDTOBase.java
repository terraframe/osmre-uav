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

@com.runwaysdk.business.ClassSignature(hash = -143209710)
public abstract class ImageryWorkflowTaskDTOBase extends gov.geoplatform.uasdm.bus.AbstractUploadTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.ImageryWorkflowTask";
  private static final long serialVersionUID = -143209710;
  
  protected ImageryWorkflowTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImageryWorkflowTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String IMAGERY = "imagery";
  public String getImagery()
  {
    return getValue(IMAGERY);
  }
  
  public void setImagery(String value)
  {
    if(value == null)
    {
      setValue(IMAGERY, "");
    }
    else
    {
      setValue(IMAGERY, value);
    }
  }
  
  public boolean isImageryWritable()
  {
    return isWritable(IMAGERY);
  }
  
  public boolean isImageryReadable()
  {
    return isReadable(IMAGERY);
  }
  
  public boolean isImageryModified()
  {
    return isModified(IMAGERY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeUUIDMdDTO getImageryMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeUUIDMdDTO) getAttributeDTO(IMAGERY).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ImageryWorkflowTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
