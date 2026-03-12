package gov.geoplatform.uasdm.processing;

import java.util.LinkedList;
import java.util.List;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.ODMTaskProcessor.TaskResult;
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
    final ProcessConfiguration config = task.getConfiguration();
    final Product product = (Product) component.createProductIfNotExist(config.getProductName());
    
    List<SiteObject> items = RemoteFileFacade.getSiteObjects(component, FargateProcessingTask.JOBS + "/" + task.getProcessingJobId(), new LinkedList<SiteObject>(), null, null).getObjects();
    
    handleArtifact(".*\\.copc\\.laz", ImageryComponent.PTCLOUD + "/pointcloud.copc.laz", (config.toLidar().isGenerateCopc() && !alreadyHasCopc()), items, product, component, monitor);
    handleArtifact("m_Classification_veg_density.cog.tif", ImageryComponent.ORTHO + "/m_Classification_veg_density.cog.tif", config.toLidar().isGenerateTreeCanopyCover(), items, product, component, monitor);
    handleArtifact("m_Z_diff.cog.tif", ImageryComponent.ORTHO + "/m_Z_diff.cog.tif", config.toLidar().isGenerateTreeStructure(), items, product, component, monitor);
    handleArtifact("m_Z_max.cog.tif", ImageryComponent.ORTHO + "/m_Z_max.cog.tif", config.toLidar().isGenerateGSM(), items, product, component, monitor);
    handleArtifact("m_Z_min.cog.tif", ImageryComponent.ORTHO + "/m_Z_min.cog.tif", config.toLidar().isGenerateTerrainModel(), items, product, component, monitor);
    
    // Designate a primary product if it makes sense
    designatePrimaryProduct(component, config);
  }
  
  protected boolean alreadyHasCopc() {
    DocumentIF laz = FargateRunConfiguration.selectLazForProcessing(task, task.getComponentInstance());
    
    // This works so long as we never allow them to store a non-copc format with this extension.
    return laz.getName().endsWith(".copc.laz");
  }

}
