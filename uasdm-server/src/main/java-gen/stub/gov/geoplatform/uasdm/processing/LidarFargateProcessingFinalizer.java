package gov.geoplatform.uasdm.processing;

import java.util.LinkedList;
import java.util.List;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.lidar.LidarProcessConfiguration;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
import gov.geoplatform.uasdm.processing.SilvimetricProcessor.Metric;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.view.SiteObject;

public class LidarFargateProcessingFinalizer extends FargateProcessingFinalizer
{

  public LidarFargateProcessingFinalizer(FargateTaskIF task, TaskResult result)
  {
    super(task, result);
  }
  
  @Override
  protected void handleArtifacts() {
    final StatusMonitorIF monitor = new WorkflowTaskMonitor((AbstractWorkflowTask) task);
    final UasComponentIF component = task.getComponentInstance();
    final LidarProcessConfiguration config = task.getConfiguration().toLidar();
    
    List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, FargateProcessingTask.JOBS + "/" + task.getProcessingJobId(), new LinkedList<SiteObject>(), null, null).getObjects();
    
    if (config.toLidar().isGenerateCopc() && !alreadyHasCopc()) {
      final Product product = (Product) component.createProductIfNotExist(config.getProductName());
      potentialOutput(".*\\.copc\\.laz", ImageryComponent.PTCLOUD + "/pointcloud.copc.laz", ManagedDocumentTool.GDAL, true, items, product, component, monitor);
    }
    
    for (var metric : Metric.values())
      potentialMetricOutput(metric, config, items, component, monitor);
    
//    potentialOutput("m_Classification_veg_density.cog.tif", ImageryComponent.ORTHO + "/m_Classification_veg_density.cog.tif", config.toLidar().isGenerateTreeCanopyCover(), items, null, component, monitor);
//    potentialOutput("m_Z_diff.cog.tif", ImageryComponent.ORTHO + "/m_Z_diff.cog.tif", config.toLidar().isGenerateTreeStructure(), items, null, component, monitor);
//    potentialOutput("m_Z_max.cog.tif", ImageryComponent.ORTHO + "/m_Z_max.cog.tif", config.toLidar().isGenerateGSM(), items, null, component, monitor);
//    potentialOutput("m_Z_min.cog.tif", ImageryComponent.ORTHO + "/m_Z_min.cog.tif", config.toLidar().isGenerateTerrainModel(), items, null, component, monitor);
    
    // Designate a primary product if it makes sense
    designatePrimaryProduct(component, config);
  }
  
  protected boolean alreadyHasCopc() {
    DocumentIF laz = FargateRunConfiguration.selectLazForProcessing(task, task.getComponentInstance());
    
    // This works so long as we never allow them to store a non-copc format with this extension.
    return laz.getName().endsWith(".copc.laz");
  }
  
  protected void potentialMetricOutput(Metric metric, LidarProcessConfiguration config, List<SiteObject> items, UasComponentIF component, StatusMonitorIF monitor) {
    if (!metric.shouldGenerate(config))
      return;
    
    var cogTif = items.stream().filter(i -> i.getName().toLowerCase().startsWith(metric.getSilvimetricName()) && i.getName().toLowerCase().endsWith(".cog.tif")).findFirst().orElse(null);
    var tif = items.stream().filter(i -> i.getName().toLowerCase().startsWith(metric.getSilvimetricName()) && !i.getName().toLowerCase().endsWith(".cog.tif") && i.getName().toLowerCase().endsWith(".tif")).findFirst().orElse(null);
    
    if (cogTif == null || tif == null)
    {
      if (cogTif == null) task.createAction("Processing job did not generated expected file [" + metric.getName() + ".cog.tif]", TaskActionType.ERROR);
      if (tif == null) task.createAction("Processing job did not generated expected file [" + metric.getName() + ".tif]", TaskActionType.ERROR);
      missingRequiredArtifact = true;
    }
    else
    {
      Product product = (Product) component.createProductIfNotExist(config.getProductName() + "_" + metric.getName());
      handleGeneratedArtifact(ImageryComponent.ORTHO + "/" + metric.getName() + ".tif", ManagedDocumentTool.SILVIMETRIC, tif, product, component, monitor);
      handleGeneratedArtifact(ImageryComponent.ORTHO + "/" + metric.getName() + ".cog.tif", ManagedDocumentTool.GDAL, cogTif, product, component, monitor);
    }
  }

}
