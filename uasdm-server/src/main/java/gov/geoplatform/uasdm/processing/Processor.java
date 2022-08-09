package gov.geoplatform.uasdm.processing;

import com.runwaysdk.resource.ApplicationFileResource;

public interface Processor
{

  public boolean process(ApplicationFileResource res);

}