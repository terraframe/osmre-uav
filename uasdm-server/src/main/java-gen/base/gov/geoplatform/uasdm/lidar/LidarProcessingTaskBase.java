package gov.geoplatform.uasdm.lidar;

@com.runwaysdk.business.ClassSignature(hash = -891552514)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to LidarProcessingTask.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class LidarProcessingTaskBase extends gov.geoplatform.uasdm.bus.WorkflowTask
{
  public final static String CLASS = "gov.geoplatform.uasdm.lidar.LidarProcessingTask";
  public final static java.lang.String CONFIGURATIONJSON = "configurationJson";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -891552514;
  
  public LidarProcessingTaskBase()
  {
    super();
  }
  
  public String getConfigurationJson()
  {
    return getValue(CONFIGURATIONJSON);
  }
  
  public void validateConfigurationJson()
  {
    this.validateAttribute(CONFIGURATIONJSON);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getConfigurationJsonMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(gov.geoplatform.uasdm.lidar.LidarProcessingTask.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CONFIGURATIONJSON);
  }
  
  public void setConfigurationJson(String value)
  {
    if(value == null)
    {
      setValue(CONFIGURATIONJSON, "");
    }
    else
    {
      setValue(CONFIGURATIONJSON, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static LidarProcessingTaskQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    LidarProcessingTaskQuery query = new LidarProcessingTaskQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static LidarProcessingTask get(String oid)
  {
    return (LidarProcessingTask) com.runwaysdk.business.Business.get(oid);
  }
  
  public static LidarProcessingTask getByKey(String key)
  {
    return (LidarProcessingTask) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static LidarProcessingTask lock(java.lang.String oid)
  {
    LidarProcessingTask _instance = LidarProcessingTask.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static LidarProcessingTask unlock(java.lang.String oid)
  {
    LidarProcessingTask _instance = LidarProcessingTask.get(oid);
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