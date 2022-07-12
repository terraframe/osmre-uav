
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
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import org.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.FiltersBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import gov.geoplatform.uasdm.index.elastic.ElasticSearchIndex;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.service.IndexService;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSImageMosaicEncoder;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class Sandbox
{
  public static void main(String[] args) throws Exception
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
      // client.indices().create(i ->
      // i.index(ElasticSearchIndex.STAC_INDEX_NAME).mappings(m ->
      // m.properties("geometry", p -> p.geoShape(v ->
      // v)).properties("properties.datetime", p -> p.date(v -> v))));
      // GetIndexResponse response = client.indices().get(g ->
      // g.index(ElasticSearchIndex.STAC_INDEX_NAME));
      //
      // System.out.println(response);

      // client.index(i -> i.index("stac").ma);

      // client.index(i -> i.index("stac").document(result));
      final String text = "DDD";
      
      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(ElasticSearchIndex.STAC_INDEX_NAME).aggregations("totals", a -> a.filters(v -> v.filters(b -> {
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
      
      
      s.query(q -> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(2022, Calendar.JULY, 10);
        JsonData startDate = JsonData.of(sdf.format(calendar.getTime()));

        calendar.set(2022, Calendar.JULY, 12);
        JsonData endDate = JsonData.of(sdf.format(calendar.getTime()));
        
        q.bool(b -> b.must(m -> m.range(r -> r.field("properties.datetime").gte(startDate).lte(endDate))));

        return q;
      });
      
      SearchResponse<StacItem> search = client.search(s.build(), StacItem.class);

      Map<String, Aggregate> aggregations = search.aggregations();

      Aggregate totals = aggregations.get("totals");

      Map<String, FiltersBucket> child = totals.filters().buckets().keyed();
      FiltersBucket temp = child.get("site_count");
      System.out.println(totals);
      System.out.println("Site: " + temp.docCount());

      HitsMetadata<StacItem> metadata = search.hits();

      System.out.println(metadata.total());

      List<Hit<StacItem>> hits = metadata.hits();

      for (Hit<StacItem> hit : hits)
      {
        StacItem source = hit.source();

        System.out.println(mapper.writeValueAsString(source));
      }
    }
    
    JSONArray results = IndexService.getTotals("DD", new JSONArray());
    
    System.out.println(results);
    
    IndexService.shutdown();

    // BodyContentHandler handler = new BodyContentHandler();
    // Metadata metadata = new Metadata();
    // FileInputStream inputstream = new FileInputStream(new File("pom.xml"));
    // ParseContext pcontext = new ParseContext();
    //
    // //Xml parser
    // XMLParser xmlparser = new XMLParser();
    // xmlparser.parse(inputstream, handler, metadata, pcontext);
    // System.out.println("Contents of the document:" + handler.toString());
    // System.out.println("Metadata of the document:");
    // String[] metadataNames = metadata.names();
    //
    // for(String name : metadataNames) {
    // System.out.println(name + ": " + metadata.get(name));
    // }
    // testGetCount();

    // testGeoserver();
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

  public static void testGeoserver() throws FileNotFoundException
  {
    String geoserverData = System.getProperty("GEOSERVER_DATA_DIR");

    if (geoserverData == null)
    {
      throw new ProgrammingErrorException("Unable to find geoserver data directory: Please set the JVM arg GEOSERVER_DATA_DIR");
    }

    final File baseDir = new File(geoserverData + "/data/" + AppProperties.getPublicWorkspace());

    final GeoServerRESTPublisher publisher = GeoserverProperties.getPublisher();
    final String workspace = AppProperties.getPublicWorkspace();
    final String layerName = "image-public";

    final GSLayerEncoder layerEnc = new GSLayerEncoder();
    layerEnc.setDefaultStyle("raster");

    // coverage encoder
    final GSImageMosaicEncoder coverageEnc = new GSImageMosaicEncoder();
    coverageEnc.setName(layerName);
    coverageEnc.setTitle(layerName);
    // coverageEnc.setEnabled(true);
    // coverageEnc.setNativeCRS("EPSG:4326");
    // coverageEnc.setSRS("EPSG:4326");
    coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);

    // ... many other options are supported

    // create a new ImageMosaic layer...
    // publisher.publishExternalMosaic(workspace, layerName, baseDir,
    // coverageEnc, layerEnc);
    final boolean published = publisher.publishExternalMosaic(workspace, layerName, baseDir, coverageEnc, layerEnc);

    // check the results
    if (!published)
    {
      System.out.println("Bad");
    }
  }
}
