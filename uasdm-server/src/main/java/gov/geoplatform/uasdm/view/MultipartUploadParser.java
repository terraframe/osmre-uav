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
package gov.geoplatform.uasdm.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ParameterValue;

import jakarta.servlet.ServletContext;

public class MultipartUploadParser
{
  final Logger                log    = LoggerFactory.getLogger(MultipartUploadParser.class);

  private Map<String, String> params = new HashMap<String, String>();

  private List<FileItem>      files  = new ArrayList<FileItem>();

  public MultipartUploadParser(Map<String, ParameterValue> values, File repository, ServletContext context) throws Exception
  {
    if (!repository.exists() && !repository.mkdirs())
    {
      throw new IOException("Unable to mkdirs to " + repository.getAbsolutePath());
    }

    for (Entry<String, ParameterValue> entry : values.entrySet())
    {
      ParameterValue value = entry.getValue();

      if (value instanceof MultipartFileParameter)
      {
        this.files.add( ( (MultipartFileParameter) value ).getFileItem());
      }
      else
      {
        this.params.put(entry.getKey(), entry.getValue().getSingleValue());
      }
    }

    // List<FileItem> formFileItems = upload.parseRequest(values);
    //
    // parseFormFields(formFileItems);
    //
    if (files.isEmpty())
    {
      log.warn("No files were found when processing the requst. Debugging info follows.");

      // writeDebugInfo(values);

      throw new FileUploadException("No files were found when processing the requst.");
    }
    else
    {
      // if (log.isDebugEnabled())
      // {
      // writeDebugInfo(values);
      // }
    }
  }

  public Map<String, String> getParams()
  {
    return params;
  }

  public List<FileItem> getFiles()
  {
    if (files.isEmpty())
    {
      throw new RuntimeException("No FileItems exist.");
    }

    return files;
  }

  public FileItem getFirstFile()
  {
    if (files.isEmpty())
    {
      throw new RuntimeException("No FileItems exist.");
    }

    return files.iterator().next();
  }
}