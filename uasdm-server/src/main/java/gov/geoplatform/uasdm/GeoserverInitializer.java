package gov.geoplatform.uasdm;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.model.ComponentFacade;
import gov.geoplatform.uasdm.model.ProductIF;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import net.geoprism.context.ServerContextListener;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverProperties;

public class GeoserverInitializer implements UncaughtExceptionHandler, ServerContextListener
{
  private static boolean             initialized = false;

  private static final ReentrantLock lock        = new ReentrantLock();

  private static final Logger        initLogger  = LoggerFactory.getLogger(GeoserverInitializer.class);

  private static InitializerThread   initializerThread;

  public static class InitializerThread extends Thread
  {

    private static final Logger logger = LoggerFactory.getLogger(InitializerThread.class);

    public InitializerThread()
    {
      super();
    }

    @Override
    public void run()
    {
      GeoServerRESTReader reader = GeoserverProperties.getReader();

      while (!Thread.interrupted())
      {
        try
        {
          lock.lock();

          logger.debug("Attempting to check existence of geoserver");

          if (reader.existGeoserver())
          {
            logger.debug("Geoserver available.");

            runInRequest();

            initialized = true;
            logger.debug("Geoserver initialized.");
            return; // we are done here
          }
          else
          {
            try
            {
              logger.debug("Waiting for Geoserver to start.");
              Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
              return;
            }
          }
        }
        catch (Throwable t)
        {
          // we couldn't hit the application correctly, so log the error
          // and quit the loop to avoid excessive logging
          logger.error("Unable to start the application.", t);
          return;
        }
        finally
        {
          lock.unlock();
        }
      }
    }

    @Request
    private void runInRequest()
    {
      boolean rebuild = false;

      if (!GeoserverFacade.workspaceExists(AppProperties.getPublicWorkspace()))
      {
        GeoserverFacade.publishWorkspace(AppProperties.getPublicWorkspace());

        rebuild = true;
      }

      if (!GeoserverFacade.workspaceExists(GeoserverProperties.getWorkspace()))
      {
        GeoserverFacade.publishWorkspace();

        rebuild = true;
      }

      if (rebuild)
      {
        logger.info("Geoserver workspace and store not found.  Republishing layers.");

        List<ProductIF> products = ComponentFacade.getProducts();

        for (ProductIF product : products)
        {
          product.createImageService();
        }
      }
    }

  }

  /**
   * Log the error.
   */
  @Override
  public void uncaughtException(Thread t, Throwable e)
  {
    initLogger.error("Exception occurred in thread [" + t.getName() + "].", e);
  }

  @Override
  public void initialize()
  {
  }

  @Override
  public void startup()
  {
    initGeoserver();
  }

  @Override
  public void shutdown()
  {
    cleanup();
  }

  public static boolean isInitialized()
  {
    try
    {
      lock.lock();

      return initialized;
    }
    finally
    {
      lock.unlock();
    }
  }

  public static void initGeoserver()
  {
    GeoserverInitializer init = new GeoserverInitializer();

    try
    {
      initLogger.debug("Attempting to initialize context.");

      // create another thread to avoid blocking the one starting the webapps.
      initializerThread = new InitializerThread();
      initializerThread.setUncaughtExceptionHandler(init);
      initializerThread.setDaemon(true);
      initializerThread.start();

      initLogger.debug("Context initialized...[" + GeoserverInitializer.class + "] started.");
    }
    catch (Throwable t)
    {
      initLogger.error("Could not initialize context.", t);
    }
  }

  public static void cleanup()
  {
    if (initializerThread != null)
    {
      initializerThread.interrupt();
    }
  }
}