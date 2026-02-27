package gov.geoplatform.uasdm.processing;

/**
 * A task definition as defined in Fargate.
 */
public enum FargateTaskDefinition {
  SMALL("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-small", 100),
  MEDIUM("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-medium", 160),
  LARGE("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-large", 320),
  XLARGE("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-xlarge", 640),
  LARGE2X("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-2xlarge", 800),
  LARGE3X("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-3xlarge", 1000),
  LARGE4X("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-4xlarge", 1200);
  
  String arn;
  int maxDiskGb;
  
  FargateTaskDefinition(String arn, int maxDiskGb) {
    this.arn = arn;
    this.maxDiskGb = maxDiskGb;
  }

  public String getArn()
  {
    return arn;
  }

  public int getMaxDiskGb()
  {
    return maxDiskGb;
  }
  
  public static FargateTaskDefinition select(int jobSizeGb)
  {
    var values = values();
    
    for (var def : values) {
      if (jobSizeGb < def.maxDiskGb)
        return def;
    }
    
    return values[values.length - 1];
  }
}
