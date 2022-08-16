package gov.geoplatform.uasdm.mock;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;

import gov.geoplatform.uasdm.index.Index;
import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.view.QueryResult;

public class MockIndex implements Index
{

  public static enum IndexActionType {
    DELETE_DOCUMENTS, DELETE_DOCUMENT, UPDATE_DOCUMENT, UPDATE_METADATA, UPDATE_NAME, UPDATE_COMPONENT, QUERY, CREATE_STAC, REMOVE_STAC, GET_TOTALS, GET_STAC
  }

  public static class IndexAction
  {
    private IndexActionType type;

    private Object[] objects;

    public IndexAction(IndexActionType type, Object... objects)
    {
      this.type = type;
      this.objects = objects;
    }

    public IndexActionType getType()
    {
      return type;
    }

    public Object[] getObjects()
    {
      return objects;
    }

  }

  private List<IndexAction> actions = new LinkedList<IndexAction>();

  public List<IndexAction> getActions()
  {
    return actions;
  }

  @Override
  public boolean startup()
  {

    return false;
  }

  @Override
  public void shutdown()
  {

  }

  @Override
  public void deleteDocuments(String fieldId, String oid)
  {
    this.actions.add(new IndexAction(IndexActionType.DELETE_DOCUMENTS, fieldId, oid));
  }

  @Override
  public void deleteDocument(UasComponentIF component, String key)
  {
    this.actions.add(new IndexAction(IndexActionType.DELETE_DOCUMENT, component.getOid(), key));
  }

  @Override
  public void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name)
  {
    this.actions.add(new IndexAction(IndexActionType.UPDATE_DOCUMENT, ancestors, component.getOid(), key, name));
  }

  @Override
  public void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata)
  {
    this.actions.add(new IndexAction(IndexActionType.UPDATE_METADATA, ancestors, component.getOid(), key, name, metadata));
  }

  @Override
  public void updateName(UasComponentIF component)
  {
    this.actions.add(new IndexAction(IndexActionType.UPDATE_NAME, component.getOid()));
  }

  @Override
  public void updateComponent(UasComponentIF component)
  {
    this.actions.add(new IndexAction(IndexActionType.UPDATE_COMPONENT, component.getOid()));
  }

  @Override
  public void createDocument(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    this.actions.add(new IndexAction(IndexActionType.UPDATE_DOCUMENT, component.getOid()));
  }

  @Override
  public List<QueryResult> query(String text)
  {
    this.actions.add(new IndexAction(IndexActionType.QUERY, text));

    return new LinkedList<QueryResult>();
  }

  @Override
  public void createStacItems(ProductIF product)
  {
    this.actions.add(new IndexAction(IndexActionType.CREATE_STAC, product.getOid()));

    RemoteFileFacade.putStacItem(product.toStacItem());
  }

  @Override
  public void removeStacItems(ProductIF product)
  {
    this.actions.add(new IndexAction(IndexActionType.REMOVE_STAC, product.getOid()));

    RemoteFileFacade.removeStacItem(product);
  }

  @Override
  public JSONArray getTotals(String text, JSONArray filters)
  {
    this.actions.add(new IndexAction(IndexActionType.GET_TOTALS, filters));

    return new JSONArray();
  }

  @Override
  public Page<StacItem> getItems(JSONArray filters, Integer pageSize, Integer pageNumber)
  {
    this.actions.add(new IndexAction(IndexActionType.GET_STAC, filters, pageSize, pageNumber));

    return new Page<StacItem>(0, 1, 10, new LinkedList<StacItem>());
  }

}
