/*******************************************************************************
 * Copyright (C) 2018 IVCC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.session.InvalidLoginException;

public class HTTPConnector
{
  HttpClient client;
  
  Logger logger = LoggerFactory.getLogger(HTTPConnector.class);
  
  String serverurl;
  
  String username;
  
  String password;
  
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
    this.client = new HttpClient();
    
    if (username != null && password != null)
    {
      client.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
      client.getState().setCredentials(AuthScope.ANY, defaultcreds);
    }
  }
  
  public boolean isInitialized()
  {
    return client != null;
  }
  
  public HTTPResponse httpGet(String url, NameValuePair[] params)
  {
    if (!isInitialized())
    {
      initialize();
    }
    
    GetMethod get = new GetMethod(this.getServerUrl() + url);
    
    get.setRequestHeader("Accept", "application/json");
    
    get.setQueryString(params);
    
    HTTPResponse response = this.httpRequest(this.client, get);
    
    if (response.getStatusCode() == 401)
    {
      throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
    }
    
    return response;
  }
  
  public HTTPResponse postAsMultipart(String url, Part[] parts)
  {
//    try {
      if (!isInitialized())
      {
        initialize();
      }
      
      PostMethod post = new PostMethod(this.getServerUrl() + url);
      
//      post.setRequestHeader("Content-Type", "multipart/form-data");
      
      MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(parts, post.getParams());
      
      post.setRequestEntity(multipartRequestEntity);
      
      HTTPResponse response = this.httpRequest(this.client, post);
      
      if (response.getStatusCode() == 401)
      {
        throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
      }
      
      return response;
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException(e);
//    }
  }
  
  public HTTPResponse httpPost(String url, String body)
  {
    if (!isInitialized())
    {
      initialize();
    }
    
    try
    {
      PostMethod post = new PostMethod(this.getServerUrl() + url);
      
      post.setRequestHeader("Content-Type", "application/json");
      
      post.setRequestEntity(new StringRequestEntity(body, null, null));
      
      HTTPResponse response = this.httpRequest(this.client, post);
      
      if (response.getStatusCode() == 401)
      {
        throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
      }
      
      return response;
    }
    catch (UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public HTTPResponse httpRequest(HttpClient client, HttpMethod method)
  {
    String sResponse = null;
    try
    {
      this.logger.info("Sending request to " + method.getURI());

      // Execute the method.
      int statusCode = client.executeMethod(method);
      
      // Follow Redirects
      if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT || statusCode == HttpStatus.SC_SEE_OTHER)
      {
        this.logger.info("Redirected [" + statusCode + "] to [" + method.getResponseHeader("location").getValue() + "].");
        method.setURI(new URI(method.getResponseHeader("location").getValue(), true, method.getParams().getUriCharset()));
        method.releaseConnection();
        return httpRequest(client, method);
      }
      
      // TODO : we might blow the memory stack here, read this as a stream somehow if possible.
      Header contentTypeHeader = method.getResponseHeader("Content-Type");
      if (contentTypeHeader == null)
      {
        sResponse = new String(method.getResponseBody(), "UTF-8");
      }
      else
      {
        sResponse = method.getResponseBodyAsString();
      }
      
      if (sResponse.length() < 1000)
      {
        this.logger.info("Response string = '" + sResponse + "'.");
      }
      else
      {
        this.logger.info("Receieved a very large response.");
      }
      
      return new HTTPResponse(sResponse, statusCode);
    }
    catch (ConnectException e)
    {
      throw new HttpConnectionException(e);
    }
    catch (HttpException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      method.releaseConnection();
    }
  }
}
