package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1886134064)
public abstract class DuplicateSiteExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.DuplicateSiteException";
  private static final long serialVersionUID = 1886134064;
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateSiteExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateSiteExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String FOLDERNAME = "folderName";
  public static java.lang.String OID = "oid";
  public String getFolderName()
  {
    return getValue(FOLDERNAME);
  }
  
  public void setFolderName(String value)
  {
    if(value == null)
    {
      setValue(FOLDERNAME, "");
    }
    else
    {
      setValue(FOLDERNAME, value);
    }
  }
  
  public boolean isFolderNameWritable()
  {
    return isWritable(FOLDERNAME);
  }
  
  public boolean isFolderNameReadable()
  {
    return isReadable(FOLDERNAME);
  }
  
  public boolean isFolderNameModified()
  {
    return isModified(FOLDERNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getFolderNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(FOLDERNAME).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{folderName}", this.getFolderName().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
