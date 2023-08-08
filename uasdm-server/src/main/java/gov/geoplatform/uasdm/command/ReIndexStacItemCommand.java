package gov.geoplatform.uasdm.command;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.service.IndexService;

public class ReIndexStacItemCommand implements Command
{
  private ProductIF product;

  public ReIndexStacItemCommand(ProductIF product)
  {
    this.product = product;
  }

  @Override
  public void doIt()
  {
    IndexService.createStacItems(this.product);
  }

  @Override
  public void undoIt()
  {
    ProductIF original = ComponentFacade.getProduct(this.product.getOid());

    IndexService.createStacItems(original);
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
