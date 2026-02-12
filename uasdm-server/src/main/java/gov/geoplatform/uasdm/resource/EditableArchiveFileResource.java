/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.runwaysdk.query.ListOIterator;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.ApplicationTreeResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.FileResource;

/**
 * TODO : The next time Runway is updated, we can simply override the "extractAndListFiles" method and delete all these overridden helpers.
 * But at the moment, Runway doesn't have that method, so we have to override a bunch of stuff to get the same behaviour.
 */
public class EditableArchiveFileResource extends ArchiveFileResource
{
  
  protected List<File> excludedFiles = new ArrayList<File>();

  public EditableArchiveFileResource(ApplicationFileResource archive)
  {
    super(archive);
  }
  
  /**
   * Excludes the file from any further operations. The file is not actually removed from the underlying zip, but it will not be
   * available for iteration or other operations when using this abstraction.
   */
  public void exclude(ApplicationFileResource file) {
    excludedFiles.add(file.getUnderlyingFile());
  }
  
  /**
   * Excludes the file from any further operations. The file is not actually removed from the underlying zip, but it will not be
   * available for iteration or other operations when using this abstraction.
   */
  public void exclude(File file) {
    excludedFiles.add(file);
  }
  
  @Override
  public OIterator<ApplicationFileResource> getChildrenFiles()
  {
    ArrayList<ApplicationFileResource> result = new ArrayList<ApplicationFileResource>();
    
    for (File file : extractAndListFiles())
    {
      result.add(new FileResource(file));
    }
    
    return new ListOIterator<ApplicationFileResource>(result);
  }

  @Override
  public Optional<ApplicationFileResource> getChildFile(String path)
  {
    for (File file : extractAndListFiles())
    {
      if (file.getName().equals(path))
        return Optional.of(new FileResource(file));
    }
    
    return Optional.empty();
  }
  
  @Override
  public boolean hasChildren()
  {
    return extractAndListFiles().length > 0;
  }
  
  @Override
  public Collection<ApplicationResource> getContents()
  {
    ArrayList<ApplicationResource> result = new ArrayList<ApplicationResource>();
    
    for (File file : extractAndListFiles())
    {
      result.add(new FileResource(file));
    }
    
    return result;
  }
  
  public List<ApplicationTreeResource> getTreeContents()
  {
    List<ApplicationTreeResource> result = new ArrayList<ApplicationTreeResource>();
    
    for (File file : extractAndListFiles())
    {
      result.add(new FileResource(file));
    }
    
    return result;
  }
  
  @Override
  public Optional<ApplicationTreeResource> getChild(String path)
  {
    for (File file : extractAndListFiles())
    {
      if (file.getName().equals(path))
        return Optional.of(new FileResource(file));
    }
    
    return Optional.empty();
  }
  
  protected File[] extractAndListFiles()
  {
    File extracted = extract();
    
    return Arrays.stream(extracted.listFiles()).filter(f -> !excludedFiles.contains(f)).toArray(File[]::new);
  }

}
