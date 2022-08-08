package gov.geoplatform.uasdm.index;

import java.io.File;
import java.util.List;

import org.json.JSONArray;

import gov.geoplatform.uasdm.model.Page;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.StacItem;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.QueryResult;

public class MockIndex implements Index
{

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

  }

  @Override
  public void deleteDocument(UasComponentIF component, String key)
  {

  }

  @Override
  public void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name)
  {

  }

  @Override
  public void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata)
  {

  }

  @Override
  public void updateName(UasComponentIF component)
  {

  }

  @Override
  public void updateComponent(UasComponentIF component)
  {

  }

  @Override
  public void createDocument(List<UasComponentIF> ancestors, UasComponentIF component)
  {

  }

  @Override
  public List<QueryResult> query(String text)
  {

    return null;
  }

  @Override
  public void createStacItems(ProductIF product)
  {

  }

  @Override
  public void removeStacItems(ProductIF product)
  {

  }

  @Override
  public JSONArray getTotals(String text, JSONArray filters)
  {

    return null;
  }

  @Override
  public Page<StacItem> getItems(JSONArray filters, Integer pageSize, Integer pageNumber)
  {

    return null;
  }

}
