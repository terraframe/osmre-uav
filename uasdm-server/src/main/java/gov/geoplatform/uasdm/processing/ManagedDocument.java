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
package gov.geoplatform.uasdm.processing;

import java.util.Date;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.resource.ApplicationFileResource;

import gov.geoplatform.uasdm.graph.Document;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.service.IndexService;

public class ManagedDocument extends S3FileUpload
{
  public static class DocumentInfo
  {
    private boolean searchable;

    private String  ptEpsg;

    private Date    startDate;

    private Date    endDate;

    public DocumentInfo()
    {
    }

    public DocumentInfo(boolean searchable)
    {
      this.searchable = searchable;
    }

    public DocumentInfo(String ptEpsg)
    {
      this.ptEpsg = ptEpsg;
    }

    public boolean isSearchable()
    {
      return searchable;
    }

    public void setSearchable(boolean searchable)
    {
      this.searchable = searchable;
    }

    public String getPtEpsg()
    {
      return ptEpsg;
    }

    public void setPtEpsg(String ptEpsg)
    {
      this.ptEpsg = ptEpsg;
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }
  }

  private DocumentInfo info;

  public ManagedDocument(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor)
  {
    this(s3Path, product, collection, monitor, true);
  }

  public ManagedDocument(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor, boolean searchable)
  {
    super(s3Path, product, collection, monitor);

    this.info = new DocumentInfo(searchable);
  }

  public ManagedDocument(String s3Path, Product product, CollectionIF collection, StatusMonitorIF monitor, DocumentInfo info)
  {
    super(s3Path, product, collection, monitor);

    this.info = info;
  }

  protected ManagedDocumentTool getTool()
  {
    return ManagedDocumentTool.ODM;
  }

  @Override
  public boolean process(ApplicationFileResource res)
  {
    boolean success = super.process(res);

    if (!res.isDirectory())
    {
      String key = this.getS3Key();

      String documentName = key.substring(key.lastIndexOf("/") + 1);

      DocumentIF document = this.collection.createDocumentIfNotExist(key, documentName, null, this.getTool().name(), this.info.getPtEpsg(), this.info.getStartDate(), this.info.getEndDate());

      final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.PRODUCT_HAS_DOCUMENT);

      this.product.addChild((Document) document, mdEdge).apply();

      if (info.isSearchable())
      {
        IndexService.updateOrCreateDocument(this.collection.getAncestors(), this.collection, key, documentName);
      }
    }

    return success;
  }
}