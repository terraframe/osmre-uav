package gov.geoplatform.uasdm.command;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.IndexService;

public class IndexUpdateDocumentCommand implements Command
{
  private UasComponentIF component;

  private boolean        isNameModified;

  public IndexUpdateDocumentCommand(UasComponentIF component, boolean isNameModified)
  {
    this.component = component;
    this.isNameModified = isNameModified;
  }

  @Override
  public void doIt()
  {
    IndexService.updateComponent(this.component, isNameModified);
  }

  @Override
  public void undoIt()
  {
    UasComponentIF original = ComponentFacade.getComponent(this.component.getOid());

    IndexService.updateComponent(original, isNameModified);
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
