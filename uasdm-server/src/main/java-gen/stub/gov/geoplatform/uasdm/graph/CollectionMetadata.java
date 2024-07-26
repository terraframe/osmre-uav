package gov.geoplatform.uasdm.graph;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentWithAttributes;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.ProductIF;

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

    ((VertexObject) collection).addChild(this, EdgeType.COLLECTION_HAS_METADATA).apply();
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

}
