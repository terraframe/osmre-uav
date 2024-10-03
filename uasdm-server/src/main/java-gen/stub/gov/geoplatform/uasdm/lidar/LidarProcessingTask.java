package gov.geoplatform.uasdm.lidar;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.graph.Product;
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
      File tempDir = Files.createTempDirectory(this.getProductId()).toFile();
      new ZipFile(pointcloud.getUnderlyingFile()).extractAll(tempDir.getAbsolutePath());
      
      List<File> lazList = Arrays.asList(tempDir.listFiles()).stream().filter(f -> FilenameUtils.getExtension(f.getName()).toLowerCase().contains("laz")).collect(Collectors.toList());
      if (lazList.size() == 0 || lazList.size() > 1) {
        throw new RuntimeException("Expected a single laz file, but there were " + lazList.size());
      }
      File laz = lazList.get(0);
      
      UasComponentIF component = getComponentInstance();
      StatusMonitorIF monitor = new WorkflowTaskMonitor(this);
      LidarProcessConfiguration config = getConfiguration();
      
      new COPCConverterProcessor(config, component, monitor)
        .addDownstream(new SilvimetricProcessor(config, component, monitor)
            .addDownstream(new CogTifProcessor(null, null, component, monitor)))
        .process(new FileResource(laz));
      
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
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("Lidar processing is complete");
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
