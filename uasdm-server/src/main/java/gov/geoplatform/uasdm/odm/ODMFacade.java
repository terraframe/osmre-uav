package gov.geoplatform.uasdm.odm;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

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
  
  public static void main(String[] args)
  {
    mainInReq();
  }
  @Request
  public static void mainInReq()
  {
    TaskRemoveResponse resp = ODMFacade.taskRemove("a4eb86d5-cd26-4395-8275-942e3ae2e240");
    
    System.out.println(resp.isSuccess() + " : " + resp.getHTTPResponse().getStatusCode() + " : " + resp.getHTTPResponse().getResponse());
  }
  
//  public static void options()
//  {
//    initialize();
//    
//    HTTPResponse resp = connector.httpGet("options", new NameValuePair[] {});
//    
//    System.out.println(resp.getResponse());
//  }
  
  public static TaskOutputResponse taskOutput(String uuid)
  {
    initialize();
    
    HTTPResponse resp = connector.httpGet("task/" + uuid + "/output", new NameValuePair[] {});
    
    return new TaskOutputResponse(resp);
  }
  
  public static TaskRemoveResponse taskRemove(String uuid)
  {
    initialize();
    
    HTTPResponse resp = connector.httpPost("task/remove", "{\"uuid\":\"" + uuid + "\"}");
    
    return new TaskRemoveResponse(resp);
  }
  
  public static File taskDownload(String uuid)
  {
    initialize();
    
    try
    {
      File zip = File.createTempFile("all", ".zip");
      
      FileUtils.copyURLToFile(new URL(connector.getServerUrl() + "task/" + uuid + "/download/all.zip"), zip, 20000, 0);
      
      return zip;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  public static NewResponse taskNew(File images)
  {
    initialize();
    
    try
    {
      Part[] parts = new Part[2];
      
      parts[0] = new FilePart("images", images, "application/octet-stream", "UTF-8");
      
      parts[1] = new StringPart("options", "[{\n" + 
          "  \"name\": \"dsm\",\n" + 
          "  \"value\": \"true\"\n" + 
          "},\n" + 
          "{\n" + 
          "  \"name\": \"dtm\",\n" + 
          "  \"value\": \"true\"\n" + 
          "}]");
      
      HTTPResponse resp = connector.postAsMultipart("task/new", parts);
      
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