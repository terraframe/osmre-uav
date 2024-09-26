package gov.geoplatform.uasdm.lidar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.odm.EmptyFileSetException;
import gov.geoplatform.uasdm.odm.ODMStatus;

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

  public void initiate(ApplicationResource pointcloud)
  {
    try
    {
      // TODO: HEADS UP - LIDAR PROCESSING

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
