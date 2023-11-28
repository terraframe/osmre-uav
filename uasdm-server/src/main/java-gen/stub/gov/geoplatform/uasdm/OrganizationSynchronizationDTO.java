package gov.geoplatform.uasdm;

public class OrganizationSynchronizationDTO extends OrganizationSynchronizationDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1597106059;
  
  public OrganizationSynchronizationDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected OrganizationSynchronizationDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
