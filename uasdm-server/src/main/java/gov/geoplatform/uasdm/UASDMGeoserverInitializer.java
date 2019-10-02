package gov.geoplatform.uasdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.bus.Collection;
import gov.geoplatform.uasdm.bus.CollectionQuery;
import net.geoprism.gis.geoserver.GeoserverInitializerIF;

public class UASDMGeoserverInitializer implements GeoserverInitializerIF
{
  private static final Logger logger = LoggerFactory.getLogger(UASDMGeoserverInitializer.class);

  @Override
  public void initialize()
  {
    logger.info("Starting publishing of Collection image services (this may take a while on dev environments)");

    CollectionQuery cq = new CollectionQuery(new QueryFactory());

    try (OIterator<? extends Collection> it = cq.getIterator())
    {
      while (it.hasNext())
      {
        Collection col = it.next();

        logger.info("Creating image services for Collection [" + col.getName() + "].");
        col.createImageServices();
      }
    }

    logger.info("Collection image services published successfully.");
  }
}
