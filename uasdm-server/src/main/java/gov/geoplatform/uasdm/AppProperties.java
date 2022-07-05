/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm;

import java.io.File;
import java.io.InputStream;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;

import net.geoprism.GeoprismProperties;

public class AppProperties
{
  /**
   * The server.properties configuration file
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

  public static String getBucketName()
  {
    return Singleton.getProps().getString("bucket.name");
  }

  public static String getPublicWorkspace()
  {
    return Singleton.getProps().getString("public.workspace", "image-public");
  }

  public static String getPublicHillshadeWorkspace()
  {
    return Singleton.getProps().getString("geoserver.workspace.public.hillshade", "public-hillshade");
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
    return Singleton.getProps().getString("elasticsearch.host", "https");
  }

  public static int getElasticsearchPort()
  {
    return Singleton.getProps().getInteger("elasticsearch.host", 9200);
  }

  public static String getElasticsearchUsername()
  {
    return Singleton.getProps().getString("elasticsearch.username", "username");
  }

  public static String getElasticsearchPassword()
  {
    return Singleton.getProps().getString("elasticsearch.password", "password");
  }

  public static Boolean isSolrEnabled()
  {
    return Singleton.getProps().getBoolean("solr.enabled", true);
  }

  public static Boolean isKeycloakEnabled()
  {
    return Singleton.getProps().getBoolean("keycloak.enabled", false);
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
    return new File(GeoprismProperties.getGeoprismFileStorage(), Singleton.getProps().getString("temp.dir", "temp"));
  }

  public static File getUploadDirectory()
  {
    return new File(GeoprismProperties.getGeoprismFileStorage(), Singleton.getProps().getString("upload.dir", "upload"));
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
}
