package gov.geoplatform.uasdm;

@com.runwaysdk.business.ClassSignature(hash = 82482719)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImageryProcessingJob.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class ImageryProcessingJobQuery extends com.runwaysdk.system.scheduler.ExecutableJobQuery

{

  public ImageryProcessingJobQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public ImageryProcessingJobQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return gov.geoplatform.uasdm.ImageryProcessingJob.CLASS;
  }
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile()
  {
    return getImageryFile(null);

  }
 
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile(String alias)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE);

    return (com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE, mdAttributeIF, this, alias, null);

  }
 
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile(String alias, String displayLabel)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE);

    return (com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE, mdAttributeIF, this, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix()
  {
    return getOutFileNamePrefix(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.ImageryProcessingJob.OUTFILENAMEPREFIX, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.ImageryProcessingJob.OUTFILENAMEPREFIX, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getUploadTarget()
  {
    return getUploadTarget(null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.ImageryProcessingJob.UPLOADTARGET, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(gov.geoplatform.uasdm.ImageryProcessingJob.UPLOADTARGET, alias, displayLabel);

  }
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask()
  {
    return getWorkflowTask(null);

  }
 
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK);

    return (gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK, mdAttributeIF, this, alias, null);

  }
 
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias, String displayLabel)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK);

    return (gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK, mdAttributeIF, this, alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE)) 
    {
       return new com.runwaysdk.system.VaultFileQuery.VaultFileQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else if (name.equals(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK)) 
    {
       return new gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
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
  public com.runwaysdk.query.OIterator<? extends ImageryProcessingJob> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<ImageryProcessingJob>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ImageryProcessingJobQueryReferenceIF extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getOutFileNamePrefix();
    public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias);
    public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getUploadTarget();
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias);
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel);
    public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask();
    public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias);
    public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.ImageryProcessingJob imageryProcessingJob);

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.ImageryProcessingJob imageryProcessingJob);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ImageryProcessingJobQueryReference extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryReference
 implements ImageryProcessingJobQueryReferenceIF

  {

  public ImageryProcessingJobQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(gov.geoplatform.uasdm.ImageryProcessingJob imageryProcessingJob)
    {
      if(imageryProcessingJob == null) return this.EQ((java.lang.String)null);
      return this.EQ(imageryProcessingJob.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(gov.geoplatform.uasdm.ImageryProcessingJob imageryProcessingJob)
    {
      if(imageryProcessingJob == null) return this.NE((java.lang.String)null);
      return this.NE(imageryProcessingJob.getOid());
    }

  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile()
  {
    return getImageryFile(null);

  }
 
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile(String alias)
  {
    return (com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE, alias, null);

  }
 
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile(String alias, String displayLabel)
  {
    return (com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE,  alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix()
  {
    return getOutFileNamePrefix(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.OUTFILENAMEPREFIX, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.OUTFILENAMEPREFIX, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getUploadTarget()
  {
    return getUploadTarget(null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.UPLOADTARGET, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.UPLOADTARGET, alias, displayLabel);

  }
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask()
  {
    return getWorkflowTask(null);

  }
 
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias)
  {
    return (gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK, alias, null);

  }
 
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias, String displayLabel)
  {
    return (gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE)) 
    {
       return new com.runwaysdk.system.VaultFileQuery.VaultFileQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else if (name.equals(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK)) 
    {
       return new gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
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
  public interface ImageryProcessingJobQueryMultiReferenceIF extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getOutFileNamePrefix();
    public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias);
    public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getUploadTarget();
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias);
    public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel);
    public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask();
    public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias);
    public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob);
    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob);
    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob);
    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob);
    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ImageryProcessingJobQueryMultiReference extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryMultiReference
 implements ImageryProcessingJobQueryMultiReferenceIF

  {

  public ImageryProcessingJobQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob)  {

      String[] itemIdArray = new String[imageryProcessingJob.length]; 

      for (int i=0; i<imageryProcessingJob.length; i++)
      {
        itemIdArray[i] = imageryProcessingJob[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob)  {

      String[] itemIdArray = new String[imageryProcessingJob.length]; 

      for (int i=0; i<imageryProcessingJob.length; i++)
      {
        itemIdArray[i] = imageryProcessingJob[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob)  {

      String[] itemIdArray = new String[imageryProcessingJob.length]; 

      for (int i=0; i<imageryProcessingJob.length; i++)
      {
        itemIdArray[i] = imageryProcessingJob[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob)  {

      String[] itemIdArray = new String[imageryProcessingJob.length]; 

      for (int i=0; i<imageryProcessingJob.length; i++)
      {
        itemIdArray[i] = imageryProcessingJob[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(gov.geoplatform.uasdm.ImageryProcessingJob ... imageryProcessingJob)  {

      String[] itemIdArray = new String[imageryProcessingJob.length]; 

      for (int i=0; i<imageryProcessingJob.length; i++)
      {
        itemIdArray[i] = imageryProcessingJob[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile()
  {
    return getImageryFile(null);

  }
 
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile(String alias)
  {
    return (com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE, alias, null);

  }
 
  public com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF getImageryFile(String alias, String displayLabel)
  {
    return (com.runwaysdk.system.VaultFileQuery.VaultFileQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE,  alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix()
  {
    return getOutFileNamePrefix(null);

  }
 
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.OUTFILENAMEPREFIX, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getOutFileNamePrefix(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.OUTFILENAMEPREFIX, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getUploadTarget()
  {
    return getUploadTarget(null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.UPLOADTARGET, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getUploadTarget(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.UPLOADTARGET, alias, displayLabel);

  }
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask()
  {
    return getWorkflowTask(null);

  }
 
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias)
  {
    return (gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK, alias, null);

  }
 
  public gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF getWorkflowTask(String alias, String displayLabel)
  {
    return (gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReferenceIF)this.get(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(gov.geoplatform.uasdm.ImageryProcessingJob.IMAGERYFILE)) 
    {
       return new com.runwaysdk.system.VaultFileQuery.VaultFileQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else if (name.equals(gov.geoplatform.uasdm.ImageryProcessingJob.WORKFLOWTASK)) 
    {
       return new gov.geoplatform.uasdm.bus.AbstractWorkflowTaskQuery.AbstractWorkflowTaskQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  }
}