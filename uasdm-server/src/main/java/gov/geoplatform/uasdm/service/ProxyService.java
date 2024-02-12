package gov.geoplatform.uasdm.service;

import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.remote.RemoteFileFacade;
import gov.geoplatform.uasdm.remote.RemoteFileObject;

@Service
public class ProxyService
{
 
  @Request(RequestType.SESSION)
  public RemoteFileObject file(String sessionId, String path)
  {
    return RemoteFileFacade.download(path);
  }

}
