package gov.geoplatform.uasdm.tile;

@com.runwaysdk.business.ClassSignature(hash = 535005097)
public abstract class TileServiceAuthenticatorDTOBase extends com.runwaysdk.business.UtilDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.tile.TileServiceAuthenticator";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 535005097;
  
  protected TileServiceAuthenticatorDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public final void authenticate()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.tile.TileServiceAuthenticatorDTO.CLASS, "authenticate", _declaredTypes);
    getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final void authenticate(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.tile.TileServiceAuthenticatorDTO.CLASS, "authenticate", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static TileServiceAuthenticatorDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.UtilDTO dto = (com.runwaysdk.business.UtilDTO)clientRequest.get(oid);
    
    return (TileServiceAuthenticatorDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createSessionComponent(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
}
