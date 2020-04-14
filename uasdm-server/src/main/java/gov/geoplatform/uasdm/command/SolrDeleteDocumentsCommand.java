package gov.geoplatform.uasdm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.service.SolrService;

public class SolrDeleteDocumentsCommand implements Command
{
  private Logger log = LoggerFactory.getLogger(SolrDeleteDocumentsCommand.class);

  private String fieldId;

  private String oid;

  public SolrDeleteDocumentsCommand(String fieldId, String oid)
  {
    this.fieldId = fieldId;
    this.oid = oid;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Deleting all documents from solr wher the field [" + this.fieldId + "] equals [" + this.oid + "]");

    SolrService.deleteDocuments(this.fieldId, this.oid);
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
