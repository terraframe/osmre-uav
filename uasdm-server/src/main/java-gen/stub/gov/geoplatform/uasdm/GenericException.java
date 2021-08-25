package gov.geoplatform.uasdm;

public class GenericException extends GenericExceptionBase
{
  private static final long serialVersionUID = 680944847;
  
  public GenericException()
  {
    super();
  }
  
  public GenericException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public GenericException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public GenericException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
