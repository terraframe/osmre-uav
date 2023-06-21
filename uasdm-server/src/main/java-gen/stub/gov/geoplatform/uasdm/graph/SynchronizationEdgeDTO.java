package gov.geoplatform.uasdm.graph;

public class SynchronizationEdgeDTO extends SynchronizationEdgeDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -565560064;
  
  public SynchronizationEdgeDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected SynchronizationEdgeDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
