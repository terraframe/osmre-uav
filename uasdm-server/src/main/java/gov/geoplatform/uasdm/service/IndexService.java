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
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.index.Index;
import gov.geoplatform.uasdm.index.elastic.ElasticSearchIndex;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.QueryResult;

public class IndexService
{
  private static Index index = new ElasticSearchIndex();

  public synchronized static void setIndex(Index i)
  {
    index = i;
  }
  
  public synchronized static Index getIndex()
  {
    return index;
  }

  public static boolean startup()
  {
    if (AppProperties.isSearchEnabled())
    {
      return index.startup();
    }

    return true;
  }

  public static void shutdown()
  {
    if (AppProperties.isSearchEnabled())
    {
      index.shutdown();
    }
  }

  public static void deleteDocuments(String fieldId, String oid)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.deleteDocuments(fieldId, oid);
    }
  }

  public static void deleteDocument(UasComponentIF component, String key)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.deleteDocument(component, key);
    }
  }

  public static void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.updateOrCreateDocument(ancestors, component, key, name);
    }
  }

  public static void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.updateOrCreateMetadataDocument(ancestors, component, key, name, metadata);
    }
  }

  public static void updateName(UasComponentIF component)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.updateName(component);
    }
  }

  public static void updateComponent(UasComponentIF component)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.updateComponent(component);
    }
  }

  public static void createDocument(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.createDocument(ancestors, component);
    }
  }

  public static List<QueryResult> query(String text)
  {
    if (AppProperties.isSearchEnabled())
    {
      return index.query(text);
    }

    return new LinkedList<QueryResult>();
  }

  public static void createStacItems(ProductIF product)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.createStacItems(product);
    }
  }

  public static void removeStacItems(ProductIF product)
  {
    if (AppProperties.isSearchEnabled())
    {
      index.removeStacItems(product);
    }
  }

  public static JSONArray getTotals(String text, JSONArray filters)
  {
    if (AppProperties.isSearchEnabled())
    {
      return index.getTotals(text, filters);
    }

    return new JSONArray();
  }

  public static Page<StacItem> getItems(JSONObject criteria, Integer pageSize, Integer pageNumber)
  {
    if (AppProperties.isSearchEnabled())
    {
      return index.getItems(criteria, pageSize, pageNumber);
    }

    return new Page<StacItem>();
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
