package gov.geoplatform.uasdm.keycloak;

public class UnassignedKeycloakUserException extends UnassignedKeycloakUserExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 2040858520;
  
  public UnassignedKeycloakUserException()
  {
    super();
  }
  
  public UnassignedKeycloakUserException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UnassignedKeycloakUserException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UnassignedKeycloakUserException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
