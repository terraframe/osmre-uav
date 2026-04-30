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
package gov.geoplatform.uasdm.processing.raw;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.opencsv.exceptions.CsvValidationException;
import com.runwaysdk.RunwayException;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.ArchiveFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.bus.AbstractUploadTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.CollectionMetadata;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.ProcessConfiguration;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.odm.GeoLocationFileMissingException;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration;
import gov.geoplatform.uasdm.odm.ODMProcessConfiguration.FileFormat;
import gov.geoplatform.uasdm.processing.gcp.GroundControlPointFileValidator;
import gov.geoplatform.uasdm.processing.geolocation.GeoLocationFileValidator;
import gov.geoplatform.uasdm.processing.geolocation.RX1R2GeoFileConverter;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.service.ProjectManagementService;
import gov.geoplatform.uasdm.view.SiteObject;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class PostUploadValidationProcessor
{
  @SuppressWarnings("resource")
  public void validate(ApplicationFileResource glFile, ApplicationFileResource gcpFile, ArchiveFileResource archive, AbstractUploadTask task, ProcessConfiguration configuration)
  {
    CloseableFile downloadedFile = null;
    
    try {
      final UasComponentIF component = task.getImageryComponent().getUasComponent();
      
      if (configuration.isODM() && component instanceof Collection) {
        final Set<String> allImageNames = new HashSet<String>();
        final Sensor sensor = ((Collection)component).getMetadata().get().getSensor();
        final ODMProcessConfiguration odmConfig = configuration.toODM();
        
        if (!sensor.getHasGeologger() && !odmConfig.isIncludeGroundControlPointFile()) return;
        
        // We need to calculate the full set of raw inside this collection, after the upload. We'll start with imagery that's inside the zip.
        ProjectManagementService.fileNamesInArchive(archive).stream().filter(n -> !Product.GEO_LOCATION_FILE.equals(n) && !Product.GCP_FILE.equals(n)).forEach(n -> allImageNames.add(n));
        
        // Now we need to check what's in S3 and merge it with what's in the archive. This is necessary because the upload could be a partial upload.
        RemoteFileFacade.getSiteObjects(component, Collection.RAW, new LinkedList<SiteObject>(), null, null)
          .getObjects().stream().filter(n -> !Product.GEO_LOCATION_FILE.equals(n) && !Product.GCP_FILE.equals(n))
          .forEach(o -> allImageNames.add(o.getName()));
      
        // If the sensor has a geologger, we need to validate if they upload a new geo file or if they upload new imagery, regardless of whether they checked "includes geo location file" on upload.
        if (sensor.getHasGeologger()) {
          if (glFile == null) {
            try {
              RemoteFileObject remoteGeo = component.download(component.getS3location() + Collection.RAW + "/" + Product.GEO_LOCATION_FILE);
              downloadedFile = remoteGeo.openNewFile();
              glFile = new FileResource(downloadedFile);
            } catch(S3Exception ex) {
              // No problem if the file doesn't exist
            }
          }
          
          if (glFile == null)
          {
            if (odmConfig.isIncludeGeoLocationFile()) {
              throw new GeoLocationFileMissingException(odmConfig.getGeoLocationFileName());
            } else if (sensorRequiresGLF(task)) {
              task.createAction("Your collection references a sensor which generates a geo location (geologger) file, but you did not select one on upload. Please upload a geo location file before processing.", TaskActionType.WARNING);
            }
          }
          else {
            // Validate
            if (allImageNames.size() > 0)
              GeoLocationFileValidator.validate(odmConfig.getGeoLocationFormat(), glFile, allImageNames, task);
            
            // Convert
            if (odmConfig.getGeoLocationFormat().equals(FileFormat.RX1R2))
            {
              try (RX1R2GeoFileConverter reader = RX1R2GeoFileConverter.open(glFile.openNewStream()))
              {
                try (
                    FileInputStream in = new FileInputStream(reader.getOutput());
                    FileOutputStream out = new FileOutputStream(glFile.getUnderlyingFile())
                ) {
                  IOUtils.copy(in, out);
                }
              }
              
              // The format is now ODM (just in case this gets referenced elsewhere).
              odmConfig.setGeoLocationFormat(FileFormat.ODM);
            }
          }
        }
        
        // Validate GCP
        if (odmConfig.isIncludeGroundControlPointFile()) {
          if (gcpFile == null)
          {
            throw new GenericException("Could not find the specified ground control point file [" + odmConfig.getGroundControlPointFileName() + "] inside the uploaded archive.");
          }
          
          GroundControlPointFileValidator.validate(ODMProcessConfiguration.FileFormat.ODM, gcpFile, allImageNames, task);
        }
      }
    }
    catch (IOException | CsvValidationException t) {
      throw new GenericException("Exception occurred while validating uploaded geo location or gcp file: " + RunwayException.localizeThrowable(t, Session.getCurrentLocale()));
    }
    finally {
      if (downloadedFile != null)
        downloadedFile.close();
    }
  }
    
  public ApplicationFileResource findGlf(ArchiveFileResource archive) {
    ApplicationFileResource result = null;
    
    Queue<ApplicationFileResource> queue = new LinkedList<>();
    queue.add(archive);
    while(!queue.isEmpty())
    {
      var res = queue.poll();
      
      if (res.hasChildren())
      {
        for (var child : res.getChildrenFiles())
          queue.add(child);
        
        continue;
      }
      
      if (res.getName().equals(Product.GEO_LOCATION_FILE))
        result = res;
    }
    
    return result;
  }
  
  private boolean sensorRequiresGLF(AbstractUploadTask task) {
    ImageryComponent component = task.getImageryComponent();
    
    if (component instanceof Collection) {
      CollectionMetadata metadata = ( (Collection) component ).getMetadata().orElse(null);
      
      if (metadata != null && metadata.getSensor() != null) {
        return metadata.getSensor().getHasGeologger();
      }
    }
    
    return false;
  }
  
}
