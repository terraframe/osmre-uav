/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.odm;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.bus.OrthoProcessingTask;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class GdalProcessor
{
  private static final Logger logger = LoggerFactory.getLogger(GdalProcessor.class);

  protected List<DocumentIF> documents = new LinkedList<DocumentIF>();

  protected OrthoProcessingTask uploadTask;

  protected CollectionIF collection;

  protected ProductIF product;

  protected File source;

  public GdalProcessor(CollectionIF collection, OrthoProcessingTask uploadTask, ProductIF product, File source)
  {
    this.collection = collection;
    this.uploadTask = uploadTask;
    this.product = product;
    this.source = source;
  }

  public void process() throws InterruptedException
  {
    // Generate the file name
    List<S3FileUpload> processors = new LinkedList<GdalProcessor.S3FileUpload>();

    if (this.uploadTask.getUploadTarget().equals(ImageryComponent.ORTHO) && this.uploadTask.getProcessOrtho())
    {
      processors.add(new GdalTransformProcessor(ImageryComponent.ORTHO, new String[] {
          FilenameUtils.getBaseName(source.getName()) + ".png"
      }));
    }

    if (this.uploadTask.getUploadTarget().equals(ImageryComponent.DEM)  && this.uploadTask.getProcessDem())
    {
      processors.add(new GdalDemProcessor(ODMZipPostProcessor.DEM_GDAL, new String[] {
          "dsm.tif"
      }));
    }

    for (S3FileUpload processor : processors)
    {
      processor.processFile(this.source);

      processor.handleUnprocessedFiles();
    }

    product.addDocuments(this.getDocuments());
  }

  public List<DocumentIF> getDocuments()
  {
    return documents;
  }

  private class S3FileUpload
  {
    private String s3FolderName;

    private String[] mandatoryFiles;

    private ArrayList<String> processedFiles;

    public S3FileUpload(String s3FolderName, String[] mandatoryFiles)
    {
      this.s3FolderName = s3FolderName;
      this.mandatoryFiles = mandatoryFiles;
      this.processedFiles = new ArrayList<String>();
    }

    protected void handleUnprocessedFiles()
    {
      List<String> unprocessed = this.getUnprocessedFiles();

      if (unprocessed.size() > 0)
      {
        for (String name : unprocessed)
        {
          this.handleUnprocessedFile(name);
        }
      }
    }

    protected void handleUnprocessedFile(String name)
    {

    }

    protected List<String> getUnprocessedFiles()
    {
      ArrayList<String> unprocessed = new ArrayList<String>();

      if (mandatoryFiles == null)
      {
        return unprocessed;
      }

      for (String file : mandatoryFiles)
      {
        if (!processedFiles.contains(file))
        {
          unprocessed.add(file);
        }
      }

      return unprocessed;
    }

    public String getS3FolderName()
    {
      return s3FolderName;
    }

    protected void processFile(File file)
    {
      String key = GdalProcessor.this.collection.getS3location() + this.getS3FolderName() + "/" + file.getName();

      if (file.isDirectory())
      {
        RemoteFileFacade.uploadDirectory(file, key, GdalProcessor.this.uploadTask, true);
      }
      else
      {
        RemoteFileFacade.uploadFile(file, key, GdalProcessor.this.uploadTask);
      }

      if (GdalProcessor.this.collection instanceof CollectionIF)
      {
        CollectionReport.updateSize((CollectionIF) GdalProcessor.this.collection);
      }
    }
  }

  private class ManagedDocument extends S3FileUpload
  {
    private boolean searchable;

    public ManagedDocument(String s3FolderName, String[] mandatoryFiles, boolean searchable)
    {
      super(s3FolderName, mandatoryFiles);

      this.searchable = searchable;
    }

    @Override
    protected void handleUnprocessedFile(String name)
    {
      if (GdalProcessor.this.uploadTask != null)
      {
        GdalProcessor.this.uploadTask.createAction("GDAL Transform did not produce an expected file [" + this.getS3FolderName() + "/" + name + "].", "error");
      }
    }

    @Override
    protected void processFile(File file)
    {
      super.processFile(file);

      if (!file.isDirectory())
      {
        String key = GdalProcessor.this.collection.getS3location() + this.getS3FolderName() + "/" + file.getName();

        GdalProcessor.this.documents.add(GdalProcessor.this.collection.createDocumentIfNotExist(key, file.getName(), null, null));

        if (searchable)
        {
          IndexService.updateOrCreateDocument(GdalProcessor.this.collection.getAncestors(), GdalProcessor.this.collection, key, file.getName());
        }
      }
    }
  }

  private abstract class SystemProcessProcessor extends ManagedDocument
  {
    public SystemProcessProcessor(String s3FolderName, String[] mandatoryFiles, boolean searchable)
    {
      super(s3FolderName, mandatoryFiles, searchable);
    }

    protected void executeProcess(String[] commands)
    {
      final Runtime rt = Runtime.getRuntime();

      StringBuilder stdOut = new StringBuilder();
      StringBuilder stdErr = new StringBuilder();

      Thread t = new Thread()
      {
        public void run()
        {
          try
          {
            Process proc = rt.exec(commands);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // read the output from the command
            String s = null;
            while ( ( s = stdInput.readLine() ) != null)
            {
              stdOut.append(s + "\n");
            }

            // read any errors from the attempted command
            while ( ( s = stdError.readLine() ) != null)
            {
              stdErr.append(s + "\n");
            }
          }
          catch (Throwable t)
          {
            logger.error("Error occured while processing dem file with gdal.", t);
          }
        }
      };
      t.start();

      try
      {
        t.join(10000);
      }
      catch (InterruptedException e)
      {
        logger.error("Interrupted when processing dem file with gdal", e);
      }

      if (stdOut.toString().trim().length() > 0)
      {
        logger.info("Processed transform with gdal [" + stdOut.toString() + "].");
      }

      if (stdErr.toString().trim().length() > 0)
      {
        logger.error("Unexpected error while processing gdal transform [" + stdErr.toString() + "].");
      }
    }

  }

  private class GdalTransformProcessor extends SystemProcessProcessor
  {
    public GdalTransformProcessor(String s3FolderName, String[] mandatoryFiles)
    {
      super(s3FolderName, mandatoryFiles, false);
    }

    @Override
    protected void processFile(File file)
    {
      final String basename = FilenameUtils.getBaseName(file.getName());

      File png = new File(file.getParent(), basename + ".png");

      // gdal_translate -of PNG odm_orthophoto.tif test.png

      this.executeProcess(new String[] {
          "gdal_translate", "-of", "PNG", file.getAbsolutePath(), png.getAbsolutePath()
      });

      if (png.exists())
      {
        super.processFile(png);
      }
    }

  }

  private class GdalDemProcessor extends SystemProcessProcessor
  {
    public GdalDemProcessor(String s3FolderName, String[] mandatoryFiles)
    {
      super(s3FolderName, mandatoryFiles, false);
    }

    @Override
    protected void processFile(File file)
    {
      File hillshade = new File(file.getParent(), "dsm.tif");

      this.executeProcess(new String[] {
          "gdaldem", "hillshade", "-compute_edges", file.getAbsolutePath(), hillshade.getAbsolutePath()
      });

      if (hillshade.exists())
      {
        super.processFile(hillshade);
      }
    }

  }

}
