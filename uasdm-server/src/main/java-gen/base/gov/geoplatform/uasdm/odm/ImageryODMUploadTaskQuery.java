package gov.geoplatform.uasdm.odm;

@com.runwaysdk.business.ClassSignature(hash = 96811912)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImageryODMUploadTask.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class ImageryODMUploadTaskQuery extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery

{

  public ImageryODMUploadTaskQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public ImageryODMUploadTaskQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return gov.geoplatform.uasdm.odm.ImageryODMUploadTask.CLASS;
  }
  public com.runwaysdk.query.SelectableChar getOdmUUID()
  {
    return getOdmUUID(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.ODMUUID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.ODMUUID, alias, displayLabel);

  }
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask()
  {
    return getProcessingTask(null);

  }
 
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK);

    return (gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK, mdAttributeIF, this, alias, null);

  }
 
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias, String displayLabel)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK);

    return (gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK, mdAttributeIF, this, alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK)) 
    {
       return new gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends ImageryODMUploadTask> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<ImageryODMUploadTask>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ImageryODMUploadTaskQueryReferenceIF extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getOdmUUID();
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias);
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel);
    public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask();
    public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias);
    public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.odm.ImageryODMUploadTask imageryODMUploadTask);

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.odm.ImageryODMUploadTask imageryODMUploadTask);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ImageryODMUploadTaskQueryReference extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryReference
 implements ImageryODMUploadTaskQueryReferenceIF

  {

  public ImageryODMUploadTaskQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.odm.ImageryODMUploadTask imageryODMUploadTask)
    {
      if(imageryODMUploadTask == null) return this.EQ((java.lang.String)null);
      return this.EQ(imageryODMUploadTask.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.odm.ImageryODMUploadTask imageryODMUploadTask)
    {
      if(imageryODMUploadTask == null) return this.NE((java.lang.String)null);
      return this.NE(imageryODMUploadTask.getOid());
    }

  public com.runwaysdk.query.SelectableChar getOdmUUID()
  {
    return getOdmUUID(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.ODMUUID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.ODMUUID, alias, displayLabel);

  }
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask()
  {
    return getProcessingTask(null);

  }
 
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias)
  {
    return (gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK, alias, null);

  }
 
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias, String displayLabel)
  {
    return (gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK)) 
    {
       return new gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ImageryODMUploadTaskQueryMultiReferenceIF extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getOdmUUID();
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias);
    public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel);
    public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask();
    public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias);
    public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask);
    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask);
    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask);
    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask);
    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ImageryODMUploadTaskQueryMultiReference extends gov.geoplatform.uasdm.bus.ImageryWorkflowTaskQuery.ImageryWorkflowTaskQueryMultiReference
 implements ImageryODMUploadTaskQueryMultiReferenceIF

  {

  public ImageryODMUploadTaskQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask)  {

      String[] itemIdArray = new String[imageryODMUploadTask.length]; 

      for (int i=0; i<imageryODMUploadTask.length; i++)
      {
        itemIdArray[i] = imageryODMUploadTask[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask)  {

      String[] itemIdArray = new String[imageryODMUploadTask.length]; 

      for (int i=0; i<imageryODMUploadTask.length; i++)
      {
        itemIdArray[i] = imageryODMUploadTask[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask)  {

      String[] itemIdArray = new String[imageryODMUploadTask.length]; 

      for (int i=0; i<imageryODMUploadTask.length; i++)
      {
        itemIdArray[i] = imageryODMUploadTask[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask)  {

      String[] itemIdArray = new String[imageryODMUploadTask.length]; 

      for (int i=0; i<imageryODMUploadTask.length; i++)
      {
        itemIdArray[i] = imageryODMUploadTask[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.odm.ImageryODMUploadTask ... imageryODMUploadTask)  {

      String[] itemIdArray = new String[imageryODMUploadTask.length]; 

      for (int i=0; i<imageryODMUploadTask.length; i++)
      {
        itemIdArray[i] = imageryODMUploadTask[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.query.SelectableChar getOdmUUID()
  {
    return getOdmUUID(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.ODMUUID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOdmUUID(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.ODMUUID, alias, displayLabel);

  }
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask()
  {
    return getProcessingTask(null);

  }
 
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias)
  {
    return (gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK, alias, null);

  }
 
  public gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF getProcessingTask(String alias, String displayLabel)
  {
    return (gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(gov.geoplatform.uasdm.odm.ImageryODMUploadTask.PROCESSINGTASK)) 
    {
       return new gov.geoplatform.uasdm.odm.ImageryODMProcessingTaskQuery.ImageryODMProcessingTaskQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  }
}