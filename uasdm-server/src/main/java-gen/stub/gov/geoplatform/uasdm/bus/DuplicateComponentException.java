package gov.geoplatform.uasdm.bus;

public class DuplicateComponentException extends DuplicateComponentExceptionBase
{
  private static final long serialVersionUID = -581529503;
  
  public DuplicateComponentException()
  {
    super();
  }
  
  public DuplicateComponentException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public DuplicateComponentException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public DuplicateComponentException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
