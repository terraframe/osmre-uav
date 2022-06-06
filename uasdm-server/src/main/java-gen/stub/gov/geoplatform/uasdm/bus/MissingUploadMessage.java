package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.model.CollectionIF;
import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ImageryComponent;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class MissingUploadMessage extends MissingUploadMessageBase
{
  private static final long serialVersionUID = -495056580;

  public MissingUploadMessage()
  {
    super();
  }

  @Override
  public String getMessage()
  {
    return "No files have been uploaded for collection [" + this.getComponentLabel() + "]";
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
    JSONObject object = new JSONObject();
    object.put(COMPONENT, this.getComponent());

    return object;
  }

  public static void remove(UasComponentIF collection)
  {
    MissingUploadMessageQuery query = new MissingUploadMessageQuery(new QueryFactory());
    query.WHERE(query.getComponent().EQ(collection.getOid()));

    try (OIterator<? extends MissingUploadMessage> iterator = query.getIterator())
    {
      iterator.getAll().forEach(message -> {
        message.delete();
      });
    }
  }

}
