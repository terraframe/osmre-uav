package gov.geoplatform.uasdm.odm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.InvalidLoginException;

import gov.geoplatform.uasdm.DevProperties;

public class HTTPConnector
{
  private static final Logger logger = LoggerFactory.getLogger(HTTPConnector.class);

  /**
   * Tune these as needed.
   */
  private static final int CONNECT_TIMEOUT_MS = 10_000;
  private static final int CONNECTION_REQUEST_TIMEOUT_MS = 10_000;
  private static final int SOCKET_TIMEOUT_MS = 60_000;

  /**
   * How long an idle pooled connection can sit before we stop trusting it.
   * Apache will validate the connection before reuse if it has been idle longer
   * than this threshold.
   */
  private static final int VALIDATE_AFTER_INACTIVITY_MS = 5_000;

  /**
   * Pool sizing. Adjust if you truly need more concurrency.
   */
  private static final int MAX_TOTAL_CONNECTIONS = 50;
  private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

  private volatile CloseableHttpClient client;
  private volatile PoolingHttpClientConnectionManager connectionManager;

  private String serverurl;
  private String username;
  private String password;

  private final HTTPExceptionHandler handler;

  public HTTPConnector()
  {
    this.handler = e -> {
      throw new UnreachableHostException(e);
    };
  }

  public HTTPConnector(HTTPExceptionHandler handler)
  {
    this.handler = handler;
  }

  public void setCredentials(String username, String password)
  {
    this.username = username;
    this.password = password;
  }

  public String getServerUrl()
  {
    return this.serverurl;
  }

  public void setServerUrl(String url)
  {
    if (!url.endsWith("/"))
    {
      url = url + "/";
    }

    this.serverurl = url;
  }

  public synchronized void initialize()
  {
    if (this.client != null)
    {
      return;
    }

    try
    {
      HttpClientBuilder builder = HttpClients.custom();

      PoolingHttpClientConnectionManager cm = this.buildConnectionManager();
      cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
      cm.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
      cm.setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY_MS);

      RequestConfig defaultRequestConfig = RequestConfig.custom()
          .setConnectTimeout(CONNECT_TIMEOUT_MS)
          .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
          .setSocketTimeout(SOCKET_TIMEOUT_MS)
          .build();

      builder.setConnectionManager(cm);
      builder.setDefaultRequestConfig(defaultRequestConfig);

      HttpRequestRetryHandler backoffRetryHandler = new DefaultHttpRequestRetryHandler(4, false)
      {
        @Override
        public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context)
        {
          if (!super.retryRequest(exception, executionCount, context))
          {
            return false;
          }

          try
          {
            long sleepMs = (long) (500L * Math.pow(2, executionCount - 1));
            Thread.sleep(sleepMs);
          }
          catch (InterruptedException e)
          {
            Thread.currentThread().interrupt();
            return false;
          }

          return true;
        }
      };

      builder.setRetryHandler(backoffRetryHandler);

      // Evict connections that have expired or been idle too long.
      builder.evictExpiredConnections();
      builder.evictIdleConnections(30, java.util.concurrent.TimeUnit.SECONDS);

