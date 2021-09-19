package gov.geoplatform.uasdm.graph;

import org.json.JSONObject;

import gov.geoplatform.uasdm.model.JSONSerializable;

public class UAVPageView implements JSONSerializable
{
  private UAV uav;

  public UAVPageView(UAV uav)
  {
    super();

    this.uav = uav;
  }

  @Override
  public Object toJSON()
  {
    JSONObject object = new JSONObject();
    object.put(UAV.OID, this.uav.getOid());
    object.put(UAV.SERIALNUMBER, this.uav.getSerialNumber());
    object.put(UAV.FAANUMBER, this.uav.getFaaNumber());
    object.put(UAV.DESCRIPTION, this.uav.getDescription());
    object.put(UAV.BUREAU, this.uav.getBureau().getDisplayLabel());
    object.put(UAV.PLATFORM, this.uav.getPlatform().getName());

    if (this.uav.getSeq() != null)
    {
      object.put(UAV.SEQ, this.uav.getSeq());
    }

    return object;
  }

}
