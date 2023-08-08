package gov.geoplatform.uasdm.command;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.MetadataXMLGenerator;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;

public class GenerateMetadataCommand implements Command
{
  private CollectionIF collection;

  public GenerateMetadataCommand(CollectionIF collection)
  {
    this.collection = collection;
  }

  @Override
  public void doIt()
  {
    new MetadataXMLGenerator().generateAndUpload(this.collection);
  }

  @Override
  public void undoIt()
  {
    CollectionIF original = ComponentFacade.getCollection(this.collection.getOid());

    new MetadataXMLGenerator().generateAndUpload(original);
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
