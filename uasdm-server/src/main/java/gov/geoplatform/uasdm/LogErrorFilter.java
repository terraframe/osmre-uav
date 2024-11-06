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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogErrorFilter implements Filter
{
  public static class ResponseWrapper implements HttpServletResponse
  {

    private HttpServletResponse resp;

    public ResponseWrapper(HttpServletResponse resp)
    {
      this.resp = resp;
    }

    public void addCookie(Cookie cookie)
    {
      resp.addCookie(cookie);
    }

    public boolean containsHeader(String name)
    {
      return resp.containsHeader(name);
    }

    public String encodeURL(String url)
    {
      return resp.encodeURL(url);
    }

    public String getCharacterEncoding()
    {
      return resp.getCharacterEncoding();
    }

    public String encodeRedirectURL(String url)
    {
      return resp.encodeRedirectURL(url);
    }

    public String getContentType()
    {
      return resp.getContentType();
    }

    public String encodeUrl(String url)
    {
      return resp.encodeUrl(url);
    }

    public String encodeRedirectUrl(String url)
    {
      return resp.encodeRedirectUrl(url);
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
      return resp.getOutputStream();
    }

    public void sendError(int sc, String msg) throws IOException
    {
      resp.sendError(sc, msg);
    }

    public PrintWriter getWriter() throws IOException
    {
      return resp.getWriter();
    }

    public void sendError(int sc) throws IOException
    {
      resp.sendError(sc);
    }

    public void setCharacterEncoding(String charset)
    {
      resp.setCharacterEncoding(charset);
    }

    public void sendRedirect(String location) throws IOException
    {
      resp.sendRedirect(location);
    }

    public void setDateHeader(String name, long date)
    {
      resp.setDateHeader(name, date);
    }

    public void setContentLength(int len)
    {
      resp.setContentLength(len);
    }

    public void addDateHeader(String name, long date)
    {
      resp.addDateHeader(name, date);
    }

    public void setContentType(String type)
    {
      resp.setContentType(type);
    }

    public void setHeader(String name, String value)
    {
      resp.setHeader(name, value);
    }

    public void addHeader(String name, String value)
    {
      resp.addHeader(name, value);
    }

    public void setBufferSize(int size)
    {
      resp.setBufferSize(size);
    }

    public void setIntHeader(String name, int value)
    {
      resp.setIntHeader(name, value);
    }

    public void addIntHeader(String name, int value)
    {
      resp.addIntHeader(name, value);
    }

    public void setStatus(int sc)
    {
      resp.setStatus(sc);
    }

    public int getBufferSize()
    {
      return resp.getBufferSize();
    }

    public void flushBuffer() throws IOException
    {
      resp.flushBuffer();
    }

    public void setStatus(int sc, String sm)
    {
      resp.setStatus(sc, sm);
    }

    public void resetBuffer()
    {
      resp.resetBuffer();
    }

    public int getStatus()
    {
      return resp.getStatus();
    }

    public boolean isCommitted()
    {
      return resp.isCommitted();
    }

    public String getHeader(String name)
    {
      return resp.getHeader(name);
    }

    public void reset()
    {
      resp.reset();
    }

    public Collection<String> getHeaders(String name)
    {
      return resp.getHeaders(name);
    }

    public void setLocale(Locale loc)
    {
      resp.setLocale(loc);
    }

    public Collection<String> getHeaderNames()
    {
      return resp.getHeaderNames();
    }

    public Locale getLocale()
    {
      return resp.getLocale();
    }

  }

  public void init(FilterConfig filterConfig) throws ServletException
  {
  }

  public void destroy()
  {
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
  {
    try
    {
      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse resp = (HttpServletResponse) response;
      String path = req.getRequestURI().substring(req.getContextPath().length());

      if (path.contains("notify"))
      {
        chain.doFilter(req, new ResponseWrapper(resp));
        System.out.println("Finished");
      }
      else
      {
        chain.doFilter(req, resp);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();

      throw e;
    }
  }
}
