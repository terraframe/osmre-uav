package gov.geoplatform.uasdm.resource;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.resource.ApplicationCollectionResource;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.ResourceException;

public class ArchiveFileResourceDecorator extends ApplicationTreeResourceDecorator implements ApplicationCollectionResource, ApplicationFileResource
{
  private CloseableFile extractedParent;

  public ArchiveFileResourceDecorator(ArchiveFileResource resource)
  {
    super(resource);
  }

  @Override
  protected ArchiveFileResource getResource()
  {
    return (ArchiveFileResource) super.getResource();
  }

  public CloseableFile extract() throws ResourceException
  {
    this.extractedParent = this.getResource().extract();

    return extractedParent;
  }

  public CloseableFile getExtractedParent()
  {
    return extractedParent;
  }

  @Override
  public Collection<ApplicationResource> getContents()
  {
    return this.getResource().getContents();
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
