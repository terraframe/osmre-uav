package com.runwaysdk.build.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.service.IndexService;

public class ResetStacIndexPatch implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(ResetStacIndexPatch.class);

  public static void main(String[] args)
  {
    try
    {
      new ResetStacIndexPatch().run();
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

  @Request
  public void run()
  {
    logger.error("Deleting entire STAC index");

    IndexService.deleteStacIndex();
    
    IndexService.shutdown();
    
    logger.error("Finished");
  }

}
