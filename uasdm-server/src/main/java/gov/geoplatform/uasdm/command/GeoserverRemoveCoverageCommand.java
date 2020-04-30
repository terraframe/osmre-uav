package gov.geoplatform.uasdm.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import gov.geoplatform.uasdm.Util;

public class GeoserverRemoveCoverageCommand implements Command
{
  private Logger log = LoggerFactory.getLogger(RemoteFileDeleteCommand.class);

  private String workspace;

  private String storeName;

  public GeoserverRemoveCoverageCommand(String workspace, String storeName)
  {
    this.workspace = workspace;
    this.storeName = storeName;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Deleting coverage [" + this.workspace + "][" + this.storeName + "] from geoserver");

    Util.removeCoverageStore(workspace, storeName);
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
