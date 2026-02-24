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
package gov.geoplatform.uasdm;

import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.odm.HttpResponse;
import gov.geoplatform.uasdm.odm.HttpInfoResponse;
import gov.geoplatform.uasdm.odm.ODMStatus;

/**
 * Useful for disabling features to make testing faster / easier.
 * 
 * @author richard1
 */
public class DevProperties
{
  private ConfigurationReaderIF props;

  private static DevProperties  instance = null;

  public DevProperties()
  {
    // TODO : For now we're just using app.properties, since none of these
    // properties should even exist outside of envcfg.properties anyway.
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

  public static Boolean isLocalODM()
  {
    return getInstance().props.getBoolean("dev.isLocalODM", false);
  }

  public static Boolean isLocalKnowStac()
  {
    return getInstance().props.getBoolean("dev.isLocalKnowStac", false);
  }

  public static CloseableFile orthoResults()
  {
    return new CloseableFile(getInstance().props.getString("dev.orthoResults"), false);
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
  public static HttpInfoResponse getMockOdmTaskInfo()
  {
    JSONObject mock = new JSONObject();

    JSONObject status = new JSONObject();
    status.put("code", ODMStatus.COMPLETED.getCode());
    mock.put("status", status);

    mock.put("processingTime", 100L);

    mock.put("imagesCount", 99);

    return new HttpInfoResponse(new HttpResponse(mock.toString(), 200));
  }
}
