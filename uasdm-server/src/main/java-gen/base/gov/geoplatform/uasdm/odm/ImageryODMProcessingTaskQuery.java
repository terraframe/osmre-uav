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
package gov.geoplatform.uasdm.odm;

@com.runwaysdk.business.ClassSignature(hash = -1151049901)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImageryODMProcessingTask.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class ImageryODMProcessingTaskQuery extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery

{

  public ImageryODMProcessingTaskQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public ImageryODMProcessingTaskQuery(com.runwaysdk.query.ValueQuery valueQuery)
  {
    super(valueQuery);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = new com.runwaysdk.business.BusinessQuery(valueQuery, this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public String getClassType()
  {
    return gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CLASS;
  }
  public com.runwaysdk.query.SelectableChar getConfigurationJson()
  {
    return getConfigurationJson(null);

  }
 
  public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CONFIGURATIONJSON, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CONFIGURATIONJSON, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getFilePrefix()
  {
    return getFilePrefix(null);

  }
 
  public com.runwaysdk.query.SelectableChar getFilePrefix(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.FILEPREFIX, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getFilePrefix(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.FILEPREFIX, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOdmOutput()
  {
    return getOdmOutput(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmOutput(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMOUTPUT, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmOutput(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMOUTPUT, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOdmUUID()
  {
    return getOdmUUID(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMUUID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMUUID, alias, displayLabel);

  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends ImageryODMProcessingTask> getIterator()
  {
    this.checkNotUsedInValueQuery();
    String sqlStmt;
    if (_limit != null && _skip != null)
    {
      sqlStmt = this.getComponentQuery().getSQL(_limit, _skip);
    }
    else
    {
      sqlStmt = this.getComponentQuery().getSQL();
    }
    java.util.Map<String, com.runwaysdk.query.ColumnInfo> columnInfoMap = this.getComponentQuery().getColumnInfoMap();

    java.sql.ResultSet results = com.runwaysdk.dataaccess.database.Database.query(sqlStmt);
    return new com.runwaysdk.business.BusinessIterator<ImageryODMProcessingTask>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ImageryODMProcessingTaskQueryReferenceIF extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getConfigurationJson();
    public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias);
    public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getFilePrefix();
    public com.runwaysdk.query.SelectableChar getFilePrefix(String alias);
    public com.runwaysdk.query.SelectableChar getFilePrefix(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getOdmOutput();
    public com.runwaysdk.query.SelectableChar getOdmOutput(String alias);
    public com.runwaysdk.query.SelectableChar getOdmOutput(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getOdmUUID();
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias);
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask imageryODMProcessingTask);

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask imageryODMProcessingTask);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ImageryODMProcessingTaskQueryReference extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryReference
 implements ImageryODMProcessingTaskQueryReferenceIF

  {

  public ImageryODMProcessingTaskQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask imageryODMProcessingTask)
    {
      if(imageryODMProcessingTask == null) return this.EQ((java.lang.String)null);
      return this.EQ(imageryODMProcessingTask.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask imageryODMProcessingTask)
    {
      if(imageryODMProcessingTask == null) return this.NE((java.lang.String)null);
      return this.NE(imageryODMProcessingTask.getOid());
    }

  public com.runwaysdk.query.SelectableChar getConfigurationJson()
  {
    return getConfigurationJson(null);

  }
 
  public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CONFIGURATIONJSON, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CONFIGURATIONJSON, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getFilePrefix()
  {
    return getFilePrefix(null);

  }
 
  public com.runwaysdk.query.SelectableChar getFilePrefix(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.FILEPREFIX, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getFilePrefix(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.FILEPREFIX, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOdmOutput()
  {
    return getOdmOutput(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmOutput(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMOUTPUT, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmOutput(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMOUTPUT, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOdmUUID()
  {
    return getOdmUUID(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMUUID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMUUID, alias, displayLabel);

  }
  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ImageryODMProcessingTaskQueryMultiReferenceIF extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getConfigurationJson();
    public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias);
    public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getFilePrefix();
    public com.runwaysdk.query.SelectableChar getFilePrefix(String alias);
    public com.runwaysdk.query.SelectableChar getFilePrefix(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getOdmOutput();
    public com.runwaysdk.query.SelectableChar getOdmOutput(String alias);
    public com.runwaysdk.query.SelectableChar getOdmOutput(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getOdmUUID();
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias);
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask);
    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask);
    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask);
    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask);
    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ImageryODMProcessingTaskQueryMultiReference extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryMultiReference
 implements ImageryODMProcessingTaskQueryMultiReferenceIF

  {

  public ImageryODMProcessingTaskQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask)  {

      String[] itemIdArray = new String[imageryODMProcessingTask.length]; 

      for (int i=0; i<imageryODMProcessingTask.length; i++)
      {
        itemIdArray[i] = imageryODMProcessingTask[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask)  {

      String[] itemIdArray = new String[imageryODMProcessingTask.length]; 

      for (int i=0; i<imageryODMProcessingTask.length; i++)
      {
        itemIdArray[i] = imageryODMProcessingTask[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask)  {

      String[] itemIdArray = new String[imageryODMProcessingTask.length]; 

      for (int i=0; i<imageryODMProcessingTask.length; i++)
      {
        itemIdArray[i] = imageryODMProcessingTask[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask)  {

      String[] itemIdArray = new String[imageryODMProcessingTask.length]; 

      for (int i=0; i<imageryODMProcessingTask.length; i++)
      {
        itemIdArray[i] = imageryODMProcessingTask[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask ... imageryODMProcessingTask)  {

      String[] itemIdArray = new String[imageryODMProcessingTask.length]; 

      for (int i=0; i<imageryODMProcessingTask.length; i++)
      {
        itemIdArray[i] = imageryODMProcessingTask[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.query.SelectableChar getConfigurationJson()
  {
    return getConfigurationJson(null);

  }
 
  public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CONFIGURATIONJSON, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getConfigurationJson(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.CONFIGURATIONJSON, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getFilePrefix()
  {
    return getFilePrefix(null);

  }
 
  public com.runwaysdk.query.SelectableChar getFilePrefix(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.FILEPREFIX, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getFilePrefix(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.FILEPREFIX, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOdmOutput()
  {
    return getOdmOutput(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmOutput(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMOUTPUT, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmOutput(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMOUTPUT, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOdmUUID()
  {
    return getOdmUUID(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMUUID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMProcessingTask.ODMUUID, alias, displayLabel);

  }
  }
}
