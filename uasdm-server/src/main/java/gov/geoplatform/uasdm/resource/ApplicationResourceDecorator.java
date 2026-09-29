package gov.geoplatform.uasdm.resource;

import java.io.InputStream;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

public class ApplicationResourceDecorator implements ApplicationResource
{
  private ApplicationResource resource;

  public ApplicationResourceDecorator(ApplicationResource resource)
  {
    super();
    this.resource = resource;
  }

  protected ApplicationResource getResource()
  {
    return resource;
  }

  @Override
  public String getAbsolutePath()
  {
    return this.getResource().getAbsolutePath();
  }

  @Override
  public InputStream openNewStream()
  {
    return this.getResource().openNewStream();
  }

  @Override
  public CloseableFile openNewFile()
  {
    return this.getResource().openNewFile();
  }

  @Override
  public String getName()
  {
    return this.getResource().getName();
  }

  @Override
  public String getBaseName()
  {
    return this.getResource().getBaseName();
  }

  @Override
  public String getNameExtension()
  {
    return this.getResource().getNameExtension();
  }

  @Override
  public boolean isRemote()
  {
    return this.getResource().isRemote();
  }

  @Override
  public boolean exists()
  {
    return this.getResource().exists();
  }

  @Override
  public void close()
  {
    this.getResource().close();
  }

  @Override
  public void delete()
  {
    this.getResource().delete();
  }

}
