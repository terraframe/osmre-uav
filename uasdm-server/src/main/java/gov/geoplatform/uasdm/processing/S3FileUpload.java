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
package gov.geoplatform.uasdm.processing;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.ProcessResult.Status;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class S3FileUpload implements Processor
{
  protected String          s3Path;

  protected StatusMonitorIF monitor;

  protected UasComponentIF  component;

  protected Product         product;

  protected List<Processor> downstream = new LinkedList<>();

  /**
   * 
   * @param s3Path
   *          The s3 target upload path, relative to the collection.
   * @param collection
   * @param isDirectory
   * @param monitor
   */
  public S3FileUpload(String s3Path, Product product, UasComponentIF component, StatusMonitorIF monitor)
  {
    this.s3Path = s3Path;
    this.monitor = monitor;
    this.product = product;
    this.component = component;
  }

  public S3FileUpload addDownstream(Processor downstreamProcessor)
  {
    this.downstream.add(downstreamProcessor);

    return this;
  }

  public String getS3Path()
  {
    return s3Path;
  }

  public void setS3Path(String s3Path)
  {
    this.s3Path = s3Path;
  }

  public UasComponentIF getComponent()
  {
    return component;
  }

  public void setComponent(UasComponentIF component)
  {
    this.component = component;
  }

  public Product getProduct()
  {
    return product;
  }

  public void setProduct(Product product)
  {
    this.product = product;
  }

  @Override
  public ProcessResult process(ApplicationFileResource res)
  {
    if (!res.exists())
    {
      this.monitor.addError("S3 uploader expected file [" + res.getAbsolutePath() + "] to exist.");

      return ProcessResult.fail();
    }

    this.uploadFile(res);

    if (this.component instanceof CollectionIF)
    {
      CollectionReportFacade.updateSize((CollectionIF) this.component).doIt();
    }

    if (this.downstream.size() > 0)
    {
      return this.downstream.stream().map(p -> p.process(res)).reduce(new ProcessResult(Status.SUCCESS), ProcessResult::join);
    }

    return ProcessResult.success(res);
  }

  protected String getS3Key()
  {
    if (this.s3Path.startsWith(this.component.getS3location()))
    {
      return this.s3Path;
    }

    return this.component.getS3location(product, s3Path);
  }

  protected void uploadFile(ApplicationFileResource res)
  {
    String key = this.getS3Key();

    if (res.isDirectory())
    {
      RemoteFileFacade.uploadDirectory(res.getUnderlyingFile(), key, this.monitor, true);

      if (this.product.isPublished())
      {
        // TODO : copyObject is more efficient but doesn't work on directories
        RemoteFileFacade.uploadDirectory(res.getUnderlyingFile(), key, AppProperties.getPublicBucketName(), this.monitor, true);
      }
    }
    else
    {
      RemoteFileFacade.uploadFile(res.getUnderlyingFile(), key, this.monitor);

      if (this.product.isPublished())
      {
        RemoteFileFacade.copyObject(key, AppProperties.getBucketName(), key, AppProperties.getPublicBucketName());
      }
    }
  }
}