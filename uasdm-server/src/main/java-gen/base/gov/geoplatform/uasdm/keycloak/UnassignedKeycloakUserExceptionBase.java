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
package gov.geoplatform.uasdm.keycloak;

@com.runwaysdk.business.ClassSignature(hash = 92244283)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to UnassignedKeycloakUserException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class UnassignedKeycloakUserExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "gov.geoplatform.uasdm.keycloak.UnassignedKeycloakUserException";
  public final static java.lang.String EMAIL = "email";
  public final static java.lang.String OID = "oid";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 92244283;
  
  public UnassignedKeycloakUserExceptionBase()
  {
    super();
  }
  
  public UnassignedKeycloakUserExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UnassignedKeycloakUserExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UnassignedKeycloakUserExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getEmail()
  {
    return getValue(EMAIL);
  }
  
  public void validateEmail()
  {
    this.validateAttribute(EMAIL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getEmailMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.keycloak.UnassignedKeycloakUserException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(EMAIL);
  }
  
  public void setEmail(String value)
  {
    if(value == null)
    {
      setValue(EMAIL, "");
    }
    else
    {
      setValue(EMAIL, value);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.keycloak.UnassignedKeycloakUserException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{email}", this.getEmail());
    message = replace(message, "{oid}", this.getOid());
    return message;
  }
  
}