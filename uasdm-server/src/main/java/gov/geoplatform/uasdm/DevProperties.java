package gov.geoplatform.uasdm;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;

import gov.geoplatform.uasdm.odm.HTTPResponse;
import gov.geoplatform.uasdm.odm.InfoResponse;
import gov.geoplatform.uasdm.odm.ODMStatus;

/**
 * Useful for disabling features to make testing faster / easier.
 * 
 * @author richard1
 */
public class DevProperties
{
  private ConfigurationReaderIF props;
  
  private static DevProperties instance = null;
  
  public DevProperties()
  {
    // TODO : For now we're just using app.properties, since none of these properties should even exist outside of envcfg.properties anyway.
    this.props = ConfigurationManager.getReader(UasdmConfigGroup.COMMON, "app.properties");
  }
  
  public static synchronized DevProperties getInstance()
  {
    if (instance == null)
    {
      instance = new DevProperties();
    }
    
    return instance;
  }
  
  public static Boolean uploadAllZip()
  {
    return getInstance().props.getBoolean("dev.s3.uploadAllZip", true);
  }
  
  public static Boolean uploadRaw()
  {
    return getInstance().props.getBoolean("dev.s3.uploadRaw", true);
  }
  
  public static Boolean runOrtho()
  {
    return getInstance().props.getBoolean("dev.runOrtho", true);
  }
  
  public static File orthoResults()
  {
    return new File(getInstance().props.getString("dev.orthoResults"));
  }
  
  public static boolean shouldUploadProduct(String name)
  {
    String shouldUpload = getInstance().props.getString("dev.s3.shouldUploadProduct", "");
    
    if (shouldUpload.equals(""))
    {
      return true;
    }
    else
    {
      String[] allowed = shouldUpload.split(",");
      
      return ArrayUtils.contains(allowed, name);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static InfoResponse getMockOdmTaskInfo()
  {
    JSONObject mock = new JSONObject();
    
    JSONObject status = new JSONObject();
    status.put("code", ODMStatus.COMPLETED.getCode());
    mock.put("status", status);
    
    mock.put("processingTime", 100L);
    
    mock.put("imagesCount", 99);
    
    return new InfoResponse(new HTTPResponse(mock.toString(), 200));
  }
}
