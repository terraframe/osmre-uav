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

@com.runwaysdk.business.ClassSignature(hash = 1336818713)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to OrthoProcessingTask.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class OrthoProcessingTaskQuery extends gov.geoplatform.uasdm.bus.WorkflowTaskQuery

{

  public OrthoProcessingTaskQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public OrthoProcessingTaskQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return gov.geoplatform.uasdm.bus.OrthoProcessingTask.CLASS;
  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends OrthoProcessingTask> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<OrthoProcessingTask>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface OrthoProcessingTaskQueryReferenceIF extends gov.geoplatform.uasdm.bus.WorkflowTaskQuery.WorkflowTaskQueryReferenceIF
  {


    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.bus.OrthoProcessingTask orthoProcessingTask);

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.bus.OrthoProcessingTask orthoProcessingTask);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class OrthoProcessingTaskQueryReference extends gov.geoplatform.uasdm.bus.WorkflowTaskQuery.WorkflowTaskQueryReference
 implements OrthoProcessingTaskQueryReferenceIF

  {

  public OrthoProcessingTaskQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.bus.OrthoProcessingTask orthoProcessingTask)
    {
      if(orthoProcessingTask == null) return this.EQ((java.lang.String)null);
      return this.EQ(orthoProcessingTask.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.bus.OrthoProcessingTask orthoProcessingTask)
    {
      if(orthoProcessingTask == null) return this.NE((java.lang.String)null);
      return this.NE(orthoProcessingTask.getOid());
    }

  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface OrthoProcessingTaskQueryMultiReferenceIF extends gov.geoplatform.uasdm.bus.WorkflowTaskQuery.WorkflowTaskQueryMultiReferenceIF
  {


    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask);
    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask);
    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask);
    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask);
    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class OrthoProcessingTaskQueryMultiReference extends gov.geoplatform.uasdm.bus.WorkflowTaskQuery.WorkflowTaskQueryMultiReference
 implements OrthoProcessingTaskQueryMultiReferenceIF

  {

  public OrthoProcessingTaskQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask)  {

      String[] itemIdArray = new String[orthoProcessingTask.length]; 

      for (int i=0; i<orthoProcessingTask.length; i++)
      {
        itemIdArray[i] = orthoProcessingTask[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask)  {

      String[] itemIdArray = new String[orthoProcessingTask.length]; 

      for (int i=0; i<orthoProcessingTask.length; i++)
      {
        itemIdArray[i] = orthoProcessingTask[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask)  {

      String[] itemIdArray = new String[orthoProcessingTask.length]; 

      for (int i=0; i<orthoProcessingTask.length; i++)
      {
        itemIdArray[i] = orthoProcessingTask[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask)  {

      String[] itemIdArray = new String[orthoProcessingTask.length]; 

      for (int i=0; i<orthoProcessingTask.length; i++)
      {
        itemIdArray[i] = orthoProcessingTask[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.bus.OrthoProcessingTask ... orthoProcessingTask)  {

      String[] itemIdArray = new String[orthoProcessingTask.length]; 

      for (int i=0; i<orthoProcessingTask.length; i++)
      {
        itemIdArray[i] = orthoProcessingTask[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  }
}
