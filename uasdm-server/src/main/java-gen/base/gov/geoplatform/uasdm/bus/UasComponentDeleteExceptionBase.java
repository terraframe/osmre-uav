/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -414615060)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to UasComponentDeleteException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class UasComponentDeleteExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.UasComponentDeleteException";
  public static java.lang.String COMPONENTNAME = "componentName";
  public static java.lang.String OID = "oid";
  public static java.lang.String TYPELABEL = "typeLabel";
  private static final long serialVersionUID = -414615060;
  
  public UasComponentDeleteExceptionBase()
  {
    super();
  }
  
  public UasComponentDeleteExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UasComponentDeleteExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UasComponentDeleteExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getComponentName()
  {
    return getValue(COMPONENTNAME);
  }
  
  public void validateComponentName()
  {
    this.validateAttribute(COMPONENTNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getComponentNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.UasComponentDeleteException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(COMPONENTNAME);
  }
  
  public void setComponentName(String value)
  {
    if(value == null)
    {
      setValue(COMPONENTNAME, "");
    }
    else
    {
      setValue(COMPONENTNAME, value);
    }
  }
  
  public String getOid()
  {
    return getValue(OID);
  }
  
  public void validateOid()
  {
    this.validateAttribute(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.UasComponentDeleteException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getTypeLabel()
  {
    return getValue(TYPELABEL);
  }
  
  public void validateTypeLabel()
  {
    this.validateAttribute(TYPELABEL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getTypeLabelMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.UasComponentDeleteException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(TYPELABEL);
  }
  
  public void setTypeLabel(String value)
  {
    if(value == null)
    {
      setValue(TYPELABEL, "");
    }
    else
    {
      setValue(TYPELABEL, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{componentName}", this.getComponentName());
    message = replace(message, "{oid}", this.getOid());
    message = replace(message, "{typeLabel}", this.getTypeLabel());
    return message;
  }
  
}
