package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class MissingMetadataMessage extends MissingMetadataMessageBase
{
  private static final long serialVersionUID = -1135031986;

  public MissingMetadataMessage()
  {
    super();
  }

  @Override
  public String getMessage()
  {
    return "Metadata missing for collection [" + this.getComponentLabel() + "]";
  }

  /**
   * Returns a label of a component associated with this task.
   * 
   * @return label of a component associated with this task.
   */
  public String getComponentLabel()
  {
    return this.getImageryComponent().getName();
  }

  public ImageryComponent getImageryComponent()
  {
    return (ImageryComponent) getComponentInstance();
  }

  public UasComponentIF getComponentInstance()
  {
    return ComponentFacade.getComponent(this.getComponent());
  }

  @Override
  public JSONObject getData()
  {
    UasComponentIF component = this.getComponentInstance();

    if (component instanceof CollectionIF)
    {
      return ( (CollectionIF) component ).toMetadataMessage();
    }

    return new JSONObject();
  }

  public static void remove(CollectionIF collection)
  {
    MissingMetadataMessageQuery query = new MissingMetadataMessageQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(collection.getOid()));

    try (OIterator<? extends MissingMetadataMessage> iterator = query.getIterator())
    {
      iterator.getAll().forEach(message -> {
        message.delete();
      });
    }
  }

}
