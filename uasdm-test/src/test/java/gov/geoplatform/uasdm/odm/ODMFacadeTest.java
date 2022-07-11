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

import org.junit.Ignore;

@Ignore
public class ODMFacadeTest
{

//  @Test
//  public void testFilterZip() throws IOException, URISyntaxException
//  {
//    File file = new File(this.getClass().getResource("/small-fix-with-video.zip").toURI());
//
//    final FileResource resource = new FileResource(file);
//
//    try (final CloseablePair result = ODMFacade.filter(resource))
//    {
//      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(result.getFile())))
//      {
//        ZipEntry entry;
//
//        while ( ( entry = zis.getNextEntry() ) != null)
//        {
//          final String filename = entry.getName();
//          final String ext = FilenameUtils.getExtension(filename);
//
//          Assert.assertFalse(ext.equals("mp4"));
//        }
//      }
//    }
//  }

//  @Test
//  public void testFilterTarGz() throws IOException, URISyntaxException
//  {
//    File file = new File(this.getClass().getResource("/small-fix-with-video.tar.gz").toURI());
//
//    final FileResource resource = new FileResource(file);
//
//    try (final CloseablePair result = ODMFacade.filter(resource))
//    {
//      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(result.getFile())))
//      {
//        ZipEntry entry;
//
//        while ( ( entry = zis.getNextEntry() ) != null)
//        {
//          final String filename = entry.getName();
//          final String ext = FilenameUtils.getExtension(filename);
//
//          Assert.assertFalse(ext.equals("mp4"));
//        }
//      }
//    }
//  }

}
