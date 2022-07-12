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
package gov.geoplatform.uasdm.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONArray;
import org.xml.sax.SAXException;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.index.Index;
import gov.geoplatform.uasdm.index.elastic.ElasticSearchIndex;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.QueryResult;

public class IndexService
{
  private static Index index = new ElasticSearchIndex();

  public static void startup()
  {
    if (AppProperties.isSolrEnabled())
    {
      index.startup();
    }
  }
  
  public static void shutdown()
  {
    if (AppProperties.isSolrEnabled())
    {
      index.shutdown();
    }
  }

  public static void deleteDocuments(String fieldId, String oid)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.deleteDocuments(fieldId, oid);
    }
  }

  public static void deleteDocument(UasComponentIF component, String key)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.deleteDocument(component, key);
    }
  }

  public static void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.updateOrCreateDocument(ancestors, component, key, name);
    }
  }

  public static void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.updateOrCreateMetadataDocument(ancestors, component, key, name, metadata);
    }
  }

  public static void updateName(UasComponentIF component)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.updateName(component);
    }
  }

  public static void updateComponent(UasComponentIF component)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.updateComponent(component);
    }
  }

  public static void createDocument(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.createDocument(ancestors, component);
    }
  }

  public static List<QueryResult> query(String text)
  {
    if (AppProperties.isSolrEnabled())
    {
      return index.query(text);
    }

    return new LinkedList<QueryResult>();
  }

  public static void createStacItems(ProductIF product)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.createStacItems(product);
    }
  }

  public static void removeStacItems(ProductIF product)
  {
    if (AppProperties.isSolrEnabled())
    {
      index.removeStacItems(product);
    }
  }

  public static JSONArray getTotals(String text, JSONArray filters)
  {
    if (AppProperties.isSolrEnabled())
    {
      return index.getTotals(text, filters);
    }

    return new JSONArray();
  }

  public static String getContent(File metadata)
  {
    try
    {
      BodyContentHandler handler = new BodyContentHandler();
      AutoDetectParser parser = new AutoDetectParser();

      try (FileInputStream istream = new FileInputStream(metadata))
      {
        parser.parse(istream, handler, new Metadata());
      }

      return handler.toString();
    }
    catch (SAXException | TikaException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

}
