package gov.geoplatform.uasdm.processing.geolocation;

public class GeoLocationFileInvalidFormatException extends RuntimeException
{

  private static final long serialVersionUID = -2508082579554640833L;
  
  private String reason;
  
  public GeoLocationFileInvalidFormatException(String reason)
  {
    this.reason = reason;
  }
  
  public GeoLocationFileInvalidFormatException(String reason, Throwable cause)
  {
    super(cause);
    this.reason = reason;
  }
  
  @Override
  public String getMessage()
  {
    return "Geo location file did not match the expected file format. " + this.reason;
  }
  
}