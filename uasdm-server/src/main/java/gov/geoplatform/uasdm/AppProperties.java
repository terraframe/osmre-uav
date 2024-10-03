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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;

import net.geoprism.configuration.GeoprismProperties;

public class AppProperties
{
  /**
   * The app.properties configuration file
   */
  private ConfigurationReaderIF props;

  private AppProperties()
  {
    this.props = ConfigurationManager.getReader(UasdmConfigGroup.COMMON, "app.properties");
  }

  private static class Singleton
  {
    private static AppProperties INSTANCE = new AppProperties();

    private static AppProperties getInstance()
    {
      // INSTANCE will only ever be null if there is a problem. The if check is
      // to allow for debugging.
      if (INSTANCE == null)
      {
        INSTANCE = new AppProperties();
      }

      return INSTANCE;
    }

    private static ConfigurationReaderIF getProps()
    {
      return getInstance().props;
    }
  }
  
  public static final String PROD_VALIDATOR_CMD = "[\"python3\", \"/usr/local/tomcat/validate_cloud_optimized_geotiff.py\", \"{cog_file}\"]";
  
  public static String getCogValidatorCommand()
  {
    return Singleton.getProps().getString("cog.validator.cmd", PROD_VALIDATOR_CMD);
  }

  public static String getTitilerUrl()
  {
    return Singleton.getProps().getString("titiler.url");
  }
  
  public static Boolean getExposePublicTileEndpoints()
  {
    return Singleton.getProps().getBoolean("titiler.public.expose", true);
  }
  
  public static String getBucketName()
  {
    return Singleton.getProps().getString("bucket.name");
  }
  public static String getPublicBucketName()
  {
    return Singleton.getProps().getString("bucket.public.name");
  }

  public static String getBucketRegion()
  {
    return Singleton.getProps().getString("bucket.region");
  }

  public static String getOdmUrl()
  {
    return Singleton.getProps().getString("odm.url", "http://localhost:3000/");
  }

  public static String getOdmUsername()
  {
    return Singleton.getProps().getString("odm.username");
  }

  public static String getOdmPassword()
  {
    return Singleton.getProps().getString("odm.password");
  }

  public static String getSolrUrl()
  {
    return Singleton.getProps().getString("solr.url", "http://localhost:8983/solr/uasdm");
  }

  public static String getElasticsearchHost()
  {
    return Singleton.getProps().getString("elasticsearch.host", "localhost");
  }

  public static String getElasticsearchSchema()
  {
    return Singleton.getProps().getString("elasticsearch.schema", "http");
  }

  public static int getElasticsearchPort()
  {
    return Singleton.getProps().getInteger("elasticsearch.port", 9200);
  }

  public static String getElasticsearchUsername()
  {
    return Singleton.getProps().getString("elasticsearch.username", "elastic");
  }

  public static String getElasticsearchPassword()
  {
    return Singleton.getProps().getString("elasticsearch.password", "elastic");
  }

  public static Boolean isSearchEnabled()
  {
    return Singleton.getProps().getBoolean("search.enabled", true);
  }

  public static Boolean isKeycloakEnabled()
  {
    return Singleton.getProps().getBoolean("keycloak.enabled", false);
  }
  
  public static Boolean requireKeycloakLogin()
  {
    return Singleton.getProps().getBoolean("keycloak.requireKeycloakLogin", false);
  }
  
  public static Boolean IsKeycloakNg2Dev()
  {
    return Singleton.getProps().getBoolean("keycloak.ng2dev", false);
  }

  public static InputStream getKeycloakConfig()
  {
    if (isKeycloakEnabled() && ConfigurationManager.checkExistence(UasdmConfigGroup.COMMON, "keycloak.json"))
    {
      return ConfigurationManager.getResourceAsStream(UasdmConfigGroup.COMMON, "keycloak.json");
    }
    else
    {
      return null;
    }
  }

  public static File getTempDirectory()
  {
    File temp = new File(GeoprismProperties.getGeoprismFileStorage(), Singleton.getProps().getString("temp.dir", "temp"));
    
    if (!temp.exists())
    {
      temp.mkdir();
    }
    
    return temp;
  }

  public static File getUploadDirectory()
  {
    File upload = new File(GeoprismProperties.getGeoprismFileStorage(), Singleton.getProps().getString("upload.dir", "upload"));
    
    if (!upload.exists())
    {
      upload.mkdir();
    }
    
    return upload;
  }

  public static Integer getChunkExpireTime()
  {
    return Singleton.getProps().getInteger("chunk.expire.time", 1);
  }

  public static Integer getInviteUserTokenExpireTime()
  {
    return Singleton.getProps().getInteger("invite.user.token.expire.time", 72);
  }

  public static String getDeploymentType()
  {
    return Singleton.getProps().getString("deployment.type", "osmre");
  }

  public static String getS3AccessKey()
  {
    return Singleton.getProps().getString("s3.accessKey");
  }

  public static String getS3SecretKey()
  {
    return Singleton.getProps().getString("s3.secretKey");
  }

  /*
   * Eros Synchronization related properties
   */

  public static String getErosECSAccessKey()
  {
    return Singleton.getProps().getString("eros.ecs.accessKey");
  }

  public static String getErosECSSecretKey()
  {
    return Singleton.getProps().getString("eros.ecs.secretKey");
  }

  public static String getErosFtpServerUrl()
  {
    return Singleton.getProps().getString("eros.ftp.serverUrl");
  }

  public static String getErosFtpUsername()
  {
    return Singleton.getProps().getString("eros.ftp.username");
  }

  public static String getErosFtpPassword()
  {
    return Singleton.getProps().getString("eros.ftp.password");
  }

  public static String getErosFtpPassive()
  {
    return Singleton.getProps().getString("eros.ftp.passive");
  }

  public static String getErosFtpPort()
  {
    return Singleton.getProps().getString("eros.ftp.port");
  }

  public static String getErosFtpTargetPath()
  {
    return Singleton.getProps().getString("eros.ftp.targetPath");
  }

  public static String getErosCluster()
  {
    return Singleton.getProps().getString("eros.ecs.cluster");
  }

  public static String getErosTask()
  {
    return Singleton.getProps().getString("eros.ecs.task");
  }

  public static String getErosContainerName()
  {
    return Singleton.getProps().getString("eros.ecs.containerName");
  }

  public static String getErosSubnets()
  {
    return Singleton.getProps().getString("eros.ecs.subnets");
  }

  public static String getAppDisclaimer()
  {
    return Singleton.getProps().getString("app.disclaimer");
  }

  public static String getPotreeConverterPath()
  {
    return Singleton.getProps().getString("potree.converter.bin", "/opt/PotreeConverter/build/PotreeConverter");
  }
  
  public static List<String> getSilvimetricCommand()
  {
    return new ArrayList<String>(Arrays.asList(Singleton.getProps().getString("silvimetric.cmd", "silvimetric.cmd=/opt/silvimetric/silvimetric_idm.sh /opt/conda/etc/profile.d/conda.sh").split(" ")));
  }
  
  public static List<String> getPdalPath()
  {
    return new ArrayList<String>(Arrays.asList(Singleton.getProps().getString("pdal.bin", "/opt/conda/envs/silvimetric/bin/pdal").split(" ")));
  }
  
  public static String getProjDataPath()
  {
    return Singleton.getProps().getString("proj.data", "/opt/conda/envs/silvimetric/share/proj");
  }
}
