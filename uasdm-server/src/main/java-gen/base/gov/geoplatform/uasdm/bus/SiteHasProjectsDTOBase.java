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

@com.runwaysdk.business.ClassSignature(hash = 2145746959)
public abstract class SiteHasProjectsDTOBase extends gov.geoplatform.uasdm.bus.ComponentHasComponentDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.SiteHasProjects";
  private static final long serialVersionUID = 2145746959;
  
  public SiteHasProjectsDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String parentOid, java.lang.String childOid)
  {
    super(clientRequest, parentOid, childOid);
    
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given RelationshipDTO into a new DTO.
  * 
  * @param relationshipDTO The RelationshipDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected SiteHasProjectsDTOBase(com.runwaysdk.business.RelationshipDTO relationshipDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(relationshipDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public gov.geoplatform.uasdm.bus.SiteDTO getParent()
  {
    return gov.geoplatform.uasdm.bus.SiteDTO.get(getRequest(), super.getParentOid());
  }
  
    public gov.geoplatform.uasdm.bus.ProjectDTO getChild()
  {
    return gov.geoplatform.uasdm.bus.ProjectDTO.get(getRequest(), super.getChildOid());
  }
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.RelationshipDTO dto = (com.runwaysdk.business.RelationshipDTO) clientRequest.get(oid);
    
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) dto;
  }
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsQueryDTO parentQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String parentOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
    queryDTO.addCondition("parent_oid", "EQ", parentOid);
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsQueryDTO) clientRequest.queryRelationships(queryDTO);
  }
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsQueryDTO childQuery(com.runwaysdk.constants.ClientRequestIF clientRequest, String childOid)
  {
    com.runwaysdk.business.RelationshipQueryDTO queryDTO = (com.runwaysdk.business.RelationshipQueryDTO) clientRequest.getQuery(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS);
    queryDTO.addCondition("child_oid", "EQ", childOid);
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsQueryDTO) clientRequest.queryRelationships(queryDTO);
  }
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createRelationship(this);
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
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsQueryDTO) clientRequest.getAllInstances(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS, "lock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static gov.geoplatform.uasdm.bus.SiteHasProjectsDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.bus.SiteHasProjectsDTO.CLASS, "unlock", _declaredTypes);
    return (gov.geoplatform.uasdm.bus.SiteHasProjectsDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
