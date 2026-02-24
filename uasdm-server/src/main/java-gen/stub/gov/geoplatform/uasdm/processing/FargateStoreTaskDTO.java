package gov.geoplatform.uasdm.processing;

public class FargateStoreTaskDTO extends FargateStoreTaskDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -628562584;
  
  public FargateStoreTaskDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected FargateStoreTaskDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
