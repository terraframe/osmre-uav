package gov.geoplatform.uasdm.index.solr;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.bus.Site;
import gov.geoplatform.uasdm.bus.UasComponent;
import gov.geoplatform.uasdm.index.Index;
import gov.geoplatform.uasdm.model.SiteIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.view.QueryResult;

public class SolrIndex implements Index
{

  @Override
  public void shutdown()
  {
  }

  public void deleteDocuments(String fieldId, String oid)
  {
    try
    {
      HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

      try
      {
        client.deleteByQuery(fieldId + ":" + ClientUtils.escapeQueryChars(oid));
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

  public void deleteDocument(UasComponentIF component, String key)
  {
    SolrDocument existing = this.find(component, key);

    try
    {
      HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

      try
      {
        client.deleteById((String) existing.getFieldValue("id"));

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

  public void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name)
  {
    SolrDocument existing = this.find(component, key);

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

        for (UasComponentIF ancestor : ancestors)
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

  public void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata)
  {
    String content = IndexService.getContent(metadata);

    SolrDocument document = this.find(component, key);

    try
    {
      HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

      try
      {
        SolrInputDocument iDocument = new SolrInputDocument();
        iDocument.setField(component.getSolrIdField(), component.getOid());
        iDocument.setField(component.getSolrNameField(), component.getName());
        iDocument.setField("key", key);
        iDocument.setField("filename", name);
        iDocument.setField("content", content);

        for (UasComponentIF ancestor : ancestors)
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

  private SolrDocument find(UasComponentIF component, String key)
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

    return null;
  }

  public void updateName(UasComponentIF component)
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

        while (iterator.hasNext())
        {
          SolrDocument document = iterator.next();
          Object id = document.getFieldValue("id");

          SolrInputDocument iDocument = new SolrInputDocument();
          iDocument.setField("id", id);
          iDocument.setField(component.getSolrNameField(), partialUpdate(component.getName()));

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

  public void updateComponent(UasComponentIF component)
  {
    try
    {
      HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

      try
      {
        SolrQuery query = new SolrQuery();
        query.setQuery("oid:" + ClientUtils.escapeQueryChars(component.getOid()));
        query.setFields("id");

        QueryResponse response = client.query(query);
        SolrDocumentList list = response.getResults();

        Iterator<SolrDocument> iterator = list.iterator();

        while (iterator.hasNext())
        {
          SolrDocument document = iterator.next();

          SolrInputDocument iDocument = new SolrInputDocument();
          iDocument.setField("id", document.getFieldValue("id"));
          iDocument.setField(UasComponent.DESCRIPTION, partialUpdate(component.getDescription()));

          if (component instanceof SiteIF)
          {
            iDocument.setField(Site.BUREAU, partialUpdate( ( (SiteIF) component ).getBureau().getName()));
          }

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

  private Map<String, String> partialUpdate(String value)
  {
    Map<String, String> partialUpdate = new HashMap<String, String>();
    partialUpdate.put("set", value);

    return partialUpdate;
  }

  public void createDocument(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    try
    {
      HttpSolrClient client = new HttpSolrClient.Builder(AppProperties.getSolrUrl()).build();

      try
      {
        SolrInputDocument iDocument = new SolrInputDocument();
        iDocument.setField("id", UUID.randomUUID().toString());
        iDocument.setField("oid", component.getOid());
        iDocument.setField(component.getSolrIdField(), component.getOid());
        iDocument.setField(component.getSolrNameField(), component.getName());
        iDocument.setField(UasComponent.DESCRIPTION, component.getDescription());

        if (component instanceof SiteIF)
        {
          iDocument.setField(Site.BUREAU, ( (SiteIF) component ).getBureau().getName());
        }

        for (UasComponentIF ancestor : ancestors)
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

  public List<QueryResult> query(String text)
  {
    List<QueryResult> results = new LinkedList<QueryResult>();

    if (text != null && text.length() > 0)
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
