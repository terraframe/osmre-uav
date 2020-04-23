package gov.geoplatform.uasdm.model;

import org.json.JSONObject;

import com.runwaysdk.ComponentIF;

public interface DocumentIF extends ComponentIF, JSONSerializable
{

  public String getS3location();

  public String getName();

  public void addGeneratedProduct(ProductIF product);

  public void delete();

  public void delete(boolean removeFromS3);

  public JSONObject toJSON();

}
