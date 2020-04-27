package gov.geoplatform.uasdm.bus;

public class InvalidPasswordException extends InvalidPasswordExceptionBase
{
  private static final long serialVersionUID = -2144855266;
  
  public InvalidPasswordException()
  {
    super();
  }
  
  public InvalidPasswordException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidPasswordException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidPasswordException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
