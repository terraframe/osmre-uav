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

@com.runwaysdk.business.ClassSignature(hash = 230435699)
public abstract class MissionDTOBase extends gov.geoplatform.uasdm.bus.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Mission";
  private static final long serialVersionUID = 230435699;
  
  protected MissionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected MissionDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String METADATAUPLOADED = "metadataUploaded";
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
  public java.util.List<? extends gov.geoplatform.uasdm.bus.CollectionDTO> getAllCollections()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.CollectionDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.CollectionDTO> getAllCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.CollectionDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO> getAllCollectionsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO> getAllCollectionsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionHasCollectionDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.MissionHasCollectionDTO addCollections(gov.geoplatform.uasdm.bus.CollectionDTO child)
  {
    return (gov.geoplatform.uasdm.bus.MissionHasCollectionDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.MissionHasCollectionDTO addCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.CollectionDTO child)
  {
    return (gov.geoplatform.uasdm.bus.MissionHasCollectionDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public void removeCollections(gov.geoplatform.uasdm.bus.MissionHasCollectionDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllCollections()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  public static void removeAllCollections(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.bus.MissionHasCollectionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO> getAllProject()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO> getAllProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO> getAllProjectRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO> getAllProjectRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.ProjectHasMissionDTO addProject(gov.geoplatform.uasdm.bus.ProjectDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasMissionDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasMissionDTO addProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.ProjectDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasMissionDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public void removeProject(gov.geoplatform.uasdm.bus.ProjectHasMissionDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllProject()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public static void removeAllProject(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.MissionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.MissionDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.MissionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.MissionQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.MissionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.MissionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.MissionDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.MissionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.MissionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.MissionDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.MissionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
