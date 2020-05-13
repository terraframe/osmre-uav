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

@com.runwaysdk.business.ClassSignature(hash = -953665935)
public abstract class ProjectDTOBase extends gov.geoplatform.uasdm.bus.UasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Project";
  private static final long serialVersionUID = -953665935;
  
  protected ProjectDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ProjectDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ImageryDTO> getAllImagery()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ImageryDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ImageryDTO> getAllImagery(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ImageryDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO> getAllImageryRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO> getAllImageryRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasImageryDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.ProjectHasImageryDTO addImagery(gov.geoplatform.uasdm.bus.ImageryDTO child)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasImageryDTO addImagery(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.ImageryDTO child)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasImageryDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public void removeImagery(gov.geoplatform.uasdm.bus.ProjectHasImageryDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeImagery(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllImagery()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  public static void removeAllImagery(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.bus.ProjectHasImageryDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getAllMissions()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>) getRequest().getChildren(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO> getAllMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.MissionDTO>) clientRequestIF.getChildren(oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO> getAllMissionsRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO>) getRequest().getChildRelationships(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO> getAllMissionsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.ProjectHasMissionDTO>) clientRequestIF.getChildRelationships(oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.ProjectHasMissionDTO addMissions(gov.geoplatform.uasdm.bus.MissionDTO child)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasMissionDTO) getRequest().addChild(this.getOid(), child.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectHasMissionDTO addMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.MissionDTO child)
  {
    return (gov.geoplatform.uasdm.bus.ProjectHasMissionDTO) clientRequestIF.addChild(oid, child.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public void removeMissions(gov.geoplatform.uasdm.bus.ProjectHasMissionDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllMissions()
  {
    getRequest().deleteChildren(this.getOid(), gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  public static void removeAllMissions(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, gov.geoplatform.uasdm.bus.ProjectHasMissionDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.SiteDTO> getAllSite()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.SiteDTO>) getRequest().getParents(this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.SiteDTO> getAllSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.SiteDTO>) clientRequestIF.getParents(oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO> getAllSiteRelationships()
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO>) getRequest().getParentRelationships(this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO> getAllSiteRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends gov.geoplatform.uasdm.bus.SiteHasProjectsDTO>) clientRequestIF.getParentRelationships(oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public gov.geoplatform.uasdm.bus.SiteHasProjectsDTO addSite(gov.geoplatform.uasdm.bus.SiteDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) getRequest().addParent(parent.getOid(), this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsDTO addSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, gov.geoplatform.uasdm.bus.SiteDTO parent)
  {
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) clientRequestIF.addParent(parent.getOid(), oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public void removeSite(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllSite()
  {
    getRequest().deleteParents(this.getOid(), gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public static void removeAllSite(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.ProjectDTO) dto;
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
  
  public static gov.geoplatform.uasdm.bus.ProjectQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.ProjectQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.ProjectDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ProjectDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ProjectDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.ProjectDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.ProjectDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.ProjectDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
