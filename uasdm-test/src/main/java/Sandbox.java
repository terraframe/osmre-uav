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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.WorkflowTask;
import gov.geoplatform.uasdm.bus.WorkflowTaskQuery;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
import it.geosolutions.geoserver.rest.encoder.coverage.GSImageMosaicEncoder;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class Sandbox
{
  public static void main(String[] args) throws Exception
  {
    BodyContentHandler handler = new BodyContentHandler();
    Metadata metadata = new Metadata();
    FileInputStream inputstream = new FileInputStream(new File("pom.xml"));
    ParseContext pcontext = new ParseContext();
    
    //Xml parser
    XMLParser xmlparser = new XMLParser(); 
    xmlparser.parse(inputstream, handler, metadata, pcontext);
    System.out.println("Contents of the document:" + handler.toString());
    System.out.println("Metadata of the document:");
    String[] metadataNames = metadata.names();
    
    for(String name : metadataNames) {
       System.out.println(name + ": " + metadata.get(name));
    }    
//    testGetCount();

//    testGeoserver();
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
//    coverageEnc.setEnabled(true);
//    coverageEnc.setNativeCRS("EPSG:4326");
//    coverageEnc.setSRS("EPSG:4326");
    coverageEnc.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);

    // ... many other options are supported

    // create a new ImageMosaic layer...
//    publisher.publishExternalMosaic(workspace, layerName, baseDir, coverageEnc, layerEnc);
    final boolean published = publisher.publishExternalMosaic(workspace, layerName, baseDir, coverageEnc, layerEnc);

    // check the results
    if (!published)
    {
      System.out.println("Bad");
    }
  }
}
