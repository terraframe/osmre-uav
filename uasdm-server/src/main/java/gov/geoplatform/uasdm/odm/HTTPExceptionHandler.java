package gov.geoplatform.uasdm.odm;

public interface HTTPExceptionHandler
{
  public void uncaughtException(Throwable e) throws RuntimeException;

}
