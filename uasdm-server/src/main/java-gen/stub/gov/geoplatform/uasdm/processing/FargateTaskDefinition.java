package gov.geoplatform.uasdm.processing;

/**
 * This is not a task as defined in IDM. This is a task definition as defined in Fargate.
 */
public enum FargateTaskDefinition {
  SMALL("arn:aws:ecs:us-east-1:813324710591:task-definition/uasdm-processing-small:1", 1, 3_072);
  
  String arn; int vcpu; int mib;
  
  FargateTaskDefinition(String arn, int vcpu, int mib) {
    this.arn = arn;
    this.vcpu = vcpu;
    this.mib = mib;
  }

  public String getArn()
  {
    return arn;
  }

  public int getVcpu()
  {
    return vcpu;
  }

  public int getMib()
  {
    return mib;
  }
}
