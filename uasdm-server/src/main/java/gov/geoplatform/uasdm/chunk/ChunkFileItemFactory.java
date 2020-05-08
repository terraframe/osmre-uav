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
package gov.geoplatform.uasdm.chunk;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;

import com.runwaysdk.controller.RequestManager;
import com.runwaysdk.mvc.ServletFileUploadFactory;

import gov.geoplatform.uasdm.AppProperties;

public class ChunkFileItemFactory implements ServletFileUploadFactory
{
  // fileItemsFactory is a field (even though it's scoped to the constructor) to
  // prevent the
  // org.apache.commons.fileupload.servlet.FileCleanerCleanup thread from
  // attempting to delete the
  // temp file before while it is still being used.
  //
  // FileCleanerCleanup uses a java.lang.ref.ReferenceQueue to delete the temp
  // file when the FileItemsFactory marker object is GCed

  @Override
  public ServletFileUpload instance(RequestManager manager)
  {
    ServletContext context = manager.getReq().getServletContext();

    DiskFileItemFactory fileItemsFactory = setupFileItemFactory(AppProperties.getTempDirectory(), context);

    ServletFileUpload upload = new ServletFileUpload(fileItemsFactory);

    return upload;
  }

  private DiskFileItemFactory setupFileItemFactory(File repository, ServletContext context)
  {
    if (!repository.exists() && !repository.mkdirs())
    {
      throw new RuntimeException("Unable to mkdirs to " + repository.getAbsolutePath());
    }

    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
    factory.setRepository(repository);

    FileCleaningTracker pTracker = FileCleanerCleanup.getFileCleaningTracker(context);
    factory.setFileCleaningTracker(pTracker);

    return factory;
  }

}
