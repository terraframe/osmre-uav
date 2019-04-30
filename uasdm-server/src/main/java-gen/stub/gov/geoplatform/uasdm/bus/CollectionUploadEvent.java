package gov.geoplatform.uasdm.bus;

import java.io.File;

import gov.geoplatform.uasdm.odm.ODMProcessingTask;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.view.RequestParser;
import net.geoprism.GeoprismUser;

public class CollectionUploadEvent extends CollectionUploadEventBase
{
  private static final long serialVersionUID = -285847093;
  
  public CollectionUploadEvent()
  {
    super();
  }

  public void handleUploadFinish(RequestParser parser, File infile)
  {
    WorkflowTask task = WorkflowTask.getTaskByUploadId(parser.getUuid());
    
    task.lock();
    task.setStatus("Processing");
    task.setMessage("Processing archived files");
    task.apply();

    Collection collection = task.getCollection();
    collection.uploadArchive(task, infile);

    task.lock();
    task.setStatus("Complete");
    task.setMessage("The upload successfully completed.  All files except those mentioned were archived.");
    task.apply();
    
    startODMProcessing(infile, task);
    
//    handleMetadataWorkflow(task);
  }
  
  private void startODMProcessing(File infile, WorkflowTask uploadTask)
  {
    ODMProcessingTask task = new ODMProcessingTask();
    task.setUpLoadId(uploadTask.getUpLoadId());
    task.setCollectionId(uploadTask.getCollectionOid());
    task.setGeoprismUser((GeoprismUser) GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("Orthorectification Processing (ODM) [" + task.getCollection().getName() + "]");
    task.setMessage("Your images are submitted for processing. Check back later for updates.");
    task.apply();
    
    task.initiate(infile);
  }
  
//  private void handleMetadataWorkflow(WorkflowTask uploadTask)
//  {
//    if (this.getCollection().getMetadataUploaded())
//    {
//      WorkflowTask task = new WorkflowTask();
//      task.setUpLoadId(uploadTask.getUpLoadId());
//      task.setCollectionId(uploadTask.getCollectionOid());
//      task.setGeoprismUser(uploadTask.getGeoprismUser());
//      task.setWorkflowType(WorkflowTask.NEEDS_METADATA);
//      task.setStatus("Message");
//      task.setTaskLabel("Missing Metadata");
//      task.setMessage("Metadata is missing for Collection [" + uploadTask.getCollection().getName() + "].");
//      task.apply();
//    }
//  }
  
}
