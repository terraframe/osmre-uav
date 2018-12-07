package gov.geoplatform.uasdm.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.view.QueryResult;

public class SolrService
{

  public static void deleteDocuments(UasComponent component)
  {
    if (AppProperties.isSolrEnabled())
    {
      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          client.deleteByQuery(component.getSolrIdField() + ":" + ClientUtils.escapeQueryChars(component.getOid()));
          client.commit();
        }
        finally
        {
          client.close();
        }
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  public static void updateOrCreateDocument(List<UasComponent> ancestors, UasComponent component, String key, String name)
  {
    if (AppProperties.isSolrEnabled())
    {

      SolrDocument existing = SolrService.find(component, key);

      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          SolrInputDocument document = new SolrInputDocument();
          document.setField(component.getSolrIdField(), component.getOid());
          document.setField(component.getSolrNameField(), component.getName());
          document.setField("key", key);
          document.setField("filename", name);

          for (UasComponent ancestor : ancestors)
          {
            document.setField(ancestor.getSolrIdField(), ancestor.getOid());
            document.setField(ancestor.getSolrNameField(), ancestor.getName());
          }

          if (existing != null)
          {
            document.setField("id", existing.getFieldValue("id"));
          }
          else
          {
            document.setField("id", UUID.randomUUID().toString());
          }

          client.add(document);
          client.commit();
        }
        finally
        {
          client.close();
        }
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  public static void updateOrCreateMetadataDocument(List<UasComponent> ancestors, UasComponent component, String key, String name, File metadata)
  {
    if (AppProperties.isSolrEnabled())
    {

      SolrDocument document = SolrService.find(component, key);

      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          try (StringWriter writer = new StringWriter())
          {
            AutoDetectParser parser = new AutoDetectParser();

            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);

            try (FileInputStream istream = new FileInputStream(metadata))
            {
              parser.parse(istream, new BodyContentHandler(writer), new Metadata(), context);
            }

            SolrInputDocument iDocument = new SolrInputDocument();
            iDocument.setField(component.getSolrIdField(), component.getOid());
            iDocument.setField(component.getSolrNameField(), component.getName());
            iDocument.setField("key", key);
            iDocument.setField("filename", name);
            iDocument.setField("content", writer.toString());

            for (UasComponent ancestor : ancestors)
            {
              iDocument.setField(ancestor.getSolrIdField(), ancestor.getOid());
              iDocument.setField(ancestor.getSolrNameField(), ancestor.getName());
            }

            if (document != null)
            {
              iDocument.setField("id", document.getFieldValue("id"));
            }
            else
            {
              iDocument.setField("id", UUID.randomUUID().toString());
            }

            client.add(iDocument);
            client.commit();
          }
        }
        finally
        {
          client.close();
        }
      }
      catch (SAXException | TikaException e)
      {
        throw new ProgrammingErrorException(e);
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  public static SolrDocument find(UasComponent component, String key)
  {
    if (AppProperties.isSolrEnabled())
    {
      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          SolrQuery query = new SolrQuery();
          query.setQuery("key:" + ClientUtils.escapeQueryChars(key) + " AND " + component.getSolrIdField() + ":" + ClientUtils.escapeQueryChars(component.getOid()));
          query.setFields("id");
          query.setRows(20);

          QueryResponse response = client.query(query);
          SolrDocumentList list = response.getResults();

          Iterator<SolrDocument> iterator = list.iterator();

          if (iterator.hasNext())
          {
            SolrDocument document = iterator.next();
            String documentId = (String) document.getFieldValue("id");

            return client.getById(documentId);
          }
        }
        finally
        {
          client.close();
        }
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }

    return null;
  }

  public static void updateName(UasComponent component)
  {
    if (AppProperties.isSolrEnabled())
    {
      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          SolrQuery query = new SolrQuery();
          query.setQuery(component.getSolrIdField() + ":" + ClientUtils.escapeQueryChars(component.getOid()));
          query.setFields("id");

          QueryResponse response = client.query(query);
          SolrDocumentList list = response.getResults();

          Iterator<SolrDocument> iterator = list.iterator();

          if (iterator.hasNext())
          {
            SolrDocument document = iterator.next();

            SolrInputDocument iDocument = new SolrInputDocument();
            iDocument.setField(component.getSolrIdField(), component.getOid());
            iDocument.setField(component.getSolrNameField(), component.getName());
            iDocument.setField("id", document.getFieldValue("id"));

            client.add(iDocument);
          }

          client.commit();
        }
        finally
        {
          client.close();
        }
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  public static void createDocument(List<UasComponent> ancestors, UasComponent component)
  {
    if (AppProperties.isSolrEnabled())
    {
      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          SolrInputDocument iDocument = new SolrInputDocument();
          iDocument.setField("id", UUID.randomUUID().toString());
          iDocument.setField(component.getSolrIdField(), component.getOid());
          iDocument.setField(component.getSolrNameField(), component.getName());

          for (UasComponent ancestor : ancestors)
          {
            iDocument.setField(ancestor.getSolrIdField(), ancestor.getOid());
            iDocument.setField(ancestor.getSolrNameField(), ancestor.getName());
          }

          client.add(iDocument);
          client.commit();
        }
        finally
        {
          client.close();
        }
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  public static List<QueryResult> query(String text)
  {
    List<QueryResult> results = new LinkedList<QueryResult>();

    if (AppProperties.isSolrEnabled() && text != null && text.length() > 0)
    {
      try
      {
        HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

        try
        {
          StringBuilder ql = new StringBuilder();
          ql.append("_text_:" + "*" + ClientUtils.escapeQueryChars(text) + "*");

          SolrQuery query = new SolrQuery();
          query.setQuery(ql.toString());
          query.setFields("*");
          query.setRows(500);
          query.addSort("id", ORDER.asc); // Pay attention to this line
          String cursorMark = CursorMarkParams.CURSOR_MARK_START;

          boolean done = false;
          while (!done)
          {
            query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

            QueryResponse response = client.query(query);
            SolrDocumentList list = response.getResults();

            String nextCursorMark = response.getNextCursorMark();
            Iterator<SolrDocument> iterator = list.iterator();

            while (iterator.hasNext())
            {
              SolrDocument document = iterator.next();

              results.add(QueryResult.build(document));
            }

            if (cursorMark.equals(nextCursorMark))
            {
              done = true;
            }

            cursorMark = nextCursorMark;
          }
        }
        finally
        {
          client.close();
        }
      }
      catch (SolrServerException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }

    return results;
  }
}
