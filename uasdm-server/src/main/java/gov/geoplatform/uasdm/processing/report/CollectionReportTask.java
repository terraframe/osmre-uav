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
package gov.geoplatform.uasdm.processing.report;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.Platform;
import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.graph.Sensor;
import gov.geoplatform.uasdm.graph.UAV;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.UasComponentIF;
import net.geoprism.account.GeoprismActorIF;

public class CollectionReportTask implements Runnable
{
  public static enum Type {
    UPDATE, UAV, SENSOR, PLATFORM, DOCUMENT, PRODUCT, USER, INCLUDE_SIZE, SIZE, DELETE_COLLECTION, DELETE_UAV, DELETE_PRODUCT, DELETE_USER, DELETE_SENSOR, DELETE_PLATFORM, DOWNLOAD_COUNT
  }

  private Object[] component;

  private Type     type;

  public CollectionReportTask(Type type, Object... component)
  {
    this.type = type;
    this.component = component;
  }

  public Object getCollection()
  {
    return component;
  }

  public Type getType()
  {
    return type;
  }

  @Override
  @Request
  public void run()
  {
    try
    {

      if (this.type.equals(Type.UPDATE))
      {
        CollectionReport.update((UasComponentIF) this.component[0]);
      }
      else if (this.type.equals(Type.UAV))
      {
        CollectionReport.update((UAV) this.component[0]);
      }
      else if (this.type.equals(Type.SENSOR))
      {
        CollectionReport.update((Sensor) this.component[0]);
      }
      else if (this.type.equals(Type.PLATFORM))
      {
        CollectionReport.update((Platform) this.component[0]);
      }
      else if (this.type.equals(Type.DOCUMENT))
      {
        CollectionReport.update((Collection) this.component[0], (DocumentIF) this.component[1]);
      }
      else if (this.type.equals(Type.PRODUCT))
      {
        CollectionReport.update((Product) this.component[0]);
      }
      else if (this.type.equals(Type.USER))
      {
        CollectionReport.update((GeoprismActorIF) this.component[0]);
      }
      else if (this.type.equals(Type.INCLUDE_SIZE))
      {
        CollectionReport.updateIncludeSize((CollectionIF) this.component[0]);
      }
      else if (this.type.equals(Type.SIZE))
      {
        CollectionReport.updateSize((CollectionIF) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_COLLECTION))
      {
        CollectionReport.handleDelete((Collection) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_UAV))
      {
        CollectionReport.handleDelete((UAV) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_PRODUCT))
      {
        CollectionReport.handleDeleteProduct((UasComponent) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_USER))
      {
        CollectionReport.handleDelete((GeoprismActorIF) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_SENSOR))
      {
        CollectionReport.handleDelete((Sensor) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_PLATFORM))
      {
        CollectionReport.handleDelete((Platform) this.component[0]);
      }
      else if (this.type.equals(Type.DELETE_PLATFORM))
      {
        CollectionReport.handleDelete((Platform) this.component[0]);
      }
      else if (this.type.equals(Type.DOWNLOAD_COUNT))
      {
        CollectionReport.updateDownloadCount((CollectionIF) this.component[0]);
      }
    }
    catch (Exception e)
    {
      // swallow the exception
    }
  }
}
