package gov.geoplatform.uasdm.odm;

public interface ODMResponse
{

  boolean hasError();

  Response getHTTPResponse();

  String getError();

}