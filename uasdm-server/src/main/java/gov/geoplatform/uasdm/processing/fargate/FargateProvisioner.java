package gov.geoplatform.uasdm.processing.fargate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.runwaysdk.RunwayException;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.FargateProcessingTask;
import gov.geoplatform.uasdm.service.ExcludeSiteObjectPredicate;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteObject;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import net.geoprism.GeoprismUser;

@Component
public class FargateProvisioner
{
  final Logger logger = LoggerFactory.getLogger(ProjectManagementService.class);
  
  @Autowired
  private ProjectManagementService projectManagmentService;
  
  private class FargateAutoscalerThread extends Thread
  {
    private FargateProcessingTask task;

    private CollectionIF      collection;

    private Set<String>       excludes;

    public FargateAutoscalerThread(FargateProcessingTask task, CollectionIF collection, Set<String> excludes)
    {
      super("Fargate autoscaler thread for collection [" + collection.getName() + "]");

      this.task = task;
      this.collection = collection;
      this.excludes = excludes;
    }

    @Override
    @Request
    public void run()
    {
      try
      {
        List<String> filenames = new LinkedList<String>();

        JSONArray array = new JSONArray();

        for (String filename : filenames)
        {
          array.put(filename);
        }

        task.appLock();
        task.setProcessFilenameArray(array.toString());
        task.apply();

        task.initiate(excludes);

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));
      }
      catch (Throwable t)
      {
        logger.error("Exception while re-running ortho", t);

        task.appLock();
        task.setStatus(ODMStatus.FAILED.getLabel());
        task.setMessage(RunwayException.localizeThrowable(t, CommonProperties.getDefaultLocale()));
        task.apply();

        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

        throw t;
      }
    }
  }
  
  public void provision(String id, CollectionIF collection, ProcessConfiguration configuration)
  {
    FargateProcessingTask task = new FargateProcessingTask();
    task.setUploadId(id);
    task.setComponent(collection.getOid());
    task.setGeoprismUser(GeoprismUser.getCurrentUser());
    task.setStatus(ODMStatus.RUNNING.getLabel());
    task.setTaskLabel("Lidar processing for collection [" + collection.getName() + "]");
    task.setMessage("The point clouds uploaded to ['" + collection.getName() + "'] are submitted for processing. Check back later for updates.");
    task.setConfiguration(configuration.toLidar());
    task.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.JOB_CHANGE, null));

    FargateAutoscalerThread t = new FargateAutoscalerThread(task, collection, collection.getExcludes());
    t.setDaemon(true);
    t.start();
  }
}
