package gov.geoplatform.uasdm.cog;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Response;
import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;

import gov.geoplatform.uasdm.AppProperties;

public class TiTillerProxy
{
  /**
   * Invokes a HTTPS endpoint hosted by AWS API Gateway with authentication provided by AWS IAM access key credentials.
   * 
   * Special thanks to:
   * - Piotr Filipowicz @ https://inspeerity.com/blog/how-to-call-aws-api-gateway-from-the-java-code
   * - amihaiemil @ https://stackoverflow.com/questions/35985931/how-to-generate-signature-in-aws-from-java
   * 
   * @param uri
   * @return String The body of the response
   * @throws AmazonServiceException If something goes wrong while invoking the remote endpoint.
   */
  public InputStream authenticatedInvokeURL(URI endpoint, String resourcePath, Map<String, List<String>> parameters)
  {
    final BasicAWSCredentials awsCreds = new BasicAWSCredentials(AppProperties.getS3AccessKey(), AppProperties.getS3SecretKey());
    
    // Instantiate the request
    com.amazonaws.Request<Void> request = new DefaultRequest<Void>("execute-api");
    request.setHttpMethod(HttpMethodName.GET);
    request.setEndpoint(endpoint);
    request.setResourcePath(resourcePath);
    request.setParameters(parameters);

    // Sign it...
    AWS4Signer signer = new AWS4Signer();
    signer.setRegionName(AppProperties.getBucketRegion());
    signer.setServiceName(request.getServiceName());
    signer.sign(request, awsCreds);

    // Execute it and get the response...
    Response<InputStream> rsp = new AmazonHttpClient(new ClientConfiguration())
        .requestExecutionBuilder()
        .executionContext(new ExecutionContext(true))
        .request(request)
        .errorResponseHandler(new HttpResponseHandler<SdkBaseException>() {
            @Override
            public SdkBaseException handle(HttpResponse response) throws Exception {
                AmazonServiceException ase = new AmazonServiceException(response.getStatusText());
                ase.setStatusCode(response.getStatusCode());
                return ase;
            }
            @Override
            public boolean needsConnectionLeftOpen() {
                return false;
            }
        })
        .execute(new HttpResponseHandler<InputStream>() {
          @Override
          public InputStream handle(HttpResponse response) throws Exception {
              return response.getContent();
          }
          @Override
          public boolean needsConnectionLeftOpen() {
              return true;
          }
        });
    
    return rsp.getHttpResponse().getContent();
  }
  

}
