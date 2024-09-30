package gov.geoplatform.uasdm.graph;

import java.util.List;
import java.util.Optional;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.transaction.Transaction;

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

    if (sensor != null)
    {
      SensorType type = sensor.getSensorType();

      if (type.getIsMultispectral())
      {
        return true;
      }
    }

    return false;
  }

  public boolean isLidar()
  {
    Sensor sensor = this.getSensor();

    if (sensor != null)
    {
      SensorType type = sensor.getSensorType();

      return type.isLidar();
    }

    return false;
  }

}
