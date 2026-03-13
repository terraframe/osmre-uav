package gov.geoplatform.uasdm.processing;

import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.EmptyFileSetException;

abstract public class FargateRunConfiguration {
  int cpu;
  int memory;
  int maxDiskGb;
  
  FargateRunConfiguration(int cpu, int memory, int maxDiskGb) {
    this.cpu = cpu;
    this.memory = memory;
    this.maxDiskGb = maxDiskGb;
  }

  abstract public String getArn();

  public int getMaxDiskGb()
  {
    return maxDiskGb;
  }
  
  public int getCpu()
  {
    return cpu;
  }

  public int getMemory()
  {
    return memory;
  }
  
  public static FargateRunConfiguration configForTask(FargateProcessingTask task)
  {
    var config = task.getConfiguration();
    
    if (config.isLidar())
    {
      return LidarFargateRunConfiguration.build(task);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }
  
  public static DocumentIF selectLazForProcessing(FargateTaskIF task, UasComponentIF component) {
    var documents = FargateProcessingTask.getRaw(component).stream()
        .filter(d -> FilenameUtils.getExtension(d.getName()).equalsIgnoreCase("laz"))
        .collect(Collectors.toList());
    
    if (documents.size() > 1) {
      throw new RuntimeException("Expected a single laz file, but there were " + documents.size());
    } else if (documents.size() == 0) {
      throw new EmptyFileSetException();
    }
    
    return documents.get(0);
  }

  public static class LidarFargateRunConfiguration extends FargateRunConfiguration {
    public static final String LIDAR_TASK_ARN = AppProperties.getFargateLidarTaskDefinitionArn();
    
    public LidarFargateRunConfiguration(int cpu, int memory, int maxDiskGb)
    {
      super(cpu, memory, maxDiskGb);
    }

    public String getArn() {
      return LIDAR_TASK_ARN;
    }
    
    public static FargateRunConfiguration build(FargateProcessingTask task) {
      long totalBytes = 0L;
      
      if (task.getConfiguration().isLidar()) {
        var laz = selectLazForProcessing(task, task.getComponentInstance());
    
        totalBytes = laz.getFileSize();
      } else {
        // TODO (dead code) : Example code here in case you needed it for a different run config
        for (var doc : FargateProcessingTask.getRaw(task.getComponentInstance())) {
            totalBytes += doc.getFileSize();
        }
      }

      int sizeGiB = (int) Math.min(totalBytes / (1024L * 1024L * 1024L), Integer.MAX_VALUE);
      
      var bucket = FargateSizingBucket.selectFor(sizeGiB);
      
      return new LidarFargateRunConfiguration(bucket.getCpu(), bucket.getMemory(), bucket.getMaxDiskGb());
    }
  }
  
  public static enum FargateSizingBucket {
    SMALL(2048, 4096, 100), // t3a.medium
    MEDIUM(2048, 8192, 160), // m5.large
    LARGE(4096, 16384, 320), // m5.xlarge
    XLARGE(8192, 32768, 640), // m5.2xlarge
    LARGE2X(8192, 61440, 800), // r5.2xlarge
    LARGE3X(16384, 122880, 1000); // r5.4xlarge (Fargate MAX)
    
    int cpu;
    int memory;
    int maxDiskGb;
    
    private FargateSizingBucket(int cpu, int memory, int maxDiskGb)
    {
      this.cpu = cpu;
      this.memory = memory;
      this.maxDiskGb = maxDiskGb;
    }
    
    public int getCpu()
    {
      return cpu;
    }

    public int getMemory()
    {
      return memory;
    }

    public int getMaxDiskGb()
    {
      return maxDiskGb;
    }

    public static FargateSizingBucket selectFor(int jobSizeGb) {
      var values = values();
      
      for (var def : values) {
        if (jobSizeGb < def.getMaxDiskGb())
          return def;
      }
      
      return LARGE3X;
    }
  }
}
