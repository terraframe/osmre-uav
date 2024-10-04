package gov.geoplatform.uasdm.lidar;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.EmptyFileSetException;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.COPCConverterProcessor;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.SilvimetricProcessor;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.processing.WorkflowTaskMonitor;
import net.lingala.zip4j.ZipFile;

public class LidarProcessingTask extends LidarProcessingTaskBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -807737829;

  private static Logger     logger           = LoggerFactory.getLogger(LidarProcessingTask.class);

  public LidarProcessingTask()
  {
    super();
  }

  public LidarProcessConfiguration getConfiguration()
  {
    String json = this.getConfigurationJson();

    if (!StringUtils.isEmpty(json))
    {
      return LidarProcessConfiguration.parse(json);
    }

    return new LidarProcessConfiguration();
  }

  public void setConfiguration(LidarProcessConfiguration configuration)
  {
    this.setConfigurationJson(configuration.toJson().toString());
  }

  public void initiate(ApplicationFileResource pointcloud)
  {
    try
    {
      final File tempDir = Files.createTempDirectory(this.getProductId()).toFile();
      final LidarProcessConfiguration config = getConfiguration();
      final UasComponentIF component = getComponentInstance();
      final StatusMonitorIF monitor = new WorkflowTaskMonitor(this);
      
      if (config.isGenerateCopc()) {
        // Unzip source files and find a laz
        try (ZipFile zip = new ZipFile(pointcloud.getUnderlyingFile())) { zip.extractAll(tempDir.getAbsolutePath()); }
        List<File> lazList = Arrays.asList(tempDir.listFiles()).stream().filter(f -> FilenameUtils.getExtension(f.getName()).toLowerCase().contains("laz")).collect(Collectors.toList());
        if (lazList.size() == 0 || lazList.size() > 1) {
          throw new RuntimeException("Expected a single laz file, but there were " + lazList.size());
        }
        File laz = lazList.get(0);
        
        COPCConverterProcessor processor = new COPCConverterProcessor(config, component, monitor);
          
        if (config.isGenerateGSM() || config.isGenerateTerrainModel() || config.isGenerateTreeCanopyCover() || config.isGenerateTreeStructure()) {
          processor.addDownstream(new SilvimetricProcessor(config, component, monitor)
              .addDownstream(new CogTifProcessor(null, null, component, monitor)));
        }
        
        processor.process(new FileResource(laz));
      } else if (config.isGenerateGSM() || config.isGenerateTerrainModel() || config.isGenerateTreeCanopyCover() || config.isGenerateTreeStructure()) {
        // If we're not generating a copc, then one must exist already. Find it because we'll need it for processing.
        List<ProductIF> copcs = component.getProducts().stream().filter(p -> p.getProductName().equals(config.getProductName())).collect(Collectors.toList());
        
        if (copcs.size() == 0) {
          throw new EmptyFileSetException();
        }
        
        Optional<DocumentIF> op = copcs.get(0).getDocuments().stream().filter(d -> d.getName().endsWith(".copc.laz")).findFirst();
        
        if (op.isEmpty()) {
          throw new EmptyFileSetException();
        }
        
        try (CloseableFile copc = op.get().download().openNewFile()) {
          new SilvimetricProcessor(config, component, monitor)
              .addDownstream(new CogTifProcessor(null, null, component, monitor))
              .process(new FileResource(copc));
        }
      }

      // Designate a primary product if it makes sense
      if (component.getPrimaryProduct().isEmpty()) {
        for (ProductIF product : component.getProducts()) {
          if (!product.getProductName().equals(config.getProductName()) && product.getProductName().contains(config.getProductName())) {
            ((Product)product).setPrimary(true);
            ((Product)product).apply();
            break;
          }
        }
      }

      this.appLock();
      if (monitor.getErrors().size() > 0) {
        this.setStatus(ODMStatus.FAILED.getLabel());
        this.setMessage("Lidar processing encountered errors. View messages for more information.");
      } else {
        this.setStatus(ODMStatus.COMPLETED.getLabel());
        this.setMessage("Lidar processing is complete");
      }
      this.apply();
    }
    catch (EmptyFileSetException e)
    {
      this.appLock();
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("No point cloud files to be processed.");
      this.apply();
    }
    catch (Throwable t)
    {
      logger.error("Error occurred while initiating Lidar Processing.", t);

      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage(RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
      this.apply();
    }
    finally
    {
      pointcloud.close();
    }
  }

}
