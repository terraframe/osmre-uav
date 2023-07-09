package gov.geoplatform.uasdm.processing.geolocation;

import java.util.ArrayList;
import java.util.List;

public class GeoLocationValidationResults
{
  private List<String> errors = new ArrayList<String>();

  public GeoLocationValidationResults()
  {
    
  }
  
  public boolean hasErrors()
  {
    return errors.size() > 0;
  }
  
  public List<String> getErrors()
  {
    return errors;
  }

  public void setErrors(List<String> errors)
  {
    this.errors = errors;
  }
  
  public void addError(String error)
  {
    this.errors.add(error);
  }
}
