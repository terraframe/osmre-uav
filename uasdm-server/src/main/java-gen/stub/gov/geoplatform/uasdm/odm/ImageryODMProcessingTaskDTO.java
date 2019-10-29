package gov.geoplatform.uasdm.odm;

public class ImageryODMProcessingTaskDTO extends ImageryODMProcessingTaskDTOBase
{
  private static final long serialVersionUID = -28375767;
  
  public ImageryODMProcessingTaskDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImageryODMProcessingTaskDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
