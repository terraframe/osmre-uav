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
package gov.geoplatform.uasdm.bus;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.odm.GdalProcessor;
import gov.geoplatform.uasdm.odm.ODMStatus;

public class OrthoProcessingTask extends OrthoProcessingTaskBase
{
  private static final long serialVersionUID = -90821820;

  private static Logger logger = LoggerFactory.getLogger(OrthoProcessingTask.class);

  public OrthoProcessingTask()
  {
    super();
  }

  public String getImageryComponentOid()
  {
    return this.getComponent();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject obj = super.toJSON();

    return obj;
  }

  public void initiate(ApplicationResource infile)
  {
    Collection collection = Collection.get(this.getComponent());

    Product product = (Product) collection.createProductIfNotExist();

    List<DocumentIF> documents = collection.getDocuments().stream().filter(doc -> {
      return doc.getS3location().contains("/" + this.getUploadTarget() + "/");
    }).collect(Collectors.toList());

    product.addDocuments(documents);

    // Create the png for the uploaded file
    try (CloseableFile file = infile.openNewFile())
    {
      new GdalProcessor(collection, this, product, file).process();

      product.createImageService(true);

      product.updateBoundingBox();

      this.appLock();
      this.setStatus(ODMStatus.COMPLETED.getLabel());
      this.setMessage("Ortho has been processed");
      this.apply();
    }
    catch (InterruptedException e)
    {
      this.appLock();
      this.setStatus(ODMStatus.FAILED.getLabel());
      this.setMessage("The job encountered an unspecified error. [" + e.getLocalizedMessage() + "]. ");
      this.apply();
    }

  }

  /**
   * Writes the ODM output to a log file on S3, if supported by the individual
   * task implementation.
   */
  public void writeODMtoS3(JSONArray odmOutput)
  {
    // do nothing, as this does not pertain to Collections
  }
}
