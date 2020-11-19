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
package gov.osmre.uasdm.erossync;

import java.io.IOException;

public class App
{
  public static void main(String[] args) throws IOException
  {
    validate();
    new S3ToFTPCopier(System.getenv("EROSSYNC_FTP_SERVER"), System.getenv("EROSSYNC_S3_BUCKET")).copyDirectory(System.getenv("EROSSYNC_S3_SOURCE_PATH"), System.getenv("EROSSYNC_FTP_TARGET_PATH"), true);
  }
  
  public static void validate()
  {
    assertEnvNotNull("EROSSYNC_FTP_SERVER");
    assertEnvNotNull("EROSSYNC_S3_BUCKET");
    assertEnvNotNull("EROSSYNC_S3_SOURCE_PATH");
    assertEnvNotNull("EROSSYNC_FTP_TARGET_PATH");
    assertEnvNotNull("EROSSYNC_FTP_USERNAME");
    assertEnvNotNull("EROSSYNC_FTP_PASSWORD");
  }
  
  public static void assertEnvNotNull(String envParam)
  {
    if (System.getenv(envParam) == null || System.getenv(envParam).length() == 0)
    {
      throw new RuntimeException("Expected environment variable [" + envParam + "] to be set. Instead, it was either null or empty.");
    }
  }
}
