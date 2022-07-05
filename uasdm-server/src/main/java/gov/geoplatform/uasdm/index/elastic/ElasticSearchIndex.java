package gov.geoplatform.uasdm.index.elastic;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.index.Index;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.QueryResult;

public class ElasticSearchIndex implements Index
{
  public static String INDEX_NAME = "components";

  private RestClient restClient;

  public ElasticSearchIndex()
  {
    String username = AppProperties.getElasticsearchUsername();
    String host = AppProperties.getElasticsearchHost();
    String password = AppProperties.getElasticsearchPassword();
    int port = AppProperties.getElasticsearchPort();
    String schema = AppProperties.getElasticsearchSchema();

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

    RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema)).setHttpClientConfigCallback(new HttpClientConfigCallback()
    {
      @Override
      public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
      {
        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }
    });

    this.restClient = builder.build();
  }

  @Override
  public void shutdown()
  {
    try
    {
      this.restClient.close();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private ElasticsearchClient createClient()
  {
    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(this.restClient, new JacksonJsonpMapper());

    // And create the API client
    return new ElasticsearchClient(transport);
  }

  public void deleteDocuments(String fieldId, String oid)
  {
    try
    {
      ElasticsearchClient client = createClient();
      client.deleteByQuery(new DeleteByQueryRequest.Builder().index(INDEX_NAME).query(q -> q.term(t -> t.field(fieldId).value(v -> v.stringValue(oid)))).build());
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void deleteDocument(UasComponentIF component, String key)
  {
    try
    {
      ElasticsearchClient client = createClient();
      client.deleteByQuery(new DeleteByQueryRequest.Builder().index(INDEX_NAME).query(q -> q.bool(b -> b.must(m -> m.term(t -> t.field("key").value(v -> v.stringValue(key)))).must(m -> m.term(t -> t.field(component.getSolrIdField()).value(v -> v.stringValue(component.getOid())))))).build());
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name)
  {
    Hit<ElasticDocument> existing = this.find(component, key);

    try
    {
      ElasticsearchClient client = createClient();

      ElasticDocument document = new ElasticDocument();
      document.setKey(key);
      document.setFilename(name);
      document.populate(component.getSolrIdField(), component.getOid());

      for (UasComponentIF ancestor : ancestors)
      {
        document.populate(ancestor.getSolrIdField(), ancestor.getOid());
        document.populate(ancestor.getSolrNameField(), ancestor.getName());
      }

      client.index(i -> i.index(INDEX_NAME).id(existing != null ? existing.id() : UUID.randomUUID().toString()).document(document));

    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata)
  {
    String content = IndexService.getContent(metadata);

    Hit<ElasticDocument> existing = this.find(component, key);

    try
    {
      ElasticsearchClient client = createClient();

      ElasticDocument document = new ElasticDocument();
      document.setKey(key);
      document.setFilename(name);
      document.setContent(content);
      document.populate(component.getSolrIdField(), component.getOid());
      document.populate(component.getSolrNameField(), component.getName());

      for (UasComponentIF ancestor : ancestors)
      {
        document.populate(ancestor.getSolrIdField(), ancestor.getOid());
        document.populate(ancestor.getSolrNameField(), ancestor.getName());
      }

      client.index(i -> i.index(INDEX_NAME).id(existing != null ? existing.id() : UUID.randomUUID().toString()).document(document));

    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private Hit<ElasticDocument> find(UasComponentIF component, String key)
  {
    try
    {
      ElasticsearchClient client = createClient();

      SearchResponse<ElasticDocument> search = client.search(s -> s.index(INDEX_NAME).query(q -> q.bool(b -> b.must(m -> m.term(t -> t.field("key").value(v -> v.stringValue(key)))).must(m -> m.term(t -> t.field(component.getSolrIdField()).value(v -> v.stringValue(component.getOid())))))), ElasticDocument.class);

      for (Hit<ElasticDocument> hit : search.hits().hits())
      {
        return hit;
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return null;
  }

  public void updateName(UasComponentIF component)
  {
    try
    {
      ElasticsearchClient client = createClient();

      client.updateByQuery(request -> request.index(INDEX_NAME).query(q -> q.match(m -> m.field(component.getSolrIdField()).query(component.getOid()))).script(s -> s.inline(i -> i.source("ctx.source." + component.getSolrNameField() + "='" + component.getName() + "'"))));
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void updateComponent(UasComponentIF component)
  {
    try
    {
      ElasticsearchClient client = createClient();

      client.updateByQuery(request -> request.index(INDEX_NAME).query(q -> q.match(m -> m.field("oid").query(component.getOid()))).script(s -> s.inline(i -> i.source("ctx.source.description='" + component.getDescription() + "'"))));
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void createDocument(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    try
    {
      ElasticsearchClient client = createClient();

      ElasticDocument document = new ElasticDocument();
      document.setOid(component.getOid());
      document.populate(component.getSolrIdField(), component.getOid());
      document.populate(component.getSolrNameField(), component.getName());
      document.setDescription(component.getDescription());

      if (component instanceof SiteIF)
      {
        document.setBureau( ( (SiteIF) component ).getBureau().getName());
      }

      for (UasComponentIF ancestor : ancestors)
      {
        document.populate(ancestor.getSolrIdField(), ancestor.getOid());
        document.populate(ancestor.getSolrNameField(), ancestor.getName());
      }

      client.index(i -> i.index(INDEX_NAME).id(UUID.randomUUID().toString()).document(document));
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public List<QueryResult> query(String text)
  {
    List<QueryResult> results = new LinkedList<QueryResult>();

    if (text != null && text.length() > 0)
    {
      try
      {
        ElasticsearchClient client = createClient();

        SearchResponse<ElasticDocument> search = client.search(s -> s.index(INDEX_NAME).query(q -> q.queryString(m -> m.fields("siteName", "projectName", "missionName", "collectionName", "bureau", "description").query("*" + ClientUtils.escapeQueryChars(text) + "*"))), ElasticDocument.class);

        for (Hit<ElasticDocument> hit : search.hits().hits())
        {
          results.add(this.buildQueryResult(hit));
        }
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }

    return results;
  }

  private QueryResult buildQueryResult(Hit<ElasticDocument> hit)
  {
    ElasticDocument document = hit.source();

    QueryResult result = new QueryResult();
    result.setId(hit.id());
    result.setFilename(document.getFilename());
    result.addItem(document.getSiteId(), document.getSiteName());
    result.addItem(document.getProjectId(), document.getProjectName());
    result.addItem(document.getMissionId(), document.getMissionName());
    result.addItem(document.getCollectionId(), document.getCollectionName());

    return result;
  }
}
