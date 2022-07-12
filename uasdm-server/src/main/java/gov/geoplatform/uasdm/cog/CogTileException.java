package gov.geoplatform.uasdm.cog;

public class CogTileException extends RuntimeException
{
  public CogTileException(String msg)
  {
    super(msg);
  }
  
  public CogTileException(String msg, Throwable t)
  {
    super(msg, t);
  }
  
  public CogTileException(Throwable t)
  {
    super(t);
  }
}
