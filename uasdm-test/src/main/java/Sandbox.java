
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
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.index.elastic.ElasticSearchIndex;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class Sandbox
{
  public static void main(String[] args) throws Exception
  {
    // SimpleDateFormat format = new
    // SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
    // format.setTimeZone(TimeZone.getTimeZone("UTC"));
    //
    // String dateStr = format.format(new Date());
    // System.out.println(dateStr);
    //
    // System.out.println(format.parse(dateStr));

    request();
  }

  @Request
  public static void request() throws Exception
  {
    // final MdVertexDAOIF mdCollection =
    // MdVertexDAO.getMdVertexDAO(Collection.CLASS);
    //
    // StringBuilder builder = new StringBuilder();
    // builder.append("SELECT FROM " + mdCollection.getDBClassName());
    // builder.append(" ORDER BY name");
    //
    // final GraphQuery<CollectionIF> query = new
    // GraphQuery<CollectionIF>(builder.toString());
    //
    // query.getResults().forEach(collection -> {
    // collection.appLock();
    // collection.setMetadataUploaded(false);
    // collection.setUav(null);
    // collection.setSensor(null);
    // collection.apply();
    // });

    // POST /components/_delete_by_query
    // {"query":{"bool":{"must":[{"query_string":{"fields":["key"],"query":"abc/p1/m1/accessible_support/test.xml"}}]}}}
    // POST /components/_delete_by_query
    // {"query":{"bool":{"must":[{"query_string":{"fields":["key"],"query":"abc/p1/m1/accessible_support/test.xml"}}]}}}

    // IndexService.deleteDocuments("siteId",
    // "aa4e3701-57d3-42b2-9728-2934989ee957");
    //

    // JSONObject condition = new JSONObject();
    // condition.put("field", "faaNumber");
    // condition.put("value", "rgb");
    //
    // JSONArray must = new JSONArray();
    // must.add(condition);
    //
    // JSONObject criteria = new JSONObject();
    // criteria.put("must", must);

    JSONObject criteria = new JSONObject("{\"should\":[{\"field\":\"bounds\",\"id\":\"4642708e-ea82-4591-8a11-ff7039925118\",\"label\":\"Bounds\",\"value\":{\"_sw\":{\"lng\":-127.25881772939388,\"lat\":20.357148832341863},\"_ne\":{\"lng\":-64.67488227060727,\"lat\":52.711281241966475}}}],\"must\":[{\"field\":\"site\",\"id\":\"51956c50-c514-4694-97dd-e5723dbbb344\",\"label\":\"Site\",\"value\":\"abc\"}]}\n");

    Page<StacItem> items = IndexService.getItems(criteria, 30, 1);

    System.out.println(items.toJSON().toString());

    // testElasticSearch();

    IndexService.shutdown();
  }

  public static void testTika() throws Exception
  {
    BodyContentHandler handler = new BodyContentHandler();
    Metadata metadata = new Metadata();
    FileInputStream inputstream = new FileInputStream(new File("pom.xml"));
    ParseContext pcontext = new ParseContext();

    // Xml parser
    XMLParser xmlparser = new XMLParser();
    xmlparser.parse(inputstream, handler, metadata, pcontext);
    System.out.println("Contents of the document:" + handler.toString());
    System.out.println("Metadata of the document:");
    String[] metadataNames = metadata.names();

    for (String name : metadataNames)
    {
      System.out.println(name + ": " + metadata.get(name));
    }

  }

  public static void testRemoteStacItem() throws Exception
  {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    List<ProductIF> products = Product.getProducts();

    for (ProductIF product : products)
    {
      StacItem item = RemoteFileFacade.getStacItem(product);

      System.out.println(mapper.writeValueAsString(item));
      System.out.println();
    }
  }

  public static void testElasticSearch() throws Exception
  {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    // StacItem result = mapper.readValue(new
    // File("/home/jsmethie/git/osmre-uav/uasdm-test/src/test/resources/stac_item.json"),
    // StacItem.class);
    // // mapper.writeValue(System.out, result);
    // System.out.println(mapper.writeValueAsString(result));

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "JzhlvgF9NTA5JSoM7N7E"));

    RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https")).setHttpClientConfigCallback(new HttpClientConfigCallback()
    {
      @Override
      public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
      {
        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }
    });

    // Create the low-level client
    try (RestClient restClient = builder.build())
    {
      // Create the transport with a Jackson mapper
      ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

      // And create the API client
      ElasticsearchClient client = new ElasticsearchClient(transport);

      // client.indices().delete(i ->
      // i.index(ElasticSearchIndex.STAC_INDEX_NAME));
      // client.indices().delete(i ->
      // i.index(ElasticSearchIndex.COMPONENT_INDEX_NAME));

      // client.indices().get(g -> g.index(ElasticSearchIndex.STAC_INDEX_NAME));

      // // client.indices().create(i ->
      // // i.index(ElasticSearchIndex.STAC_INDEX_NAME).mappings(m ->
      // // m.properties("geometry", p -> p.geoShape(v ->
      // // v)).properties("properties.datetime", p -> p.date(v -> v))));
      // // GetIndexResponse response = client.indices().get(g ->
      // // g.index(ElasticSearchIndex.STAC_INDEX_NAME));
      // //
      // // System.out.println(response);
      //
      // // client.index(i -> i.index("stac").ma);
      //
      // // client.index(i -> i.index("stac").document(result));
      // final String text = "DDD";
      //
      // SearchRequest.Builder s = new SearchRequest.Builder();
      // s.index(ElasticSearchIndex.STAC_INDEX_NAME).aggregations("totals", a ->
      // a.filters(v -> v.filters(b -> {
      // HashMap<String, Query> map = new HashMap<String, Query>();
      // map.put("site_count", new Query.Builder().queryString(m ->
      // m.fields("properties.site").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("project_count", new Query.Builder().queryString(m ->
      // m.fields("properties.project").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("mission_count", new Query.Builder().queryString(m ->
      // m.fields("properties.mission").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("collection_count", new Query.Builder().queryString(m ->
      // m.fields("properties.collection").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("sensor_count", new Query.Builder().queryString(m ->
      // m.fields("properties.sensor").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("platform_count", new Query.Builder().queryString(m ->
      // m.fields("properties.platform").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("faa_number_count", new Query.Builder().queryString(m ->
      // m.fields("properties.faaNumber").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      // map.put("serial_number_count", new Query.Builder().queryString(m ->
      // m.fields("properties.serialNumber").query("*" +
      // ClientUtils.escapeQueryChars(text) + "*")).build());
      //
      // return b.keyed(map);
      // })));
      //
      // s.query(q -> {
      // SimpleDateFormat sdf = new
      // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      // sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      //
      // Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      // calendar.clear();
      // calendar.set(2022, Calendar.JULY, 10);
      // JsonData startDate = JsonData.of(sdf.format(calendar.getTime()));
      //
      // calendar.set(2022, Calendar.JULY, 12);
      // JsonData endDate = JsonData.of(sdf.format(calendar.getTime()));
      //
      // q.bool(b -> b.must(m -> m.range(r ->
      // r.field("properties.datetime").gte(startDate).lte(endDate))));
      //
      // return q;
      // });
      //
      // SearchResponse<StacItem> search = client.search(s.build(),
      // StacItem.class);
      //
      // Map<String, Aggregate> aggregations = search.aggregations();
      //
      // Aggregate totals = aggregations.get("totals");
      //
      // Map<String, FiltersBucket> child = totals.filters().buckets().keyed();
      // FiltersBucket temp = child.get("site_count");
      // System.out.println(totals);
      // System.out.println("Site: " + temp.docCount());
      //

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(ElasticSearchIndex.STAC_INDEX_NAME, ElasticSearchIndex.COMPONENT_INDEX_NAME).size(1000);

      SearchResponse<JsonNode> search = client.search(s.build(), JsonNode.class);

      HitsMetadata<JsonNode> metadata = search.hits();

      List<Hit<JsonNode>> hits = metadata.hits();

      for (Hit<JsonNode> hit : hits)
      {
        System.out.println(hit.source().toPrettyString());
      }
    }

    // JSONArray results = IndexService.getTotals("ABC", new JSONArray());
    //
    // System.out.println(results);
    //
    // IndexService.shutdown();

  }

  @Request
  public static void testGetCount() throws SQLException
  {
    int pageNumber = 1;
    int pageSize = 5;

    ValueQuery vQuery = new ValueQuery(new QueryFactory());
    WorkflowTaskQuery query = new WorkflowTaskQuery(vQuery);

    vQuery.SELECT_DISTINCT(query.getComponent());

    vQuery.restrictRows(pageSize, pageNumber);

    List<String> components = new LinkedList<String>();

    try (OIterator<ValueObject> iterator = vQuery.getIterator(pageSize, pageNumber))
    {
      while (iterator.hasNext())
      {
        ValueObject vObject = iterator.next();
        String component = vObject.getValue(WorkflowTask.COMPONENT);

        components.add(component);
      }
    }

  }
}
