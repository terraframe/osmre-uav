package gov.geoplatform.uasdm.processing;

public class ProcessingInProgressException extends ProcessingInProgressExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1350188862;
  
  public ProcessingInProgressException()
  {
    super();
  }
  
  public ProcessingInProgressException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public ProcessingInProgressException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public ProcessingInProgressException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
