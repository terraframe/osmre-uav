package gov.geoplatform.uasdm.bus;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.system.scheduler.ExecutionContext;

import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.service.business.IDMLabeledPropertyGraphSynchronizationBusinessService;
import gov.geoplatform.uasdm.ws.GlobalNotificationMessage;
import gov.geoplatform.uasdm.ws.MessageType;
import gov.geoplatform.uasdm.ws.NotificationFacade;
import gov.geoplatform.uasdm.ws.NotificationMessage;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.registry.lpg.adapter.HTTPConnector;
import net.geoprism.registry.lpg.adapter.RegistryConnectorBuilderIF;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.lpg.adapter.exception.HTTPException;
import net.geoprism.spring.ApplicationContextHolder;

public class LabeledPropertyGraphSynchronizationJob extends LabeledPropertyGraphSynchronizationJobBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -763257535;

  public LabeledPropertyGraphSynchronizationJob()
  {
    super();
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    // TODO Decide how to handle self-signed HTTPS certs
    RegistryConnectorFactory.setBuilder(new RegistryConnectorBuilderIF()
    {

      @Override
      public RegistryConnectorIF build(String url)
      {

        return new HTTPConnector(url)
        {
          @Override
          public synchronized void initialize()
          {
            try
            {

              CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
              this.setClient(httpClient);
            }
            catch (Exception e)
            {
              throw new RuntimeException(e);
            }
          }
        };
      }
    });

    LabeledPropertyGraphSynchronization synchronization = this.getSynchronization();

    try
    {
      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.SYNCHRONIZATION_JOB_CHANGE, NotificationMessage.content("oid", synchronization.getOid())));

      IDMLabeledPropertyGraphSynchronizationBusinessService service = ApplicationContextHolder.getBean(IDMLabeledPropertyGraphSynchronizationBusinessService.class);
      service.execute(synchronization);
    }
    catch (ProgrammingErrorException e)
    {
      if (e.getCause() != null && e.getCause() instanceof HTTPException)
      {
        GenericException exception = new GenericException(e);
        exception.setUserMessage("Unable to communicate with the remote server. Please ensure the remote server is available and try again.");
        throw exception;
      }
      else
      {
        throw e;
      }
    }
    finally
    {
      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.SYNCHRONIZATION_JOB_CHANGE, NotificationMessage.content("oid", synchronization.getOid())));
    }
  }

}
