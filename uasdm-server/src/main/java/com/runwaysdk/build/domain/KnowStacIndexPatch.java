package com.runwaysdk.build.domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Product;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;
import gov.geoplatform.uasdm.service.business.KnowStacBusinessService;

public class KnowStacIndexPatch implements Runnable
{
  private static final Logger     logger = LoggerFactory.getLogger(KnowStacIndexPatch.class);

  private KnowStacBusinessService service;

  public KnowStacIndexPatch(KnowStacBusinessService service)
  {
    this.service = service;
  }

  @Request
  public void run()
  {
    logger.error("Deleting entire STAC index");

    IndexService.deleteStacIndex();

    IndexService.shutdown();

    logger.error("Rebuilding the STAC index");

    this.getProducts().forEach(product -> {
      service.remove(product.getOid());

      IndexService.createStacItems(product);
    });

    logger.error("Finished");
  }

  private List<Product> getProducts()
  {
    final String product0 = MdVertexDAO.getMdVertexDAO(Product.CLASS).getDBClassName();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + product0);

    final GraphQuery<Product> query = new GraphQuery<Product>(builder.toString());

    return query.getResults();
  }

  public static void main(String[] args)
  {
    try
    {
      try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("net.geoprism.spring", "net.geoprism.registry.service.request", "gov.geoplatform.uasdm.service.request", "net.geoprism.registry.service.business", "net.geoprism.registry.service.permission", "gov.geoplatform.uasdm.service.business"))
      {
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        KnowStacIndexPatch obj = (KnowStacIndexPatch) factory.initializeBean(new KnowStacIndexPatch(context.getBean(KnowStacBusinessService.class)), "knowStacIndexPatch");
        obj.run();
      }
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

}
