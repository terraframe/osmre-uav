package gov.geoplatform.uasdm.processing.report;

import com.runwaysdk.dataaccess.Command;

public class CollectionReportTaskCommand implements Command
{
  private CollectionReportTask task;

  public CollectionReportTaskCommand(CollectionReportTask task)
  {
    this.task = task;
  }

  @Override
  public void doIt()
  {
    CollectionReportFacade.process(this.task);
  }

  @Override
  public void undoIt()
  {
    // Unable to undo it
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
