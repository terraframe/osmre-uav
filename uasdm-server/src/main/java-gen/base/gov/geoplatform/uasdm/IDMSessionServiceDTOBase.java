package gov.geoplatform.uasdm;

@com.runwaysdk.business.ClassSignature(hash = -2065248324)
public abstract class IDMSessionServiceDTOBase extends com.runwaysdk.business.UtilDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.IDMSessionService";
  private static final long serialVersionUID = -2065248324;
  
  protected IDMSessionServiceDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static final java.lang.String keycloakLogin(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String userJson, java.lang.String roles, java.lang.String locales)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{userJson, roles, locales};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(gov.geoplatform.uasdm.IDMSessionServiceDTO.CLASS, "keycloakLogin", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static IDMSessionServiceDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.UtilDTO dto = (com.runwaysdk.business.UtilDTO)clientRequest.get(oid);
    
    return (IDMSessionServiceDTO) dto;
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
