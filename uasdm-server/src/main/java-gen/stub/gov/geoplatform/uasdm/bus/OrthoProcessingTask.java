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
package gov.geoplatform.uasdm.bus;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.command.GenerateMetadataCommand;
import gov.geoplatform.uasdm.command.ReIndexStacItemCommand;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.odm.ODMStatus;
import gov.geoplatform.uasdm.processing.CogTifProcessor;
import gov.geoplatform.uasdm.processing.CogTifValidator;
import gov.geoplatform.uasdm.processing.GdalPNGGenerator;
import gov.geoplatform.uasdm.processing.HillshadeProcessor;
import gov.geoplatform.uasdm.processing.ODMZipPostProcessor;
import gov.geoplatform.uasdm.processing.PotreeConverterProcessor;
import gov.geoplatform.uasdm.processing.StatusMonitorIF;
import gov.geoplatform.uasdm.processing.WorkflowTaskMonitor;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class OrthoProcessingTask extends OrthoProcessingTaskBase
{
  private static final long serialVersionUID = -90821820;

  private static Logger     logger           = LoggerFactory.getLogger(OrthoProcessingTask.class);

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

  @Transaction
  public void initiate(ApplicationFileResource infile)
  {
    Collection collection = Collection.get(this.getComponent());

    Product product = (Product) collection.createProductIfNotExist(this.getProductName());

    List<DocumentIF> documents = collection.getDocuments().stream().filter(doc -> {
      return doc.getS3location().contains("/" + product.getProductName() + "/" + this.getUploadTarget() + "/");
    }).collect(Collectors.toList());

    product.addDocuments(documents);

    final StatusMonitorIF monitor = new WorkflowTaskMonitor(this);

    if (this.getUploadTarget().equals(ImageryComponent.ORTHO) && this.getProcessOrtho())
    {
      if (!new CogTifValidator().isValidCog(infile))
      {
        new CogTifProcessor(ImageryComponent.ORTHO + "/" + infile.getBaseName() + CogTifProcessor.COG_EXTENSION, product, collection, monitor).process(infile);
      }

      new GdalPNGGenerator(ImageryComponent.ORTHO + "/" + infile.getBaseName() + ".png", product, collection, monitor).process(infile);
    }

    if (this.getUploadTarget().equals(ImageryComponent.DEM) && this.getProcessDem())
    {
      if (!new CogTifValidator().isValidCog(infile))
      {
        new CogTifProcessor(ImageryComponent.DEM + "/dsm" + CogTifProcessor.COG_EXTENSION, product, collection, monitor).addDownstream(new HillshadeProcessor(ODMZipPostProcessor.DEM_GDAL + "/dsm" + CogTifProcessor.COG_EXTENSION, product, collection, new WorkflowTaskMonitor(this))).process(infile);
      }
      else
      {
        new HillshadeProcessor(ODMZipPostProcessor.DEM_GDAL + "/dsm" + CogTifProcessor.COG_EXTENSION, product, collection, new WorkflowTaskMonitor(this)).process(infile);
      }
    }

    if (this.getUploadTarget().equals(ImageryComponent.PTCLOUD) && this.getProcessPtcloud() && !StringUtils.isEmpty(AppProperties.getPotreeConverterPath()))
    {
      new PotreeConverterProcessor(ODMZipPostProcessor.POTREE, product, collection, new WorkflowTaskMonitor(this)).process(infile);
    }

    if (product.getPublished())
    {
      for (DocumentIF mappable : product.getMappableDocuments())
      {
        RemoteFileFacade.copyObject(mappable.getS3location(), AppProperties.getBucketName(), mappable.getS3location(), AppProperties.getPublicBucketName());
      }
    }

    product.updateBoundingBox(true);

    new ReIndexStacItemCommand(product).doIt();
    new GenerateMetadataCommand(collection).doIt();

    this.appLock();
    this.setStatus(ODMStatus.COMPLETED.getLabel());
    this.setMessage("Ortho has been processed");
    this.apply();
  }

  public static OrthoProcessingTask getByUploadId(String uploadId)
  {
    OrthoProcessingTaskQuery query = new OrthoProcessingTaskQuery(new QueryFactory());
    query.WHERE(query.getUploadId().EQ(uploadId));

    try (OIterator<? extends OrthoProcessingTask> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

}
