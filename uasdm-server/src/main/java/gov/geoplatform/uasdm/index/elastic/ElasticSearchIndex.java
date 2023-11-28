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
package gov.geoplatform.uasdm.index.elastic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Session;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.GeoShapeRelation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.FiltersBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.index.Index;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.QueryLocationResult;
import gov.geoplatform.uasdm.view.QueryResult;
import gov.geoplatform.uasdm.view.QuerySiteResult;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.conversion.LocalizedValueConverter;

public class ElasticSearchIndex implements Index
{
  private static Logger logger               = LoggerFactory.getLogger(ElasticSearchIndex.class);

  public static String  COMPONENT_INDEX_NAME = "components";

  public static String  STAC_INDEX_NAME      = "stac";

  public static String  LOCATION_INDEX_NAME  = "location";

  private RestClient    restClient;

  @Override
  public boolean startup()
  {
    return true;
  }

  private synchronized RestClient getRestClient()
  {
    if (this.restClient == null)
    {
      final int MAX_TRIES = 5;

      int count = 1;

      while (this.restClient == null && count < MAX_TRIES)
      {
        try
        {
          logger.debug("Attempting to check existence of elasticsearch");

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

          ElasticsearchTransport transport = new RestClientTransport(this.restClient, new JacksonJsonpMapper());

          // And create the API client
          ElasticsearchClient client = new ElasticsearchClient(transport);

          try
          {
            client.indices().get(g -> g.index(ElasticSearchIndex.STAC_INDEX_NAME));
          }
          catch (ElasticsearchException e)
          {
            // Index doesn't exist, create it
            client.indices().create(i -> i.index(ElasticSearchIndex.STAC_INDEX_NAME).mappings(m -> m.properties("geometry", p -> p.geoShape(v -> v)).properties("properties.datetime", p -> p.date(v -> v)).properties("properties.start_datetime", p -> p.date(v -> v)).properties("properties.end_datetime", p -> p.date(v -> v)).properties("properties.updated", p -> p.date(v -> v)).properties("properties.created", p -> p.date(v -> v))));
          }

          try
          {
            client.indices().get(g -> g.index(ElasticSearchIndex.LOCATION_INDEX_NAME));
          }
          catch (ElasticsearchException e)
          {
            // Index doesn't exist, create it
            client.indices().create(i -> i.index(ElasticSearchIndex.LOCATION_INDEX_NAME));
          }

          try
          {
            client.indices().get(g -> g.index(ElasticSearchIndex.COMPONENT_INDEX_NAME));
          }
          catch (ElasticsearchException e)
          {
            // Index doesn't exist, create it
            client.indices().create(i -> i.index(ElasticSearchIndex.COMPONENT_INDEX_NAME));
          }
        }
        catch (Exception e)
        {
          this.restClient = null;

          try
          {
            logger.debug("Waiting for Index to start. Attempt [" + count + "]");
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // Ignore
          }

        }

        count++;
      }
    }

    if (this.restClient == null)
    {
      throw new ProgrammingErrorException("Unable to connect to elastic search");
    }

    return this.restClient;

  }

