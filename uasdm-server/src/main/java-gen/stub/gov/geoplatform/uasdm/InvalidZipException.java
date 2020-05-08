package gov.geoplatform.uasdm;

public class InvalidZipException extends InvalidZipExceptionBase
{
  private static final long serialVersionUID = -38592636;
  
  public InvalidZipException()
  {
    super();
  }
  
  public InvalidZipException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidZipException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidZipException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
