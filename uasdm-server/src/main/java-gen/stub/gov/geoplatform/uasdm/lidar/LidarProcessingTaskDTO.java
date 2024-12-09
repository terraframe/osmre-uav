package gov.geoplatform.uasdm.lidar;

public class LidarProcessingTaskDTO extends LidarProcessingTaskDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1672705179;
  
  public LidarProcessingTaskDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected LidarProcessingTaskDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
