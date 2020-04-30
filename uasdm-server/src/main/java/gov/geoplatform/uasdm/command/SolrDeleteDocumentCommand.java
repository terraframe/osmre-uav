package gov.geoplatform.uasdm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.service.SolrService;

public class SolrDeleteDocumentCommand implements Command
{
  private Logger         log = LoggerFactory.getLogger(SolrDeleteDocumentCommand.class);

  private UasComponentIF component;

  private String         key;

  public SolrDeleteDocumentCommand(UasComponentIF component, String key)
  {
    this.component = component;
    this.key = key;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Deleting the document from solr with the component [" + this.component.getOid() + "] and key [" + this.key + "]");

    SolrService.deleteDocument(component, key);
  }

  /**
   * Executes the undo in this Command, and closes the connection.
   */
  public void undoIt()
  {
  }

  /**
   * Returns a human readable string describing what this command is trying to
   * do.
   * 
   * @return human readable string describing what this command is trying to do.
   */
  public String doItString()
  {
    return null;
  }

  public String undoItString()
  {
    return null;
  }

  /*
   * Indicates if this Command deletes something.
   * 
   * @return <code><b>true</b></code> if this Command deletes something.
   */
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void doFinally()
  {
  }

}
