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

@com.runwaysdk.business.ClassSignature(hash = -1002943334)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to WorkflowTask.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class WorkflowTaskQuery extends gov.geoplatform.uasdm.bus.AbstractUploadTaskQuery

{

  public WorkflowTaskQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public WorkflowTaskQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return gov.geoplatform.uasdm.bus.WorkflowTask.CLASS;
  }
  public com.runwaysdk.query.SelectableUUID getComponent()
  {
    return getComponent(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getComponent(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.COMPONENT, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getComponent(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.COMPONENT, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessDem()
  {
    return getProcessDem(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSDEM, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSDEM, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho()
  {
    return getProcessOrtho(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSORTHO, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSORTHO, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud()
  {
    return getProcessPtcloud(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSPTCLOUD, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSPTCLOUD, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getUploadTarget()
  {
    return getUploadTarget(null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.UPLOADTARGET, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.UPLOADTARGET, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getWorkflowType()
  {
    return getWorkflowType(null);

  }
 
  public com.runwaysdk.query.SelectableChar getWorkflowType(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.WORKFLOWTYPE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getWorkflowType(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.bus.WorkflowTask.WORKFLOWTYPE, alias, displayLabel);

  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends WorkflowTask> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<WorkflowTask>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface WorkflowTaskQueryReferenceIF extends gov.geoplatform.uasdm.bus.AbstractUploadTaskQuery.AbstractUploadTaskQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableUUID getComponent();
    public com.runwaysdk.query.SelectableUUID getComponent(String alias);
    public com.runwaysdk.query.SelectableUUID getComponent(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableBoolean getProcessDem();
    public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias);
    public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableBoolean getProcessOrtho();
    public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias);
    public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableBoolean getProcessPtcloud();
    public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias);
    public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getUploadTarget();
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias);
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getWorkflowType();
    public com.runwaysdk.query.SelectableChar getWorkflowType(String alias);
    public com.runwaysdk.query.SelectableChar getWorkflowType(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.bus.WorkflowTask workflowTask);

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.bus.WorkflowTask workflowTask);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class WorkflowTaskQueryReference extends gov.geoplatform.uasdm.bus.AbstractUploadTaskQuery.AbstractUploadTaskQueryReference
 implements WorkflowTaskQueryReferenceIF

  {

  public WorkflowTaskQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.bus.WorkflowTask workflowTask)
    {
      if(workflowTask == null) return this.EQ((java.lang.String)null);
      return this.EQ(workflowTask.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.bus.WorkflowTask workflowTask)
    {
      if(workflowTask == null) return this.NE((java.lang.String)null);
      return this.NE(workflowTask.getOid());
    }

  public com.runwaysdk.query.SelectableUUID getComponent()
  {
    return getComponent(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getComponent(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.COMPONENT, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getComponent(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.COMPONENT, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessDem()
  {
    return getProcessDem(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSDEM, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSDEM, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho()
  {
    return getProcessOrtho(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSORTHO, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSORTHO, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud()
  {
    return getProcessPtcloud(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSPTCLOUD, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSPTCLOUD, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getUploadTarget()
  {
    return getUploadTarget(null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.UPLOADTARGET, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.UPLOADTARGET, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getWorkflowType()
  {
    return getWorkflowType(null);

  }
 
  public com.runwaysdk.query.SelectableChar getWorkflowType(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.WORKFLOWTYPE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getWorkflowType(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.WORKFLOWTYPE, alias, displayLabel);

  }
  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface WorkflowTaskQueryMultiReferenceIF extends gov.geoplatform.uasdm.bus.AbstractUploadTaskQuery.AbstractUploadTaskQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableUUID getComponent();
    public com.runwaysdk.query.SelectableUUID getComponent(String alias);
    public com.runwaysdk.query.SelectableUUID getComponent(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableBoolean getProcessDem();
    public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias);
    public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableBoolean getProcessOrtho();
    public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias);
    public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableBoolean getProcessPtcloud();
    public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias);
    public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getUploadTarget();
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias);
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getWorkflowType();
    public com.runwaysdk.query.SelectableChar getWorkflowType(String alias);
    public com.runwaysdk.query.SelectableChar getWorkflowType(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask);
    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask);
    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask);
    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask);
    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class WorkflowTaskQueryMultiReference extends gov.geoplatform.uasdm.bus.AbstractUploadTaskQuery.AbstractUploadTaskQueryMultiReference
 implements WorkflowTaskQueryMultiReferenceIF

  {

  public WorkflowTaskQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask)  {

      String[] itemIdArray = new String[workflowTask.length]; 

      for (int i=0; i<workflowTask.length; i++)
      {
        itemIdArray[i] = workflowTask[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask)  {

      String[] itemIdArray = new String[workflowTask.length]; 

      for (int i=0; i<workflowTask.length; i++)
      {
        itemIdArray[i] = workflowTask[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask)  {

      String[] itemIdArray = new String[workflowTask.length]; 

      for (int i=0; i<workflowTask.length; i++)
      {
        itemIdArray[i] = workflowTask[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask)  {

      String[] itemIdArray = new String[workflowTask.length]; 

      for (int i=0; i<workflowTask.length; i++)
      {
        itemIdArray[i] = workflowTask[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.bus.WorkflowTask ... workflowTask)  {

      String[] itemIdArray = new String[workflowTask.length]; 

      for (int i=0; i<workflowTask.length; i++)
      {
        itemIdArray[i] = workflowTask[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.query.SelectableUUID getComponent()
  {
    return getComponent(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getComponent(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.COMPONENT, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getComponent(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.COMPONENT, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessDem()
  {
    return getProcessDem(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSDEM, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessDem(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSDEM, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho()
  {
    return getProcessOrtho(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSORTHO, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessOrtho(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSORTHO, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud()
  {
    return getProcessPtcloud(null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSPTCLOUD, alias, null);

  }
 
  public com.runwaysdk.query.SelectableBoolean getProcessPtcloud(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableBoolean)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.PROCESSPTCLOUD, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getUploadTarget()
  {
    return getUploadTarget(null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.UPLOADTARGET, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.UPLOADTARGET, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getWorkflowType()
  {
    return getWorkflowType(null);

  }
 
  public com.runwaysdk.query.SelectableChar getWorkflowType(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.WORKFLOWTYPE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getWorkflowType(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.bus.WorkflowTask.WORKFLOWTYPE, alias, displayLabel);

  }
  }
}
