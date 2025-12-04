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
package gov.geoplatform.uasdm.graph;

import java.util.List;
import java.util.Optional;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.graph.Sensor.CollectionFormat;
import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentWithAttributes;
import gov.geoplatform.uasdm.model.EdgeType;

public class CollectionMetadata extends CollectionMetadataBase implements ComponentWithAttributes
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1494524200;

  public CollectionMetadata()
  {
    super();
  }

  @Transaction
  public void applyWithCollection(CollectionIF collection)
  {
    this.apply();

    ( (VertexObject) collection ).addChild(this, EdgeType.COLLECTION_HAS_METADATA).apply();
  }

  public List<Product> getProducts()
  {
    return this.getParents(EdgeType.PRODUCT_HAS_METADATA, Product.class);
  }

  public Optional<Collection> getCollection()
  {
    List<Collection> list = this.getParents(EdgeType.COLLECTION_HAS_METADATA, Collection.class);

    if (list.size() == 0)
      return Optional.empty();
    else if (list.size() > 1)
      throw new IndexOutOfBoundsException();

    return Optional.of(list.get(0));
  }

  @Override
  public UAV getUav()
  {
    String oid = this.getObjectValue(UAV);

    if (oid != null && oid.length() > 0)
    {
      return ( gov.geoplatform.uasdm.graph.UAV.get(oid) );
    }

    return null;
  }

  @Override
  public Sensor getSensor()
  {
    String oid = this.getObjectValue(SENSOR);

    if (oid != null && oid.length() > 0)
    {
      return ( gov.geoplatform.uasdm.graph.Sensor.get(oid) );
    }

    return null;
  }

  public boolean isMultiSpectral()
  {
    Sensor sensor = this.getSensor();
    if (sensor == null) return false;
    
    var formats = sensor.getCollectionFormats();
    return formats.contains(CollectionFormat.STILL_MULTISPECTRAL) || formats.contains(CollectionFormat.VIDEO_MULTISPECTRAL);
  }

  public boolean isLidar()
  {
    Sensor sensor = this.getSensor();
    if (sensor == null) return false;
    
    var formats = sensor.getCollectionFormats();
    return formats.contains(CollectionFormat.LIDAR);
  }

}
