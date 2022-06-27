package gov.geoplatform.uasdm.odm;

public class UnexpectedODMResponseException extends RuntimeException
{

  public static final String MESSAGE = "Unexpected response from ODM [{odm_resp}]";
  
  private String resp;
  
  public UnexpectedODMResponseException(String resp)
  {
    super();
    this.resp = resp;
  }
  
  public UnexpectedODMResponseException(String resp, Throwable cause)
  {
    super(cause);
    this.resp = resp;
  }
  
  @Override
  public String getMessage()
  {
    return MESSAGE.replaceFirst("\\{odm_resp\\}", this.resp == null ? "" : this.resp);
  }
  
}
