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
package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;

import gov.geoplatform.uasdm.SpringInstanceTestClassRunner;
import gov.geoplatform.uasdm.TestConfig;
import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;
import gov.geoplatform.uasdm.util.FileTestUtils;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ODMFacadeTest
{

  @Test
  public void testFilterZip() throws IOException, URISyntaxException, CsvValidationException
  {
    File file = FileTestUtils.createZip(this.getClass().getResource("/raw").toURI());

    final ArchiveFileResource resource = new ArchiveFileResource(new FileResource(file));

    try (final ODMProcessingPayload result = ODMFacade.filterAndExtract(resource, new ODMProcessConfiguration("test"), null))
    {
      CloseableFile directory = result.getArchive().extract();
      File[] files = directory.listFiles();

      Assert.assertTrue(files.length > 0);

      for (File child : files)
      {
        final String ext = FilenameUtils.getExtension(child.getName());

        Assert.assertFalse(ext.equals("mp4"));
      }
    }
  }

  @Test
  public void testFilterTarGz() throws IOException, URISyntaxException, CsvValidationException
  {
    File file = FileTestUtils.createTarGz(this.getClass().getResource("/raw").toURI());

    final ArchiveFileResource resource = new ArchiveFileResource(new FileResource(file));

    try (final ODMProcessingPayload result = ODMFacade.filterAndExtract(resource, new ODMProcessConfiguration("test"), null))
    {
      CloseableFile directory = result.getArchive().extract();
      File[] files = directory.listFiles();

      Assert.assertTrue(files.length > 0);

      for (File child : files)
      {
        final String ext = FilenameUtils.getExtension(child.getName());

        Assert.assertFalse(ext.equals("mp4"));
      }
    }
  }
}
