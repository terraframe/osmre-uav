package gov.geoplatform.uasdm.model;

public class InvalidRangeException extends RuntimeException
{
  /**
   * 
   */
  private static final long serialVersionUID = -5915626704701087049L;

  private String            range;

  public InvalidRangeException(String range)
  {
    this.range = range;
  }

  public String getRange()
  {
    return range;
  }
}
