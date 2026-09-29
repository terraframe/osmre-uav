package gov.geoplatform.uasdm.resource;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.resource.ApplicationFileResource;

public class ApplicationFileResourceDecorator extends ApplicationTreeResourceDecorator implements ApplicationFileResource
{

  public ApplicationFileResourceDecorator(ApplicationFileResource resource)
  {
    super(resource);
  }

  @Override
  protected ApplicationFileResource getResource()
  {
    return (ApplicationFileResource) super.getResource();
  }

  @Override
  public void forAllFileChildren(Consumer<ApplicationFileResource> action)
  {
    this.getResource().forAllFileChildren(action);
  }

  @Override
  public File getUnderlyingFile()
  {
    return this.getResource().getUnderlyingFile();
  }

  @Override
  public boolean isDirectory()
  {
    return this.getResource().isDirectory();
  }

  @Override
  public OIterator<ApplicationFileResource> getChildrenFiles()
  {
    return this.getResource().getChildrenFiles();
  }

  @Override
  public Optional<ApplicationFileResource> getChildFile(String path)
  {
    return this.getResource().getChildFile(path);
  }

  @Override
  public Optional<ApplicationFileResource> getParentFile()
  {
    return this.getResource().getParentFile();
  }

}
