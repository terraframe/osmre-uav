package gov.geoplatform.uasdm;

@com.runwaysdk.business.ClassSignature(hash = -2057012140)
public abstract class CannotDeleteProcessingCollectionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.CannotDeleteProcessingCollection";
  private static final long serialVersionUID = -2057012140;
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public CannotDeleteProcessingCollectionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String COLLECTIONNAME = "collectionName";
  public static java.lang.String OID = "oid";
  public String getCollectionName()
  {
    return getValue(COLLECTIONNAME);
  }
  
  public void setCollectionName(String value)
  {
    if(value == null)
    {
      setValue(COLLECTIONNAME, "");
    }
    else
    {
      setValue(COLLECTIONNAME, value);
    }
  }
  
  public boolean isCollectionNameWritable()
  {
    return isWritable(COLLECTIONNAME);
  }
  
  public boolean isCollectionNameReadable()
  {
    return isReadable(COLLECTIONNAME);
  }
  
  public boolean isCollectionNameModified()
  {
    return isModified(COLLECTIONNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCollectionNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(COLLECTIONNAME).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{collectionName}", this.getCollectionName().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
