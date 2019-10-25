package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = 1398124292)
public abstract class DuplicateComponentExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.DuplicateComponentException";
  public static final long serialVersionUID = 1398124292;
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateComponentExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateComponentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CHILDCOMPONENTLABEL = "childComponentLabel";
  public static java.lang.String CHILDNAME = "childName";
  public static java.lang.String OID = "oid";
  public static java.lang.String PARENTNAME = "parentName";
  public String getChildComponentLabel()
  {
    return getValue(CHILDCOMPONENTLABEL);
  }
  
  public void setChildComponentLabel(String value)
  {
    if(value == null)
    {
      setValue(CHILDCOMPONENTLABEL, "");
    }
    else
    {
      setValue(CHILDCOMPONENTLABEL, value);
    }
  }
  
  public boolean isChildComponentLabelWritable()
  {
    return isWritable(CHILDCOMPONENTLABEL);
  }
  
  public boolean isChildComponentLabelReadable()
  {
    return isReadable(CHILDCOMPONENTLABEL);
  }
  
  public boolean isChildComponentLabelModified()
  {
    return isModified(CHILDCOMPONENTLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getChildComponentLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CHILDCOMPONENTLABEL).getAttributeMdDTO();
  }
  
  public String getChildName()
  {
    return getValue(CHILDNAME);
  }
  
  public void setChildName(String value)
  {
    if(value == null)
    {
      setValue(CHILDNAME, "");
    }
    else
    {
      setValue(CHILDNAME, value);
    }
  }
  
  public boolean isChildNameWritable()
  {
    return isWritable(CHILDNAME);
  }
  
  public boolean isChildNameReadable()
  {
    return isReadable(CHILDNAME);
  }
  
  public boolean isChildNameModified()
  {
    return isModified(CHILDNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getChildNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CHILDNAME).getAttributeMdDTO();
  }
  
  public String getParentName()
  {
    return getValue(PARENTNAME);
  }
  
  public void setParentName(String value)
  {
    if(value == null)
    {
      setValue(PARENTNAME, "");
    }
    else
    {
      setValue(PARENTNAME, value);
    }
  }
  
  public boolean isParentNameWritable()
  {
    return isWritable(PARENTNAME);
  }
  
  public boolean isParentNameReadable()
  {
    return isReadable(PARENTNAME);
  }
  
  public boolean isParentNameModified()
  {
    return isModified(PARENTNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getParentNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PARENTNAME).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{childComponentLabel}", this.getChildComponentLabel().toString());
    template = template.replace("{childName}", this.getChildName().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{parentName}", this.getParentName().toString());
    
    return template;
  }
  
}
