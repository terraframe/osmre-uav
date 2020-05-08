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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLLocalhostTrustConfiguration
{
  public static void trustLocalhost()
  {
//    try
//    {
      // Create a trust manager that does not validate certificate chains
//      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
//      {
//        public java.security.cert.X509Certificate[] getAcceptedIssuers()
//        {
//          return null;
//        }
//
//        public void checkClientTrusted(X509Certificate[] certs, String authType)
//        {
//        }
//
//        public void checkServerTrusted(X509Certificate[] certs, String authType)
//        {
//        }
//      } };

      // Install the all-trusting trust manager
//      SSLContext sc = SSLContext.getInstance("SSL");
//      sc.init(null, trustAllCerts, new java.security.SecureRandom());
//      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    
      HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();

      // Create a host name verifier which trusts localhost
      HostnameVerifier allHostsValid = new HostnameVerifier()
      {
        public boolean verify(String hostname, SSLSession session)
        {
          if (hostname.equals("127.0.0.1") || hostname.equals("localhost"))
          {
            return true;
          }
          else
          {
            return hv.verify(hostname, session);
          }
        }
      };

      // Install the localhost-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//    }
//    catch (NoSuchAlgorithmException e)
//    {
//      e.printStackTrace();
//    }
//    catch (KeyManagementException e)
//    {
//      e.printStackTrace();
//    }
  }
}
