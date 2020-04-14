package gov.geoplatform.uasdm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class RemoteFileDeleteCommand implements Command
{
  private Logger log = LoggerFactory.getLogger(RemoteFileDeleteCommand.class);

  private String key;

  public RemoteFileDeleteCommand(String key)
  {
    this.key = key;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Deleting key [" + this.key + "] from S3");

    RemoteFileFacade.deleteObjects(key);
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
    return "Deleting from S3 with the following key [" + key + "]";
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
