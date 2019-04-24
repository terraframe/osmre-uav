package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.uasdm.AppProperties;

public class ODMFacade
{
  private static HTTPConnector connector;
  
  public static synchronized void initialize()
  {
    if (connector != null)
    {
      return;
    }
    else
    {
      connector = new HTTPConnector();
      
//      connector.setCredentials(AppProperties.getOdmUsername(), AppProperties.getOdmPassword());
      connector.setServerUrl(AppProperties.getOdmUrl());
    }
  }
  
  public static void options()
  {
    initialize();
    
    HTTPResponse resp = connector.httpGet("options", new NameValuePair[] {});
    
    System.out.println(resp.getResponse());
  }
  
  public static TaskOutputResponse taskOutput(String uuid)
  {
    initialize();
    
    HTTPResponse resp = connector.httpGet("task/" + uuid + "/output", new NameValuePair[] {});
    
    return new TaskOutputResponse(resp);
  }
  
  public static NewResponse taskNew(File images)
  {
    initialize();
    
    try
    {
      HTTPResponse resp = connector.postAsMultipart("task/new", new Part[] {new FilePart("images", images, "application/octet-stream", "UTF-8")});
      
      return new NewResponse(resp);
    }
    catch (FileNotFoundException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public static InfoResponse taskInfo(String uuid)
  {
    initialize();
    
    HTTPResponse resp = connector.httpGet("task/" + uuid + "/info", new NameValuePair[] {});
    
    return new InfoResponse(resp);
  }
}
