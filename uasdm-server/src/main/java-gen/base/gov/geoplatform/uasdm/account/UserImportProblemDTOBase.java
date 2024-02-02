package gov.geoplatform.uasdm.account;

@com.runwaysdk.business.ClassSignature(hash = 2129242113)
public abstract class UserImportProblemDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.account.UserImportProblem";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 2129242113;
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected UserImportProblemDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public UserImportProblemDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String LINE = "line";
  public static java.lang.String OID = "oid";
  public static java.lang.String REASON = "reason";
  public Integer getLine()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LINE));
  }
  
  public void setLine(Integer value)
  {
    if(value == null)
    {
      setValue(LINE, "");
    }
    else
    {
      setValue(LINE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLineWritable()
  {
    return isWritable(LINE);
  }
  
  public boolean isLineReadable()
  {
    return isReadable(LINE);
  }
  
  public boolean isLineModified()
  {
    return isModified(LINE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLineMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LINE).getAttributeMdDTO();
  }
  
  public String getReason()
  {
    return getValue(REASON);
  }
  
  public void setReason(String value)
  {
    if(value == null)
    {
      setValue(REASON, "");
    }
    else
    {
      setValue(REASON, value);
    }
  }
  
  public boolean isReasonWritable()
  {
    return isWritable(REASON);
  }
  
  public boolean isReasonReadable()
  {
    return isReadable(REASON);
  }
  
  public boolean isReasonModified()
  {
    return isModified(REASON);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getReasonMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(REASON).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{line}", this.getLine().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{reason}", this.getReason().toString());
    
    return template;
  }
  
}
