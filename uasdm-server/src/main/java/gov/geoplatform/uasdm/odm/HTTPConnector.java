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
/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package gov.geoplatform.uasdm.odm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.client.cache.ExponentialBackOffSchedulingStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.InvalidLoginException;

public class HTTPConnector
{
  CloseableHttpClient client;
  
  Logger logger = LoggerFactory.getLogger(HTTPConnector.class);
  
  String serverurl;
  
  String username;
  
  String password;
  
  protected HttpClientContext localContext;
  
  public void setCredentials(String username, String password)
  {
    this.username = username;
    this.password = password;
  }
  
  public String getServerUrl()
  {
    return serverurl;
  }
  
  public void setServerUrl(String url)
  {
    if (!url.endsWith("/"))
    {
      url = url + "/";
    }
    
    this.serverurl = url;
  }
  
  synchronized public void initialize()
  {
    CachingHttpClientBuilder builder = CachingHttpClients.custom();
    
    HttpRequestRetryHandler backoffRetryHandler = new DefaultHttpRequestRetryHandler(7, true) {
      @Override
      public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
        try
        {
          Thread.sleep((long) ( 1000 * Math.pow(2, executionCount) ));
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e);
        }
        
        return super.retryRequest(exception, executionCount, context);
      }
    };
    
    builder.setRetryHandler(backoffRetryHandler);
    
    // By default, only GET requests resulting in a redirect are automatically followed. We want to follow it on POST as well.
    builder.setRedirectStrategy(new LaxRedirectStrategy());
    
    if (username != null && password != null)
    {
      HttpHost target = HttpHost.create(serverurl);
      
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
      credentialsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), defaultcreds);
      builder.setDefaultCredentialsProvider(credentialsProvider);

      
      // Adding an authCache to a localContext will make the authentication preemptive.
      AuthCache authCache = new BasicAuthCache();
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(target, basicAuth);

      localContext = HttpClientContext.create();
      localContext.setAuthCache(authCache);
    }
    
    this.client = builder.build();
  }
  
  public boolean isInitialized()
  {
    return client != null;
  }
  
  public Response httpGet(String url, List<NameValuePair> params)
  {
    if (!isInitialized())
    {
      initialize();
    }
    
    URI uri;
    try
    {
      uri = new URIBuilder(this.getServerUrl() + url)
          .addParameters(params)
          .build();
    }
    catch (URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
    
    HttpGet get = new HttpGet(uri);
    
    get.addHeader(new BasicHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()));
    
    Response response = this.httpRequest(get);
    
    if (response.getStatusCode() == 401)
    {
      throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
    }
    
    return response;
  }
  
  public Response postAsMultipart(String url, HttpEntity entity)
  {
//    try {
      if (!isInitialized())
      {
        initialize();
      }
      
      HttpPost post = new HttpPost(this.getServerUrl() + url);
      
      post.setEntity(entity);
      
      Response response = this.httpRequest(post);
      
      if (response.getStatusCode() == 401)
      {
        throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
      }
      
      return response;
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException(e);
//    }
  }
  
  public Response httpPost(String url, List<NameValuePair> body)
  {
    if (!isInitialized())
    {
      initialize();
    }
    
    HttpPost post = new HttpPost(this.getServerUrl() + url);
    
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(body, Consts.UTF_8);
    post.setEntity(entity);
    
    Response response = this.httpRequest(post);
    
    if (response.getStatusCode() == 401)
    {
      throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
    }
    
    return response;
  }
  
  public Response httpRequest(HttpUriRequest request)
  {
    this.logger.debug("Sending request to " + request.getURI());
    
    String sResponse = null;
    
    try (CloseableHttpResponse response = client.execute(request, localContext))
    {
      int statusCode = response.getStatusLine().getStatusCode();
      
      HttpEntity responseEntity = response.getEntity();
      
      if (responseEntity != null)
      {
        sResponse = EntityUtils.toString(responseEntity);
        
        if (sResponse.length() < 1000)
        {
          this.logger.debug("Response string = '" + sResponse + "'.");
        }
        else
        {
          this.logger.debug("Receieved a very large response.");
        }
      }
      
      return new gov.geoplatform.uasdm.odm.HttpResponse(sResponse, statusCode);
    }
    catch (IOException e)
    {
      throw new UnreachableHostException(e);
    }
  }
}
