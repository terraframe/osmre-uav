package gov.geoplatform.uasdm.index;

import java.io.File;
import java.util.List;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.view.QueryResult;

public interface Index
{
  public void deleteDocuments(String fieldId, String oid);

  public void deleteDocument(UasComponentIF component, String key);

  public void updateOrCreateDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name);

  public void updateOrCreateMetadataDocument(List<UasComponentIF> ancestors, UasComponentIF component, String key, String name, File metadata);

  public void updateName(UasComponentIF component);

  public void updateComponent(UasComponentIF component);

  public void createDocument(List<UasComponentIF> ancestors, UasComponentIF component);

  public List<QueryResult> query(String text);

  public void shutdown();
}
