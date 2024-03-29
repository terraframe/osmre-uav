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

@com.runwaysdk.business.ClassSignature(hash = 1298418041)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to Site.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class SiteBase extends gov.geoplatform.uasdm.bus.UasComponent
{
  public final static String CLASS = "gov.geoplatform.uasdm.bus.Site";
  public static java.lang.String BUREAU = "bureau";
  public static java.lang.String OTHERBUREAUTXT = "otherBureauTxt";
  private static final long serialVersionUID = 1298418041;
  
  public SiteBase()
  {
    super();
  }
  
  public gov.geoplatform.uasdm.bus.Bureau getBureau()
  {
    if (getValue(BUREAU).trim().equals(""))
    {
      return null;
    }
    else
    {
      return gov.geoplatform.uasdm.bus.Bureau.get(getValue(BUREAU));
    }
  }
  
  public String getBureauOid()
  {
    return getValue(BUREAU);
  }
  
  public void validateBureau()
  {
    this.validateAttribute(BUREAU);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getBureauMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Site.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(BUREAU);
  }
  
  public void setBureau(gov.geoplatform.uasdm.bus.Bureau value)
  {
    if(value == null)
    {
      setValue(BUREAU, "");
    }
    else
    {
      setValue(BUREAU, value.getOid());
    }
  }
  
  public void setBureauId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(BUREAU, "");
    }
    else
    {
      setValue(BUREAU, oid);
    }
  }
  
  public String getOtherBureauTxt()
  {
    return getValue(OTHERBUREAUTXT);
  }
  
  public void validateOtherBureauTxt()
  {
    this.validateAttribute(OTHERBUREAUTXT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getOtherBureauTxtMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.bus.Site.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(OTHERBUREAUTXT);
  }
  
  public void setOtherBureauTxt(String value)
  {
    if(value == null)
    {
      setValue(OTHERBUREAUTXT, "");
    }
    else
    {
      setValue(OTHERBUREAUTXT, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static SiteQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    SiteQuery query = new SiteQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public gov.geoplatform.uasdm.bus.SiteHasProjects addProjects(gov.geoplatform.uasdm.bus.Project project)
  {
    return (gov.geoplatform.uasdm.bus.SiteHasProjects) addChild(project, gov.geoplatform.uasdm.bus.SiteHasProjects.CLASS);
  }
  
  public void removeProjects(gov.geoplatform.uasdm.bus.Project project)
  {
    removeAllChildren(project, gov.geoplatform.uasdm.bus.SiteHasProjects.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.Project> getAllProjects()
  {
    return (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.Project>) getChildren(gov.geoplatform.uasdm.bus.SiteHasProjects.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.SiteHasProjects> getAllProjectsRel()
  {
    return (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.SiteHasProjects>) getChildRelationships(gov.geoplatform.uasdm.bus.SiteHasProjects.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public gov.geoplatform.uasdm.bus.SiteHasProjects getProjectsRel(gov.geoplatform.uasdm.bus.Project project)
  {
    com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.SiteHasProjects> iterator = (com.runwaysdk.query.OIterator<? extends gov.geoplatform.uasdm.bus.SiteHasProjects>) getRelationshipsWithChild(project, gov.geoplatform.uasdm.bus.SiteHasProjects.CLASS);
    try
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
      else
      {
        return null;
      }
    }
    finally
    {
      iterator.close();
    }
  }
  
  public static Site get(String oid)
  {
    return (Site) com.runwaysdk.business.Business.get(oid);
  }
  
  public static Site getByKey(String key)
  {
    return (Site) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static Site lock(java.lang.String oid)
  {
    Site _instance = Site.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static Site unlock(java.lang.String oid)
  {
    Site _instance = Site.get(oid);
    _instance.unlock();
    
    return _instance;
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}