      if (username != null && password != null)
      {
        HttpHost target = HttpHost.create(serverurl);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        Credentials defaultCreds = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), defaultCreds);
        builder.setDefaultCredentialsProvider(credentialsProvider);
      }

      this.connectionManager = cm;
      this.client = builder.build();

      logger.info(
          "Initialized HTTPConnector for [{}] connectTimeout={}ms socketTimeout={}ms connectionRequestTimeout={}ms",
          this.serverurl,
          CONNECT_TIMEOUT_MS,
          SOCKET_TIMEOUT_MS,
          CONNECTION_REQUEST_TIMEOUT_MS);
    }
    catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private PoolingHttpClientConnectionManager buildConnectionManager()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException
  {
    if (DevProperties.isLocalKnowStac())
    {
      SSLContextBuilder sslBuilder = new SSLContextBuilder();
      sslBuilder.loadTrustMaterial(TrustAllStrategy.INSTANCE);

      SSLConnectionSocketFactory sslSocketFactory =
          new SSLConnectionSocketFactory(sslBuilder.build(), new NoopHostnameVerifier());

      Registry<ConnectionSocketFactory> socketFactoryRegistry =
          RegistryBuilder.<ConnectionSocketFactory>create()
              .register("http", PlainConnectionSocketFactory.getSocketFactory())
              .register("https", sslSocketFactory)
              .build();

      return new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    }

    return new PoolingHttpClientConnectionManager();
  }

  public boolean isInitialized()
  {
    return this.client != null;
  }

  public Response httpGet(String url, List<NameValuePair> params)
  {
    if (!isInitialized())
    {
      initialize();
    }

    try
    {
      URI uri = new URIBuilder(this.getServerUrl() + url).addParameters(params).build();

      HttpGet get = new HttpGet(uri);
      get.addHeader(new BasicHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()));

      Response response = this.httpRequest(get);

      if (response.getStatusCode() == 401)
      {
        throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
      }

      return response;
    }
    catch (URISyntaxException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public Response postAsMultipart(String url, HttpEntity entity)
  {
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
  }

  public Response httpPost(String url, List<NameValuePair> body)
  {
    if (!isInitialized())
    {
      initialize();
    }

    HttpPost post = new HttpPost(this.getServerUrl() + url);
    post.setEntity(new UrlEncodedFormEntity(body, Consts.UTF_8));

    Response response = this.httpRequest(post);

    if (response.getStatusCode() == 401)
    {
      throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
    }

    return response;
  }

  public Response httpPost(String url, String body)
  {
    if (!isInitialized())
    {
      initialize();
    }

    HttpPost post = new HttpPost(this.getServerUrl() + url);
    post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

    Response response = this.httpRequest(post);

    if (response.getStatusCode() == 401)
    {
      throw new InvalidLoginException("Unable to log in to " + this.getServerUrl());
    }

    return response;
  }

  public Response httpRequest(HttpUriRequest request)
  {
    HttpClientContext context = this.buildRequestContext();

    logger.debug("Request start: {} {}", request.getMethod(), request.getURI());

    try (CloseableHttpResponse response = client.execute(request, context))
    {
      int statusCode = response.getStatusLine().getStatusCode();
      HttpEntity responseEntity = response.getEntity();

      String responseBody = null;

      if (responseEntity != null)
      {
        responseBody = EntityUtils.toString(responseEntity);

        if (responseBody.length() < 1000)
        {
          logger.debug("Response body for [{} {}]: {}", request.getMethod(), request.getURI(), responseBody);
        }
        else
        {
          logger.debug("Response body for [{} {}] is {} chars",
              request.getMethod(),
              request.getURI(),
              responseBody.length());
        }
      }

      logger.debug("Request end: {} {} status={}", request.getMethod(), request.getURI(), statusCode);

      return new gov.geoplatform.uasdm.odm.HttpResponse(responseBody, statusCode);
    }
    catch (IOException e)
    {
      logger.debug("Request failed: {} {}", request.getMethod(), request.getURI(), e);

      this.handler.uncaughtException(e);

      return null;
    }
  }

  private HttpClientContext buildRequestContext()
  {
    if (username == null || password == null)
    {
      return null;
    }

    HttpHost target = HttpHost.create(serverurl);

    // Adding an authCache to a localContext will make the authentication preemptive.
    AuthCache authCache = new BasicAuthCache();
    authCache.put(target, new BasicScheme());

    HttpClientContext context = HttpClientContext.create();
    context.setAuthCache(authCache);

    return context;
  }

  /**
   * Optional cleanup hook if your application lifecycle supports it.
   */
  public synchronized void close()
  {
    if (this.client != null)
    {
      try
      {
        this.client.close();
      }
      catch (IOException e)
      {
        logger.warn("Error closing HTTP client", e);
      }
      finally
      {
        this.client = null;
      }
    }

    if (this.connectionManager != null)
    {
      this.connectionManager.close();
      this.connectionManager = null;
    }
  }

  /**
   * diagnostics helper.
   */
  public String getPoolStats()
  {
    HttpClientConnectionManager cm = this.connectionManager;

    if (cm instanceof PoolingHttpClientConnectionManager)
    {
      PoolingHttpClientConnectionManager pooling = (PoolingHttpClientConnectionManager) cm;
      return pooling.getTotalStats().toString();
    }

    return "Connection manager not initialized";
  }
}