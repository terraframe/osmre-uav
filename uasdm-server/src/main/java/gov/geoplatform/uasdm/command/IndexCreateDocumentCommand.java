package gov.geoplatform.uasdm.command;

import java.util.List;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;

public class IndexCreateDocumentCommand implements Command
{
  private List<UasComponentIF> ancestors;

  private UasComponentIF       component;

  public IndexCreateDocumentCommand(List<UasComponentIF> ancestors, UasComponentIF component)
  {
    this.ancestors = ancestors;
    this.component = component;
  }

  @Override
  public void doIt()
  {
    IndexService.createDocument(this.ancestors, this.component);
  }

  @Override
  public void undoIt()
  {
    IndexService.deleteDocuments(this.component.getSolrIdField(), this.component.getOid());
  }

  @Override
  public void doFinally()
  {
  }

  @Override
  public String doItString()
  {
    return "";
  }

  @Override
  public String undoItString()
  {
    return "";
  }

  @Override
  public boolean isUndoable()
  {
    return false;
  }

}
