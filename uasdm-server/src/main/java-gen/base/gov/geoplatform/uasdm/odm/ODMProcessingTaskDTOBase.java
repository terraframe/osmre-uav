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

@com.runwaysdk.business.ClassSignature(hash = 578709338)
public abstract class ODMProcessingTaskDTOBase extends gov.geoplatform.uasdm.bus.WorkflowTaskDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.odm.ODMProcessingTask";
  private static final long serialVersionUID = 578709338;
  
  protected ODMProcessingTaskDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ODMProcessingTaskDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String FILEPREFIX = "filePrefix";
  public static java.lang.String FILENAMES = "filenames";
  public static java.lang.String ODMOUTPUT = "odmOutput";
  public static java.lang.String ODMUUID = "odmUUID";
  public String getFilePrefix()
  {
    return getValue(FILEPREFIX);
  }
  
  public void setFilePrefix(String value)
  {
    if(value == null)
    {
      setValue(FILEPREFIX, "");
    }
    else
    {
      setValue(FILEPREFIX, value);
    }
  }
  
  public boolean isFilePrefixWritable()
  {
    return isWritable(FILEPREFIX);
  }
  
  public boolean isFilePrefixReadable()
  {
    return isReadable(FILEPREFIX);
  }
  
  public boolean isFilePrefixModified()
  {
    return isModified(FILEPREFIX);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getFilePrefixMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(FILEPREFIX).getAttributeMdDTO();
  }
  
  public String getFilenames()
  {
    return getValue(FILENAMES);
  }
  
  public void setFilenames(String value)
  {
    if(value == null)
    {
      setValue(FILENAMES, "");
    }
    else
    {
      setValue(FILENAMES, value);
    }
  }
  
  public boolean isFilenamesWritable()
  {
    return isWritable(FILENAMES);
  }
  
  public boolean isFilenamesReadable()
  {
    return isReadable(FILENAMES);
  }
  
  public boolean isFilenamesModified()
  {
    return isModified(FILENAMES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getFilenamesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(FILENAMES).getAttributeMdDTO();
  }
  
  public String getOdmOutput()
  {
    return getValue(ODMOUTPUT);
  }
  
  public void setOdmOutput(String value)
  {
    if(value == null)
    {
      setValue(ODMOUTPUT, "");
    }
    else
    {
      setValue(ODMOUTPUT, value);
    }
  }
  
  public boolean isOdmOutputWritable()
  {
    return isWritable(ODMOUTPUT);
  }
  
  public boolean isOdmOutputReadable()
  {
    return isReadable(ODMOUTPUT);
  }
  
  public boolean isOdmOutputModified()
  {
    return isModified(ODMOUTPUT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getOdmOutputMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ODMOUTPUT).getAttributeMdDTO();
  }
  
  public String getOdmUUID()
  {
    return getValue(ODMUUID);
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
  
  public boolean isOdmUUIDWritable()
  {
    return isWritable(ODMUUID);
  }
  
  public boolean isOdmUUIDReadable()
  {
    return isReadable(ODMUUID);
  }
  
  public boolean isOdmUUIDModified()
  {
    return isModified(ODMUUID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getOdmUUIDMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(ODMUUID).getAttributeMdDTO();
  }
  
  public static gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO) dto;
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
  
  public static gov.geoplatform.uasdm.odm.ODMProcessingTaskQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.odm.ODMProcessingTaskQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.odm.ODMProcessingTaskDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
