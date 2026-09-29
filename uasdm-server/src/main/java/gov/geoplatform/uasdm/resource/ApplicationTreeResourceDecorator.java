package gov.geoplatform.uasdm.resource;

import java.util.Optional;
import java.util.function.Consumer;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.resource.ApplicationTreeResource;

public class ApplicationTreeResourceDecorator extends ApplicationResourceDecorator implements ApplicationTreeResource
{

  public ApplicationTreeResourceDecorator(ApplicationTreeResource resource)
  {
    super(resource);
  }

  @Override
  protected ApplicationTreeResource getResource()
  {
    return (ApplicationTreeResource) super.getResource();
  }

  @Override
  public void forAllChildren(Consumer<ApplicationTreeResource> action)
  {
    this.getResource().forAllChildren(action);
  }

  @Override
  public OIterator<ApplicationTreeResource> getChildren()
  {
    return this.getResource().getChildren();
  }

  @Override
  public boolean hasChildren()
  {
    return this.getResource().hasChildren();
  }

  @Override
  public Optional<ApplicationTreeResource> getChild(String path)
  {
    return this.getResource().getChild(path);
  }

  @Override
  public Optional<ApplicationTreeResource> getParent()
  {
    return this.getResource().getParent();
  }

}
