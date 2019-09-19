package gov.geoplatform.uasdm.controller;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import gov.geoplatform.uasdm.service.SensorService;

@Controller(url = "sensor")
public class SensorController
{
  private SensorService service;

  public SensorController()
  {
    this.service = new SensorService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "number") Integer number) throws JSONException
  {
    JSONObject page = this.service.page(request.getSessionId(), number);

    return new RestBodyResponse(page);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "sensor") String sensorJSON) throws JSONException
  {
    JSONObject sensor = new JSONObject(sensorJSON);

    JSONObject response = this.service.apply(request.getSessionId(), sensor);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "newInstance")
  public ResponseIF newInstance(ClientRequestIF request) throws JSONException
  {
    JSONObject response = this.service.newInstance(request.getSessionId());

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    JSONObject response = this.service.get(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "lock")
  public ResponseIF lock(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    JSONObject response = this.service.lock(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "unlock")
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    JSONObject response = this.service.unlock(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }
}