  @Override
  public synchronized void shutdown()
  {
    try
    {
      if (this.restClient != null)
      {
        this.restClient.close();

        this.restClient = null;
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private ElasticsearchClient createClient()
  {
    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(this.getRestClient(), new JacksonJsonpMapper());

    // And create the API client
    return new ElasticsearchClient(transport);
  }

  @Override
  public void clear()
  {
    try
    {
      ElasticsearchClient client = createClient();
      client.indices().delete(i -> i.index(STAC_INDEX_NAME));
      client.indices().delete(i -> i.index(COMPONENT_INDEX_NAME));
      client.indices().delete(i -> i.index(LOCATION_INDEX_NAME));

      this.shutdown();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void deleteDocuments(String fieldId, String oid)
  {
    try
    {
      DeleteByQueryRequest request = new DeleteByQueryRequest.Builder().index(COMPONENT_INDEX_NAME).query(q -> {
        return q.bool(b -> b.must(m -> m.queryString(qs -> qs.fields(fieldId).query(oid))));
      }).build();

      ElasticsearchClient client = createClient();
      client.deleteByQuery(request);
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
      List<Query> conditions = new LinkedList<Query>();
      conditions.add(new Query.Builder().queryString(m -> m.fields(component.getSolrIdField()).query(component.getOid())).build());
      conditions.add(new Query.Builder().queryString(m -> m.fields("key").query(key)).build());

      DeleteByQueryRequest request = new DeleteByQueryRequest.Builder().index(COMPONENT_INDEX_NAME).query(q -> q.bool(b -> b.filter(conditions))).build();

      ElasticsearchClient client = createClient();
      client.deleteByQuery(request);
    }
    catch (

    IOException e)
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
      document.populate(component.getSolrNameField(), component.getName());

      for (UasComponentIF ancestor : ancestors)
      {
        document.populate(ancestor.getSolrIdField(), ancestor.getOid());
        document.populate(ancestor.getSolrNameField(), ancestor.getName());
      }

      client.index(i -> i.index(COMPONENT_INDEX_NAME).id(existing != null ? existing.id() : UUID.randomUUID().toString()).document(document));

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

      client.index(i -> i.index(COMPONENT_INDEX_NAME).id(existing != null ? existing.id() : UUID.randomUUID().toString()).document(document));

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

      SearchResponse<ElasticDocument> search = client.search(s -> s.index(COMPONENT_INDEX_NAME).query(q -> q.bool(b -> b.must(m -> m.term(t -> t.field("key").value(v -> v.stringValue(key)))).must(m -> m.term(t -> t.field(component.getSolrIdField()).value(v -> v.stringValue(component.getOid())))))), ElasticDocument.class);

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

  @Override
  public void updateComponent(UasComponentIF component, boolean isNameModified)
  {
    try
    {
      ElasticsearchClient client = createClient();

      SearchResponse<ElasticDocument> search = client.search(s -> s.index(COMPONENT_INDEX_NAME).query(q -> q.match(m -> m.field("oid").query(component.getOid()))), ElasticDocument.class);

      for (Hit<ElasticDocument> hit : search.hits().hits())
      {
        ElasticDocument document = hit.source();
        document.setDescription(component.getDescription());
        document.populate(component.getSolrNameField(), component.getName());

        if (component instanceof SiteIF)
        {
          document.setBureau( ( (SiteIF) component ).getServerOrganization().getDisplayLabel().getValue());
        }

        client.update(b -> b.index(COMPONENT_INDEX_NAME).id(hit.id()).doc(document), Void.class);
      }

      if (isNameModified)
        client.updateByQuery(request -> request.index(COMPONENT_INDEX_NAME).query(q -> q.bool(b -> {
          b.mustNot(mu -> mu.match(m -> m.field("oid").query(component.getOid())));
          b.must(mu -> mu.match(m -> m.field(component.getSolrIdField()).query(component.getOid())));

          return b;
        })).script(s -> s.inline(i -> i.source("ctx._source." + component.getSolrNameField() + "='" + component.getName() + "'"))));

      // client.updateByQuery(request ->
      // request.index(COMPONENT_INDEX_NAME).conflicts(Conflicts.Proceed).query(q
      // -> q.match(m -> m.field("oid").query(component.getOid()))).script(s ->
      // s.inline(i -> i.source("ctx._source.description='" +
      // component.getDescription() + "'"))));
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

      client.index(i -> i.index(COMPONENT_INDEX_NAME).id(UUID.randomUUID().toString()).document(document));
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

        SearchResponse<ElasticDocument> search = client.search(s -> s.index(COMPONENT_INDEX_NAME, LOCATION_INDEX_NAME).query(q -> q.queryString(m -> m.fields("siteName", "projectName", "missionName", "collectionName", "bureau", "description", "filename", "label").query("*" + ClientUtils.escapeQueryChars(text) + "*"))), ElasticDocument.class);

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

    if (hit.index().equals(LOCATION_INDEX_NAME))
    {
      QueryLocationResult result = new QueryLocationResult();
      result.setId(hit.id());
      result.setVersionId(document.getVersionId());
      result.setSynchronizationId(document.getSynchronizationId());
      result.setLabel(document.getLabel());
      result.setOid(document.getOid());
      result.setTypeLabel(document.getTypeLabel());
      result.setHierarchyLabel(document.getHierarchyLabel());

      return result;
    }
    else
    {
      QuerySiteResult result = new QuerySiteResult();
      result.setId(hit.id());
      result.setFilename(document.getFilename());
      result.addItem(document.getSiteId(), document.getSiteName());
      result.addItem(document.getProjectId(), document.getProjectName());
      result.addItem(document.getMissionId(), document.getMissionName());
      result.addItem(document.getCollectionId(), document.getCollectionName());

      return result;
    }
  }

  @Override
  public void createStacItems(ProductIF product)
  {
    StacItem item = product.toStacItem();

    if (item != null)
    {
      try
      {
        ElasticsearchClient client = createClient();

        client.index(i -> i.index(STAC_INDEX_NAME).id(product.getOid()).document(item));
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }

    // Due to the fact that the thumbnail endpoints are in the private s3 bucket
    // we must remove the thumbnail asset from the STAC json uploaded to the
    // buckets because we do not want public STAC items to contain urls to
    // private files. However, the front-end uses the thumbnail information on
    // the search results panel. As such, we still need the thumbnail asset in
    // the index.
    item.removeAsset("thumbnail");

    RemoteFileFacade.putStacItem(item);
  }

  @Override
  public void removeStacItems(ProductIF product)
  {
    try
    {
      ElasticsearchClient client = createClient();
      client.deleteByQuery(new DeleteByQueryRequest.Builder().index(STAC_INDEX_NAME).query(q -> q.match(m -> m.field("id").query(product.getOid()))).build());
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    RemoteFileFacade.removeStacItem(product);
  }

  public JSONArray getTotals(String text, JSONArray filters)
  {
    JSONArray results = new JSONArray();

    try
    {
      ElasticsearchClient client = createClient();

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(ElasticSearchIndex.STAC_INDEX_NAME).size(0).aggregations("totals", a -> a.filters(v -> v.filters(b -> {
        HashMap<String, Query> map = new HashMap<String, Query>();
        map.put("site_count", new Query.Builder().queryString(m -> m.fields("properties.site").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("project_count", new Query.Builder().queryString(m -> m.fields("properties.project").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("mission_count", new Query.Builder().queryString(m -> m.fields("properties.mission").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("collection_count", new Query.Builder().queryString(m -> m.fields("properties.collection").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("sensor_count", new Query.Builder().queryString(m -> m.fields("properties.sensor").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("platform_count", new Query.Builder().queryString(m -> m.fields("properties.platform").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("faa_number_count", new Query.Builder().queryString(m -> m.fields("properties.faaNumber").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());
        map.put("serial_number_count", new Query.Builder().queryString(m -> m.fields("properties.serialNumber").query("*" + ClientUtils.escapeQueryChars(text) + "*")).build());

        return b.keyed(map);
      })));

      if (filters != null && filters.length() > 0)
      {
        s.query(q -> q.bool(b -> b.must(m -> {
          for (int i = 0; i < filters.length(); i++)
          {
            JSONObject filter = filters.getJSONObject(i);
            String field = filter.getString("field");

            if (field.equals("datetime"))
            {
              if (filter.has("startDate") || filter.has("endDate"))
              {
                m.range(r -> {
                  r.field("properties.datetime");

                  if (filter.has("startDate"))
                  {
                    r.gte(JsonData.of(filter.getString("startDate")));
                  }

                  if (filter.has("endDate"))
                  {
                    r.lte(JsonData.of(filter.getString("endDate")));
                  }

                  return r;
                });
              }

            }
            else
            {
              String value = filter.getString("value");
              m.queryString(qs -> qs.fields("properties." + field).query("*" + ClientUtils.escapeQueryChars(value) + "*"));
            }
          }

          return m;
        })));

      }

      SearchResponse<StacItem> search = client.search(s.build(), StacItem.class);

      Map<String, Aggregate> aggregations = search.aggregations();

      Aggregate totals = aggregations.get("totals");

      Map<String, FiltersBucket> buckets = totals.filters().buckets().keyed();

      if (buckets.get("site_count").docCount() > 0)
      {
        results.put(buildTotal("Site", "site", buckets.get("site_count").docCount()));
      }

      if (buckets.get("project_count").docCount() > 0)
      {
        results.put(buildTotal("Project", "project", buckets.get("project_count").docCount()));
      }

      if (buckets.get("mission_count").docCount() > 0)
      {
        results.put(buildTotal("Mission", "mission", buckets.get("mission_count").docCount()));
      }

      if (buckets.get("collection_count").docCount() > 0)
      {
        results.put(buildTotal("Collection", "collection", buckets.get("collection_count").docCount()));
      }

      if (buckets.get("sensor_count").docCount() > 0)
      {
        results.put(buildTotal("Sensor", "sensor", buckets.get("sensor_count").docCount()));
      }

      if (buckets.get("platform_count").docCount() > 0)
      {
        results.put(buildTotal("Platform", "platform", buckets.get("platform_count").docCount()));
      }

      if (buckets.get("faa_number_count").docCount() > 0)
      {
        results.put(buildTotal("FAA Number", "faaNumber", buckets.get("faa_number_count").docCount()));
      }

      if (buckets.get("serial_number_count").docCount() > 0)
      {
        results.put(buildTotal("Serial Number", "serialNumber", buckets.get("serial_number_count").docCount()));
      }
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return results;
  }

  public Page<StacItem> getItems(JSONObject criteria, Integer pageSize, Integer pageNumber)
  {
    Page<StacItem> page = new Page<StacItem>();
    page.setPageNumber(pageNumber);
    page.setPageSize(pageSize);

    List<StacItem> items = new LinkedList<StacItem>();

    try
    {
      ElasticsearchClient client = createClient();

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(ElasticSearchIndex.STAC_INDEX_NAME);
      s.size(pageSize);
      s.from(pageSize * ( pageNumber - 1 ));

      if (criteria.has("must") || criteria.has("should"))
      {

        s.query(q -> q.bool(b -> {
          if (criteria.has("must"))
          {
            JSONArray filters = criteria.getJSONArray("must");

            if (filters != null && filters.length() > 0)
            {
              List<Query> conditions = new LinkedList<Query>();

              for (int i = 0; i < filters.length(); i++)
              {
                JSONObject filter = filters.getJSONObject(i);
                String field = filter.getString("field");

                if (field.equals("datetime"))
                {
                  if (filter.has("startDate") || filter.has("endDate"))
                  {
                    conditions.add(new Query.Builder().range(r -> {
                      r.field("properties.datetime");

                      if (filter.has("startDate"))
                      {
                        r.gte(JsonData.of(filter.getString("startDate")));
                      }

                      if (filter.has("endDate"))
                      {
                        r.lte(JsonData.of(filter.getString("endDate")));
                      }

                      return r;
                    }).build());
                  }

                }
                else
                {
                  String value = filter.getString("value");

                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + field).query("*" + ClientUtils.escapeQueryChars(value) + "*")).build());
                }
              }

              b.filter(conditions);
            }
          }
          else
          {
            b.must(m -> m.matchAll(ma -> ma.boost(0F)));
          }

          if (criteria.has("should"))
          {
            JSONArray filters = criteria.getJSONArray("should");

            if (filters != null && filters.length() > 0)
            {
              List<Query> conditions = new LinkedList<Query>();

              for (int i = 0; i < filters.length(); i++)
              {
                JSONObject filter = filters.getJSONObject(i);
                String field = filter.getString("field");

                if (field.equalsIgnoreCase("bounds"))
                {
                  JSONObject object = filter.getJSONObject("value");

                  JSONObject sw = object.getJSONObject("_sw");
                  JSONObject ne = object.getJSONObject("_ne");

                  double x1 = sw.getDouble("lng");
                  double x2 = ne.getDouble("lng");
                  double y1 = sw.getDouble("lat");
                  double y2 = ne.getDouble("lat");

                  Envelope envelope = new Envelope(x1, x2, y1, y2);
                  GeometryFactory factory = new GeometryFactory();
                  Geometry geometry = factory.toGeometry(envelope);
                  WKTWriter writer = new WKTWriter();

                  conditions.add(new Query.Builder().geoShape(qs -> qs.boost(10F).field("geometry").shape(bb -> bb.relation(GeoShapeRelation.Intersects).shape(JsonData.of(writer.write(geometry))))).build());
                }
                else
                {
                  String value = filter.getString("value");
                  conditions.add(new Query.Builder().queryString(qs -> qs.boost(1.2F).fields("properties." + field).query("*" + ClientUtils.escapeQueryChars(value) + "*")).build());
                }
              }

              b.should(conditions);
            }
          }

          return b;
        }));

      }

      SearchRequest request = s.build();

      SearchResponse<StacItem> search = client.search(request, StacItem.class);
      HitsMetadata<StacItem> hits = search.hits();

      page.setCount(hits.total().value());

      for (Hit<StacItem> hit : hits.hits())
      {
        items.add(hit.source());
      }
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    page.setResults(items);

    return page;
  }

  private static JSONObject buildTotal(String label, String key, long total)
  {
    JSONObject object = new JSONObject();
    object.put("key", key);
    object.put("label", label);
    object.put("total", total);

    return object;
  }

  @Override
  public void createDocument(LabeledPropertyGraphSynchronization synchronization, VertexObject object)
  {
    try
    {
      LocalizedValue localizedValue = LocalizedValueConverter.convert(object.getEmbeddedComponent("displayLabel"));
      String label = localizedValue.getValue();

      ElasticsearchClient client = createClient();

      ElasticLocation document = new ElasticLocation();
      document.setOid(object.getOid());
      document.setSynchronizationId(synchronization.getOid());
      document.setVersionId(synchronization.getVersionOid());
      document.setLabel(label);
      document.setHierarchyLabel(synchronization.getDisplayLabel().getValue());
      document.setTypeLabel(object.getMdClass().getDisplayLabel(Session.getCurrentLocale()));

      client.index(i -> i.index(LOCATION_INDEX_NAME).id(UUID.randomUUID().toString()).document(document));
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void deleteDocuments(LabeledPropertyGraphSynchronization synchronization)
  {
    try
    {
      DeleteByQueryRequest request = new DeleteByQueryRequest.Builder().index(LOCATION_INDEX_NAME).query(q -> {
        return q.bool(b -> b.must(m -> m.queryString(qs -> qs.fields("versionId").query(synchronization.getVersionOid()))));
      }).build();

      ElasticsearchClient client = createClient();
      client.deleteByQuery(request);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public void deleteDocuments(LabeledPropertyGraphTypeVersion version)
  {
    try
    {
      DeleteByQueryRequest request = new DeleteByQueryRequest.Builder().index(LOCATION_INDEX_NAME).query(q -> {
        return q.bool(b -> b.must(m -> m.queryString(qs -> qs.fields("versionId").query(version.getOid()))));
      }).build();

      ElasticsearchClient client = createClient();
      client.deleteByQuery(request);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
}
