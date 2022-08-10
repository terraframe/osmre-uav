package gov.geoplatform.uasdm.mock;

import java.net.URISyntaxException;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.odm.InfoResponse;
import gov.geoplatform.uasdm.odm.NewResponse;
import gov.geoplatform.uasdm.odm.ODMResponse;
import gov.geoplatform.uasdm.odm.ODMServiceIF;
import gov.geoplatform.uasdm.odm.TaskOutputResponse;
import gov.geoplatform.uasdm.odm.TaskRemoveResponse;

public class MockODMService implements ODMServiceIF
{

  @Override
  public TaskOutputResponse taskOutput(String uuid)
  {
    return new MockTaskOutputResponse();
  }

  @Override
  public TaskRemoveResponse taskRemove(String uuid)
  {
    return new MockTaskRemoveResponse();
  }

  @Override
  public CloseableFile taskDownload(String uuid)
  {
    try
    {
      return new CloseableFile(this.getClass().getResource("/all.zip.test").toURI(), false);
    }
    catch (URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public NewResponse taskNew(ApplicationResource images, boolean isMultispectral)
  {
    return new MockNewResponse();
  }

  @Override
  public NewResponse taskNewInit(int imagesCount, boolean isMultispectral)
  {
    return new MockNewResponse();
  }

  @Override
  public ODMResponse taskNewUpload(String uuid, ApplicationResource image)
  {
    return new MockODMResponse();
  }

  @Override
  public ODMResponse taskNewCommit(String uuid)
  {
    return new MockODMResponse();
  }

  @Override
  public InfoResponse taskInfo(String uuid)
  {
    return new MockInfoResponse();
  }

}
