package com.runwaysdk.build.domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.bus.CollectionReport;
import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.UasComponent;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class CollectionReportQualityPatcher implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(CollectionReportQualityPatcher.class);

  public static void main(String[] args)
  {
    try
    {
      new CollectionReportQualityPatcher().run();
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        IndexService.shutdown();
        CollectionReportFacade.finish();
        CacheShutdown.shutdown();
      }
    }

  }

  public static void start()
  {
    Thread t = new Thread(new CollectionReportQualityPatcher(), "CollectionReportQualityPatcher");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    var collections = this.getCollections();
    
    logger.error("Updating product report count for " + collections.size() + " collections.");
    
    collections.forEach(collection -> {
      CollectionReport.updateProductCount(collection);
    });
    
    logger.error("Succesfully updated product count for " + collections.size() + " collection reports.");
  }

  private List<UasComponent> getCollections()
  {
    final String collection0 = MdVertexDAO.getMdVertexDAO(Collection.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + collection0);

    final GraphQuery<UasComponent> query = new GraphQuery<UasComponent>(builder.toString());

    return query.getResults();
  }
}
