package gov.geoplatform.uasdm.bus;

public class ProjectHasImageryDTO extends ProjectHasImageryDTOBase
{
  private static final long serialVersionUID = 600946169;
  
  public ProjectHasImageryDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, String parentOid, String childOid)
  {
    super(clientRequest, parentOid, childOid);
    
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given RelationshipDTO into a new DTO.
  * 
  * @param relationshipDTO The RelationshipDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ProjectHasImageryDTO(com.runwaysdk.business.RelationshipDTO relationshipDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(relationshipDTO, clientRequest);
  }
  
}
